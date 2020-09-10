#include <setjmp.h>
#include <stdarg.h>
#include <stddef.h>
#include "cmocka.h"

#include "tests.h"

static void test_send_internal_message(void** state) {
  plugin_config* config = read_plugin_config("./charon.conf");
  config->logger = (void *)&logger_mock_function;
  char* header = "TESTCONN";
  char* main_content = "TEST";
  check_and_spawn_accounting_process(config, 0);
  sleep(3);
  char* signed_message = sign_message(header, main_content, config->plugin_internal_shared_secret);
  char* response = send_internal_message(signed_message, 
      &config->plugin_acct_process_socket, 
      &config->plugin_local_interface, 
      config);

  int comparison = strncmp(response, signed_message, strlen(signed_message));
  assert_int_equal(comparison, 0);
  destroy_plugin_config(config);
  free(signed_message);
  free(response);
}

static void test_parse_variables(void **state) {
  const char *variables[] = {
  	"username=ognjen",
  	"password=ognjen",
  	"third_variable=1",
  	"fourth_variable=abcd",
  	0
  };
  char *extracted = extract_openvpn_info(variables);
  assert_in_range(strlen(extracted), 160, 200);
  free(extracted);
}