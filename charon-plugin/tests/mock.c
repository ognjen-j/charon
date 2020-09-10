#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include "../src/openvpn-plugin.h"

void logger_mock_function(int log_level, 
                            const char* plugin_name,
                            const char* format,
                            ...) {
  // nop
} 