#pragma version(1)
#pragma rs java_package_name(com.ali.funsol.glass.liquid.tech.liquidglass)
#pragma rs_fp_relaxed

rs_allocation in_allocation;
float intensity;
int width;
int height;

uchar4 __attribute__((kernel)) disperse(uchar4 in, uint32_t x, uint32_t y) {
    float normalizedX = ((float)x / width) * 2.0 - 1.0;
    float normalizedY = ((float)y / height) * 2.0 - 1.0;

    float dist = sqrt(normalizedX * normalizedX + normalizedY * normalizedY);
    float strength = smoothstep(1.0, 0.0, dist);

    // Calculate offsets for each color channel
    float r_offset = strength * intensity * 1.0;
    float g_offset = strength * intensity * 0.5;
    // B channel stays in place

    int r_x = clamp((int)(x - (normalizedX * r_offset * width)), 0, width - 1);
    int r_y = clamp((int)(y - (normalizedY * r_offset * height)), 0, height - 1);

    int g_x = clamp((int)(x - (normalizedX * g_offset * width)), 0, width - 1);
    int g_y = clamp((int)(y - (normalizedY * g_offset * height)), 0, height - 1);

    uchar4 red = rsGetElementAt_uchar4(in_allocation, r_x, r_y);
    uchar4 green = rsGetElementAt_uchar4(in_allocation, g_x, g_y);

    // Combine the shifted channels
    uchar4 out;
    out.r = red.r;
    out.g = green.g;
    out.b = in.b;
    out.a = in.a;

    return out;
}
