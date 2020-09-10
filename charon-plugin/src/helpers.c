#include "helpers.h"
MD5_CTX md5ctx;

int string_array_len(const char* array[]) {
  int i = 0;
  if (array) {
    while (array[i])
      ++i;
  }
  return i;
}

size_t b64_encoded_size(size_t inlen)
{
	size_t ret;
	ret = inlen;
	if (inlen % 3 != 0) ret += 3 - (inlen % 3);
	return (ret/3) * 4;
}

char *b64_encode(const unsigned char *in, size_t len)
{
  char b64chars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	char   *out;
	size_t  elen;
	size_t  i;
	size_t  j;
	size_t  v;
	if (in == NULL || len == 0) return NULL;
	elen = b64_encoded_size(len);
	out  = malloc(elen+1);
	out[elen] = '\0';
	for (i=0, j=0; i<len; i+=3, j+=4) {
		v = in[i];
		v = i+1 < len ? v << 8 | in[i+1] : v << 8;
		v = i+2 < len ? v << 8 | in[i+2] : v << 8;
		out[j]   = b64chars[(v >> 18) & 0x3F];
		out[j+1] = b64chars[(v >> 12) & 0x3F];
		if (i+1 < len) {
			out[j+2] = b64chars[(v >> 6) & 0x3F];
		} else {
			out[j+2] = '=';
		}
		if (i+2 < len) {
			out[j+3] = b64chars[v & 0x3F];
		} else {
			out[j+3] = '=';
		}
	}
	return out;
}

char *hex_encode(unsigned char *in, size_t length) {
	char* result = malloc(2*length + 1);
	for (int i=0; i<length; i++) {
		sprintf(&(result[i*2]), "%02x", in[i]);
	}
	result[2*length] = '\0';
	return result;
}

char *sign_message(char *header, char* original_message, char* shared_secret) {
  size_t payload_length = strlen(header) + strlen(original_message);
  size_t shared_secret_length = strlen(shared_secret);
  char delimiter[3] = "::";
  char *signature_source = malloc(payload_length + shared_secret_length + 1);
  memcpy(signature_source, header, strlen(header));
  memcpy(signature_source + strlen(header), original_message, strlen(original_message));
  memcpy(signature_source + payload_length, shared_secret, shared_secret_length);
  signature_source[payload_length + shared_secret_length] = '\0';
  char* hash = malloc(16);
  MD5_Init(&md5ctx);
  MD5_Update(&md5ctx, signature_source, strlen(signature_source));
  MD5_Final((unsigned char*)hash, &md5ctx);
  
  char *hash_base64 = b64_encode((unsigned char*)hash, 16);
  int hash_base64_length = strlen(hash_base64);

  char* signed_message = malloc(payload_length + hash_base64_length + 3);
  memcpy(signed_message, signature_source, payload_length);
  memcpy(signed_message + payload_length, delimiter, 2);
  memcpy(signed_message + payload_length + 2, hash_base64, hash_base64_length);
  signed_message[payload_length + hash_base64_length + 2] = '\0';
  free(signature_source);
  free(hash);
  free(hash_base64);
  return signed_message;
}

unsigned char *calculate_sha512(char *input) {
	unsigned char *output = malloc(SHA512_DIGEST_LENGTH);
	SHA512((const unsigned char*)input, strlen(input), output);
	return output;
}

