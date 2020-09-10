#ifndef _HELPERS_H_
#define _HELPERS_H_

#pragma once

#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/sha.h>
#include <openssl/bio.h>
#include <openssl/md5.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int string_array_len(const char* []);
char *sign_message(char*, char*, char*);
char *hex_encode(unsigned char *, size_t);
unsigned char *calculate_sha512(char *);

#endif
