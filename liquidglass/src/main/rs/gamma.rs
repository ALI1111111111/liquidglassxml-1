#pragma version(1)
#pragma rs java_package_name(com.ali.funsol.glass.liquid.tech.liquidglass)
#pragma rs_fp_relaxed

float gammaValue;

uchar4 RS_KERNEL applyGamma(uchar4 in) {
    float4 f4 = rsUnpackColor8888(in);
    f4.r = pow(f4.r, gammaValue);
    f4.g = pow(f4.g, gammaValue);
    f4.b = pow(f4.b, gammaValue);
    return rsPackColorTo8888(f4);
}
