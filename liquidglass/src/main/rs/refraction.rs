#pragma version(1)
#pragma rs java_package_name(com.ali.funsol.glass.liquid.tech.liquidglass)
#pragma rs_fp_relaxed

rs_allocation in_allocation;
float intensity;
int width;
int height;
int has_depth_effect; // Use int as a bool (0 or 1)

uchar4 __attribute__((kernel)) refract(uchar4 in, uint32_t x, uint32_t y) {
    float normalizedX = ((float)x / width) * 2.0 - 1.0; // -1.0 to 1.0
    float normalizedY = ((float)y / height) * 2.0 - 1.0; // -1.0 to 1.0

    // Calculate distance from the center
    float dist = sqrt(normalizedX * normalizedX + normalizedY * normalizedY);

    // Create a radial, lens-like distortion that is strongest at the center
    float strength = smoothstep(1.0, 0.0, dist); // Invert the distance

    float offsetX = normalizedX * strength * intensity;
    float offsetY = normalizedY * strength * intensity;

    // Add a parallax shift if depth effect is enabled
    if (has_depth_effect == 1) {
        offsetX += normalizedX * 0.02; // Small constant shift for parallax
        offsetY += normalizedY * 0.02;
    }

    int newX = x - (int)(offsetX * width);
    int newY = y - (int)(offsetY * height);

    newX = clamp(newX, 0, width - 1);
    newY = clamp(newY, 0, height - 1);

    return rsGetElementAt_uchar4(in_allocation, newX, newY);
}
