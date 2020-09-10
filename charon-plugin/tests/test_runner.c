#define _XOPEN_SOURCE 700

#include <setjmp.h>
#include <stdarg.h>
#include <stddef.h>
#include "cmocka.h"

#include "test_environment.c"
#include "test_encode.c"

int main(void) {
  const struct CMUnitTest tests[] = {
      //cmocka_unit_test(test_send_internal_message),
      cmocka_unit_test(test_parse_variables),
      cmocka_unit_test(test_hex_encode)
  };

  return (cmocka_run_group_tests(tests, NULL, NULL));
}
