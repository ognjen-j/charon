#include <setjmp.h>
#include <stdarg.h>
#include <stddef.h>
#include "cmocka.h"

#include "tests.h"

static void test_hex_encode(void **state) {
	char *encoded = hex_encode(calculate_sha512("ognjen"), 64);
	assert_string_equal(encoded, "21d946468c5f03254dec307a063203a3f516d7fdd1330a248c10343edb72112fcf01e994f336204f8808bfbbee0f534e6cc59f87ce36c7628075c2eb90d12d5b");
	free(encoded);
}