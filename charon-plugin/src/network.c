#include "network.h"

void* send_and_receive_message_cancellable(void* argptr) {
  network_thread_arg* arg = (network_thread_arg*)argptr;
  int socketfd = 0;
  socketfd = socket(AF_INET, SOCK_STREAM, 0);
  if (arg->local_interface != NULL) {
    bind(socketfd, (struct sockaddr*)arg->local_interface,
         sizeof(*arg->local_interface));
  }
  connect(socketfd, (struct sockaddr*)arg->server, sizeof(*arg->server));
  send(socketfd, arg->output_buffer, arg->output_length, 0);
  *arg->input_length = read(socketfd, arg->input_buffer, 4096);
  close(socketfd);
  return NULL;
}

int check_if_tcp_port_open(plugin_config *config) {
  int socketfd = socket(AF_INET, SOCK_STREAM, 0);
  bind(socketfd, (struct sockaddr*)&config->plugin_local_interface, sizeof(config->plugin_local_interface));
  int connection_result = connect(socketfd, (struct sockaddr*)&config->plugin_acct_process_socket, sizeof(config->plugin_acct_process_socket));
  if(connection_result>=0) {
    char *part1 = "PORT";
    char *part2 = "OPEN";
    char *signed_message = sign_message(part1, part2, config->plugin_internal_shared_secret);
    char *response = send_internal_message(signed_message, &config->plugin_acct_process_socket, &config->plugin_local_interface, config);
    int comparison = strncmp(response, signed_message, strlen(response));
    free(signed_message);
    free(response);
    return comparison==0;
  }
  else {
    return errno != ECONNREFUSED;
  }
}

char *send_internal_message(char* request,
                            struct sockaddr_in* server,
                            struct sockaddr_in* local_interface,
                            plugin_config* config) {
  pthread_t network_thread;
  uint8_t* input_buffer = malloc(sizeof(uint8_t) * 4096);
  uint16_t input_length = 0;
  network_thread_arg arguments;
  arguments.input_buffer = input_buffer;
  arguments.input_length = &input_length;
  arguments.local_interface = local_interface;
  arguments.output_buffer = (uint8_t *)request;
  arguments.output_length = strlen(request);
  arguments.server = server;
  time_t start_time;
  time_t iteration_time;
  time(&start_time);
  pthread_create(&network_thread, NULL, send_and_receive_message_cancellable,
                 (void*)&arguments);
  while (1) {
    if (*arguments.input_length > 0) {
      break;
    } else {
      time(&iteration_time);
      long int elapsed = iteration_time - start_time;
      if (elapsed >= 100) {
        pthread_cancel(network_thread);
        break;
      }
      sleep(1);
    }
  }
  return (char *)input_buffer;
}