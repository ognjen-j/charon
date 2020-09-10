#include <errno.h>
#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "charon.h"
#include "network.h"

#include "openvpn-plugin.h"

OPENVPN_EXPORT int openvpn_plugin_open_v3(
    const int version,
    struct openvpn_plugin_args_open_in const* args,
    struct openvpn_plugin_args_open_return* rv) {
  plugin_config* config = read_plugin_config(args->argv[1]);
  if (config == NULL) {
    fprintf(stderr, "Error configuring plugin context.\n");
    exit(-1);
  }
  config->logger = args->callbacks->plugin_log;
  config->logger(PLOG_NOTE, PLUGIN_NAME,
              "Plugin configuration loaded successfully");

  rv->type_mask = OPENVPN_PLUGIN_MASK(OPENVPN_PLUGIN_AUTH_USER_PASS_VERIFY) |
                  OPENVPN_PLUGIN_MASK(OPENVPN_PLUGIN_CLIENT_DISCONNECT);
  rv->handle = (void*)config;
  check_and_spawn_accounting_process(config, 1);

  return OPENVPN_PLUGIN_FUNC_SUCCESS;
}

OPENVPN_EXPORT int openvpn_plugin_func_v3(
    const int version,
    struct openvpn_plugin_args_func_in const* arguments,
    struct openvpn_plugin_args_func_return* retptr) {
  plugin_config* config = (plugin_config*)arguments->handle;
  int request_type = arguments->type;
  const char** environment = arguments->envp;
  char* info = extract_openvpn_info(environment);
  if (request_type == OPENVPN_PLUGIN_CLIENT_DISCONNECT) {
    int pid = fork();
    if (pid == 0) {
      return OPENVPN_PLUGIN_FUNC_SUCCESS;
    } else if (pid > 0) {
      check_and_spawn_accounting_process(config, 0);
      char* header = "STOP#";
      char* signed_message = sign_message(header, info, config->plugin_internal_shared_secret);
      send_internal_message(signed_message, &config->plugin_acct_process_socket, &config->plugin_local_interface, config);
      exit(OPENVPN_PLUGIN_FUNC_SUCCESS);
    }
  } else if (request_type == OPENVPN_PLUGIN_AUTH_USER_PASS_VERIFY) {
    config->logger(PLOG_DEBUG, PLUGIN_NAME,
                "Received a username/password verification request.");
    int pid = fork();
    if (pid == 0) {
      // the new process
      check_and_spawn_accounting_process(config, 0);
      char* header = "START#";
      char* signed_message = sign_message(header, info, config->plugin_internal_shared_secret);
      send_internal_message(signed_message, &config->plugin_acct_process_socket, &config->plugin_local_interface, config);
      free(signed_message);
      exit(0);
    } else if (pid > 0) {
      // parent process
      // We will defer authentication to not block the OpenVPN authentication
      // thread. Since the procedure involves writing the routes into the config
      // file and adding the routes to the routing table, it might take slightly
      // longer than just the round-trip to the FreeRadius server.
      return OPENVPN_PLUGIN_FUNC_DEFERRED;
    } else {
      config->logger(PLOG_ERR, PLUGIN_NAME,
                  "Error forking an authentication process.");
    }
  } else {
    config->logger(PLOG_WARN, PLUGIN_NAME, "Unknown function type encountered: %d",
                request_type);
  }
  free(info);
  return OPENVPN_PLUGIN_FUNC_ERROR;
}

OPENVPN_EXPORT void openvpn_plugin_close_v1(openvpn_plugin_handle_t handle) {
  plugin_config* config = (plugin_config*)handle;
  char* header = "EXIT#";
  char* content = "OVPNSTOP";
  char* signed_message = sign_message(header, content, config->plugin_internal_shared_secret);
  send_internal_message(signed_message, &config->plugin_acct_process_socket, &config->plugin_local_interface, config);
  free(signed_message);
  destroy_plugin_config(config);
}

char *copy_string_element(int is_first,
						 char *content,
						 char *current_element,
						 size_t current_length,
						 size_t new_element_length) {
	if(is_first) {
		content = realloc(content, new_element_length * sizeof(char));
		strncpy(content, current_element, new_element_length);
	}
	else {
		content = realloc(content, sizeof(char) * (current_length + new_element_length + 1));
		memset(content + current_length - 1, 0x23, 1);
		strncpy(content + current_length, current_element, new_element_length);
	}
	return content;
}

