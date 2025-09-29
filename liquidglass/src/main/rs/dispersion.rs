#pragma version(1)
#pragma rs java_package_name(com.ali.funsol.glass.liquid.tech.liquidglass)
#pragma rs_fp_relaxed

#include "utils.rsh"

rs_allocation in_allocation;
float intensity;
int width;
int height;

uchar4 __attribute__((kernel)) root(uint32_t x, uint32_t y) {
    float2 uv = { (float)x / width, (float)y / height };
    float2 centered_uv = uv * 2.0 - 1.0;
    float dist = length(centered_uv);
    float disp = smoothstep(0.0, 1.0, dist) * intensity;

    // Clamp the coordinates to prevent out-of-bounds memory access
    int r_x = clamp((int)x + (int)(disp * width), 0, width - 1);
    int b_x = clamp((int)x - (int)(disp * width), 0, width - 1);

    float4 r = rsUnpackColor8888(rsGetElementAt_uchar4(in_allocation, (uint32_t)r_x, y));
    float4 g = rsUnpackColor8888(rsGetElementAt_uchar4(in_allocation, x, y));
    float4 b = rsUnpackColor8888(rsGetElementAt_uchar4(in_allocation, (uint32_t)b_x, y));

    return rsPackColorTo8888((float4){ r.r, g.g, b.b, rsUnpackColor8888(rsGetElementAt_uchar4(in_allocation, x, y)).a });
}
