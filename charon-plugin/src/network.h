#ifndef _NETWORK_H_
#define _NETWORK_H_

#pragma once

#include <errno.h>
#include <error.h>
#include <netdb.h>
#include <netinet/ip.h>
#include <pthread.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>
#include "charon.h"

typedef struct plugin_network_thread_arg {
  uint8_t* output_buffer;
  uint16_t output_length;
  uint8_t* input_buffer;
  uint16_t* input_length;
  struct sockaddr_in* server;
  struct sockaddr_in* local_interface;
  int wait_for_response;
} network_thread_arg;

char* send_internal_message(char*,
                            struct sockaddr_in*,
                            struct sockaddr_in*,
                            plugin_config*);
int check_if_tcp_port_open(plugin_config*);
#endif
