#pragma version(1)
#pragma rs java_package_name(com.ali.funsol.glass.liquid.tech.liquidglass)
#pragma rs_fp_relaxed

#include "utils.rsh"

rs_allocation in_allocation;
float intensity;
int width;
int height;
int has_depth_effect;

uchar4 __attribute__((kernel)) root(uint32_t x, uint32_t y) {
    float2 uv = { (float)x / width, (float)y / height };
    float2 centered_uv = uv * 2.0 - 1.0;
    float dist = length(centered_uv);
    float disp = smoothstep(0.0, 1.0, dist) * intensity;

    if (has_depth_effect) {
        disp *= (1.0 - uv.y);
    }

    int2 offset = { (int)(centered_uv.x * disp * width), (int)(centered_uv.y * disp * height) };

    // Clamp the coordinates to prevent out-of-bounds memory access
    int newX = clamp((int)x + offset.x, 0, width - 1);
    int newY = clamp((int)y + offset.y, 0, height - 1);

    return rsGetElementAt_uchar4(in_allocation, (uint32_t)newX, (uint32_t)newY);
}
