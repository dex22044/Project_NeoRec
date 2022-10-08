#include "image_write.h"

#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "stb_image_write.h"

ImageWriteData image_write_png(void* data, int w, int h) {
    ImageWriteData ret;
    ret.len = 0;
    ret.data = (char*) malloc(32 * 1024 * 1024);
    stbi_write_png_to_func([](void* ctx, void* data, int size) {
        memcpy(((ImageWriteData*)ctx)->data + ((ImageWriteData*)ctx)->len, data, size);
        ((ImageWriteData*)ctx)->len += size;
    }, &ret, w, h, 3, data, w * 3);
    return ret;
}