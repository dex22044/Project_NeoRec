#pragma once

#include <cstdlib>

struct ImageWriteData {
public:
    char* data;
    int len;
};

ImageWriteData image_write_png(void* data, int w, int h);