char* extract_openvpn_info(const char* environment[]) {
  char *content = malloc(0);
  int env_length = string_array_len(environment);
  size_t total_length = 0;
  char password_header[9] = "password=";
  for (int cnt = 0; cnt < env_length; cnt++) {
    const char* current_element = environment[cnt];
    size_t current_element_length = strlen(current_element);
    if(strncmp(current_element, password_header, 9) == 0) {
    	char *password_value = malloc(sizeof(char) * (current_element_length - 9));
    	strncpy(password_value, current_element + 9, current_element_length - 9);
    	unsigned char *hash = calculate_sha512(password_value);
    	char *encoded = hex_encode(hash, 64);
    	free(password_value);
    	free(hash);
    	char *attribute = malloc(sizeof(char) * (strlen(encoded) + 9));
    	strncpy(attribute, password_header, 9);
    	strncpy(attribute + 9, encoded, strlen(encoded));
    	size_t new_element_length = strlen(encoded) + 9;
    	content = copy_string_element(cnt == 0, content, attribute, total_length, new_element_length);
    	free(attribute);
    	free(encoded);
    	total_length += new_element_length + 1;
    }
    else {
		content = copy_string_element(cnt == 0, content, (char *)current_element, total_length, current_element_length);
		total_length += current_element_length + 1;
    }
  }
  content = realloc(content, total_length + 1);
  content[total_length - 1] = '\0';
  return content;
}

plugin_config* read_plugin_config(const char* config_file_path) {
  plugin_config* config = malloc(sizeof(plugin_config));
  FILE* config_file = fopen(config_file_path, "r");
  if (config_file == NULL) {
    fprintf(stderr, "Unable to read the configuration file at %s\n",
            config_file_path);
    free(config);
    return NULL;
  }
  char* line = malloc(1024);
  size_t len = 1024;
  ssize_t read;
  while ((read = getline(&line, &len, config_file)) != -1) {
    if (strncmp(line, CONFIG_OPTION_PLUGIN_SHARED_SECRET,
                strlen(CONFIG_OPTION_PLUGIN_SHARED_SECRET)) == 0) {
      int payload_length = read - strlen(CONFIG_OPTION_PLUGIN_SHARED_SECRET);
      if (line[read - 1] == '\n')
        payload_length--;
      config->plugin_internal_shared_secret = calloc(payload_length, sizeof(char));
      strncpy(config->plugin_internal_shared_secret, line + strlen(CONFIG_OPTION_PLUGIN_SHARED_SECRET),
              payload_length);
    } else if (strncmp(line, CONFIG_OPTION_PLUGIN_PROCESS_EXEC,
                strlen(CONFIG_OPTION_PLUGIN_PROCESS_EXEC)) == 0) {
      int payload_length = read - strlen(CONFIG_OPTION_PLUGIN_PROCESS_EXEC);
      if (line[read - 1] == '\n')
        payload_length--;
      config->plugin_acct_process_exec = calloc(payload_length, sizeof(char));
      strncpy(config->plugin_acct_process_exec, line + strlen(CONFIG_OPTION_PLUGIN_PROCESS_EXEC),
              payload_length);
    } else if (strncmp(line, CONFIG_OPTION_PLUGIN_INTERNAL_TCP_PORT,
                       strlen(CONFIG_OPTION_PLUGIN_INTERNAL_TCP_PORT)) == 0) {
      config->plugin_acct_process_port =
          (uint16_t)atoi(line + strlen(CONFIG_OPTION_PLUGIN_INTERNAL_TCP_PORT));
    }
  }
  free(line);
  fclose(config_file);
  config->config_file_path = malloc(strlen(config_file_path));
  strcpy(config->config_file_path, config_file_path);

  memset(&config->plugin_acct_process_socket, 0, sizeof(config->plugin_acct_process_socket));
  config->plugin_acct_process_socket.sin_addr.s_addr = inet_addr("127.0.0.1");
  config->plugin_acct_process_socket.sin_family = AF_INET;
  config->plugin_acct_process_socket.sin_port = htons(config->plugin_acct_process_port);

  config->plugin_local_interface.sin_family = AF_INET;
  config->plugin_local_interface.sin_addr.s_addr = inet_addr("127.0.0.1");
  config->plugin_local_interface.sin_port = 0;
  return config;
}

void destroy_plugin_config(plugin_config* config) {
  free(config->plugin_internal_shared_secret);
  free(config->plugin_acct_process_exec);
  free(config->config_file_path);
  free(config);
}

uint32_t check_and_spawn_accounting_process(plugin_config* config, int clean_session_directory) {
  // we'll rely on the TCP connection to check the existence of the subprocess
  int response_correct = check_if_tcp_port_open(config);
  if (!response_correct) {
    int pid = fork();
    if (pid == 0) {
      // the new process
      char *args[4];
      args[0] = malloc(strlen(config->plugin_acct_process_exec));
      args[1] = config->config_file_path;
      if(clean_session_directory) {
        args[2] = "1";
      }
      else {
        args[2] = "0";
      }
      args[3] = NULL;
      execve(config->plugin_acct_process_exec, args, NULL);
      free(args[0]);
    } else if (pid > 0) {
      // parent process
      config->logger(PLOG_NOTE, PLUGIN_NAME,
                  "Accounting process started with pid: %d", pid);
      return pid;
    } else {
      config->logger(PLOG_ERR, PLUGIN_NAME,
                  "Error forking an accounting process.");
    }
  }
  return 1;
}