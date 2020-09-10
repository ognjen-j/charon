#ifndef _CHARON_H_
#define _CHARON_H_

#pragma once

#include <arpa/inet.h>
#include <stdint.h>
#include "helpers.h"
#include "openvpn-plugin.h"

#define PLUGIN_NAME "charon"
#define strlen(arg) strlen((const char*)arg)


#define CONFIG_OPTION_PLUGIN_SHARED_SECRET "plugin_internal_shared_secret="
#define CONFIG_OPTION_PLUGIN_INTERNAL_TCP_PORT "plugin_internal_tcp_port="
#define CONFIG_OPTION_PLUGIN_PROCESS_EXEC "plugin_acct_process_exec="

typedef struct PLUGIN_CONFIGURATION {
  uint16_t plugin_acct_process_port;
  struct sockaddr_in plugin_acct_process_socket;  
  char* plugin_internal_shared_secret;
  struct sockaddr_in plugin_local_interface;
  char* plugin_acct_process_exec;
  char* config_file_path;
  plugin_log_t logger;
} plugin_config;

char* extract_openvpn_info(const char*[]);
void destroy_plugin_config(plugin_config*);
plugin_config* read_plugin_config(const char*);
uint32_t check_and_spawn_accounting_process(plugin_config*, int);
#endif