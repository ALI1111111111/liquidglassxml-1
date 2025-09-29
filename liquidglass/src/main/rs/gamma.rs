#pragma version(1)
#pragma rs java_package_name(com.ali.funsol.glass.liquid.tech.liquidglass)
#pragma rs_fp_relaxed

float power;

uchar4 __attribute__((kernel)) root(uchar4 in) {
    float4 pix = rsUnpackColor8888(in);
    pix.r = pow(pix.r, power);
    pix.g = pow(pix.g, power);
    pix.b = pow(pix.b, power);
    return rsPackColorTo8888(pix);
}
