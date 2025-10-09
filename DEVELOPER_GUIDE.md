# Developer Guide: LiquidGlass (Backdrop) Library

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Physics-Based Refraction](#physics-based-refraction)
4. [Blur and Backdrop Sampling](#blur-and-backdrop-sampling)
5. [Effect Stack](#effect-stack)
6. [Shader Implementation](#shader-implementation)
7. [API Level Support](#api-level-support)
8. [Performance Optimization](#performance-optimization)
9. [Advanced Usage](#advanced-usage)

---

## Overview

The LiquidGlass library provides **physics-accurate glass effects** for Android, replicating the visual fidelity of iOS's glass materials. It supports both Jetpack Compose and XML Views with **100% feature parity**.

### Key Principles
- ✅ **Physics-accurate refraction**: Edge-only distortion, pure center
- ✅ **Efficient blur pipeline**: Blur backdrop once, sample many times
- ✅ **Hardware acceleration**: GPU rendering on supported devices
- ✅ **Backward compatibility**: Graceful degradation to API 21

---

## 1. Architecture

### 1.1 Layered Rendering Pipeline

The library uses a **three-layer rendering architecture**:

```
┌─────────────────────────────────────────────────┐
│ Layer 3: Content Layer                          │
│ - User content (text, icons, buttons)           │
│ - Rendered above glass effects                  │
└─────────────────────────────────────────────────┘
                    ↑
┌─────────────────────────────────────────────────┐
│ Layer 2: Glass Layer                            │
│ - Samples blurred backdrop                      │
│ - Applies refraction shader (edge-only)         │
│ - Adds vibrancy, highlights, shadows            │
└─────────────────────────────────────────────────┘
                    ↑
┌─────────────────────────────────────────────────┐
│ Layer 1: Backdrop Layer (LayerBackdropView)     │
│ - Captures background content to bitmap         │
│ - Applies blur effect (4dp typical)             │
│ - Provides blurred bitmap to glass tiles        │
└─────────────────────────────────────────────────┘
```

### 1.2 Core Components

#### XML Version
- **`LayerBackdropView`**: Captures and blurs background content
- **`LiquidGlassView`**: Core glass rendering view
- **`LiquidGlassContainer`**: ViewGroup wrapper with backdrop integration
- **Effect Classes**: `RefractionEffect`, `DispersionEffect`, `HighlightEffect`, etc.

#### Compose Version
- **`drawBackdrop` Modifier**: Main entry point for glass effects
- **`LayerBackdrop`**: Backdrop layer management
- **`BackdropEffectScope`**: DSL for configuring effects

### 1.3 Data Flow

```
Background Image → LayerBackdropView → Blur Bitmap → XmlBackdrop Interface
                                                            ↓
                                        LiquidGlassView samples backdrop
                                                            ↓
                                        Refraction Shader (AGSL/RuntimeShader)
                                                            ↓
                                        Color Filter (Vibrancy)
                                                            ↓
                                        Highlights & Shadows
                                                            ↓
                                        Final Glass Output
```

---

## 2. Physics-Based Refraction

### 2.1 Physical Model

Real glass bends light at its edges due to **Snell's Law** and **surface curvature**. The library simulates this with:

1. **Signed Distance Field (SDF)**: Calculate distance from each pixel to the nearest edge
2. **Circle Mapping**: Non-linear falloff from edge to center
3. **Normal Calculation**: Determine light direction based on surface gradient
4. **Refraction Vector**: Offset sampling coordinate based on distance and normal

### 2.2 Mathematical Foundation

The refraction shader implements this formula:

```glsl
// Signed distance to edge (negative = inside)
float sd = sdRoundedRectangle(centeredCoord, halfSize, cornerRadii);

// If deep inside (beyond refractionHeight), no distortion
if (-sd >= refractionHeight) {
    return content.eval(coord);  // Pure center ✅
}

// Calculate surface normal at this point
float2 normal = gradSdRoundedRectangle(centeredCoord, halfSize, gradRadius);

// Non-linear falloff: circleMap creates smooth transition
float t = 1.0 - (-sd / refractionHeight);  // 0 at edge, 1 at center
float refractedDistance = circleMap(t) * refractionAmount;

// Add depth effect (3D perspective)
float2 depthDirection = normalize(centeredCoord);
float2 refractedDirection = normalize(normal + depthEffect * depthDirection);

// Final refracted coordinate
float2 refractedCoord = coord + refractedDistance * refractedDirection;
return content.eval(refractedCoord);
```

### 2.3 Circle Map Function

The `circleMap` function creates a **physically accurate** falloff:

```glsl
float circleMap(float x) {
    return 1.0 - sqrt(1.0 - x * x);
}
```

This produces a **parabolic curve** that matches real glass optics, not a linear gradient.

### 2.4 Parameters

| Parameter | Type | Description | Typical Value |
|-----------|------|-------------|---------------|
| `refractionHeight` | Float (px) | How far edge effect extends inward | 24dp |
| `refractionAmount` | Float (px) | Intensity of light bending | 48dp |
| `depthEffect` | Float (0/1) | Enable 3D perspective distortion | 1.0 (enabled) |

---

## 3. Blur and Backdrop Sampling

### 3.1 Why Blur the Backdrop, Not the Tiles?

**Wrong Approach** (magnification everywhere):
```kotlin
// ❌ Don't do this!
glassCard.setBlurEffect(BlurEffect(4f))
// Result: Each tile blurs content independently → different colors, magnification
```

**Correct Approach** (shared blurred backdrop):
```kotlin
// ✅ Do this instead!
backdropLayer.setBackdropBlur(4f * density, 0.4f)
glassCard.setBackgroundSource(backdropLayer.getBackdrop())
// Result: All tiles sample same blurred content → consistent colors, edge-only refraction
```

### 3.2 Blur Implementation

#### API 31+ (Android 12+): Hardware-Accelerated Blur
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    setRenderEffect(
        RenderEffect.createBlurEffect(
            radius, radius,
            Shader.TileMode.CLAMP
        )
    )
}
```
- **GPU-accelerated**: Runs on dedicated blur hardware
- **Real-time**: 60fps performance even on large bitmaps
- **Visual only**: Blurs the display, not the bitmap pixels

#### API 21-30: Stack Blur Algorithm
```kotlin
private fun stackBlur(bitmap: Bitmap, radius: Int) {
    // Fast box blur approximation
    // Based on Mario Klingemann's algorithm
    // O(n) complexity (linear time)
}
```
- **CPU-based**: Software blur on bitmap pixels
- **Optimized**: Stack-based algorithm for speed
- **Pixel-accurate**: Modifies actual bitmap data

### 3.3 Backdrop Capture Pipeline

```kotlin
// 1. Pre-draw listener triggers before every frame
viewTreeObserver.addOnPreDrawListener {
    if (isDirty) {
        updateLayerNow()
        isDirty = false
    }
    true
}

// 2. Draw content to off-screen bitmap
fun updateLayerNow() {
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    // Draw background image
    backgroundBitmap?.let { canvas.drawBitmap(it, ...) }
    // Draw children
    for (child in children) { child.draw(canvas) }
    
    // 3. Apply blur to bitmap pixels
    applyBlurToBitmap()
}

// 4. Glass views sample the blurred bitmap
override fun drawBackdrop(canvas: Canvas, ...) {
    val bitmap = layerView.blurredLayerBitmap ?: layerView.layerBitmap
    canvas.drawBitmap(bitmap, offsetX, offsetY, null)
}
```

---

## 4. Effect Stack

Effects are applied in a **specific order** for correct compositing:

### 4.1 Effect Order (Bottom to Top)

1. **Backdrop Blur** (on LayerBackdropView)
2. **Refraction** (edge distortion)
3. **Dispersion** (chromatic aberration)
4. **Color Filter / Vibrancy** (saturation boost)
5. **Gamma/Exposure Adjustment**
6. **Blur Effect** (additional glass blur, optional)
7. **Highlight** (specular/ambient lighting)
8. **Shadow** (outer depth)
9. **Inner Shadow** (depth illusion)
10. **Surface Tint** (optional gradient overlay)

### 4.2 Effect Descriptions

#### Refraction
- **Purpose**: Simulate light bending through curved glass
- **Implementation**: RuntimeShader with SDF-based sampling
- **Visual**: Edge-only distortion, pure center
- **Performance**: GPU shader, negligible cost

#### Dispersion
- **Purpose**: Chromatic aberration (color splitting at edges)
- **Implementation**: Separate R/G/B channel offsets
- **Visual**: Rainbow-like edge effect
- **Performance**: GPU shader, minimal cost

#### Vibrancy
- **Purpose**: Increase color saturation (iOS-style)
- **Implementation**: ColorMatrix with saturation = 1.5
- **Visual**: More vivid colors without over-saturation
- **Performance**: CPU ColorFilter, very fast

#### Highlight
- **Purpose**: Simulate light reflection on glass surface
- **Implementation**: Gradient based on angle
- **Variants**: Specular (sharp), Ambient (soft)
- **Performance**: CPU gradient, fast

#### Shadow / Inner Shadow
- **Purpose**: Add depth and separation from background
- **Implementation**: BlurMaskFilter (shadow), custom shader (inner)
- **Performance**: Hardware-accelerated on modern devices

---

## 5. Shader Implementation

### 5.1 AGSL (Android Graphics Shading Language)

The library uses **AGSL** (Android's subset of GLSL) for shader effects on API 33+.

#### Refraction Shader (Simplified)
```glsl
uniform shader content;
uniform float2 size;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = coord - halfSize;
    
    // Calculate signed distance to edge
    float sd = sdRoundedRectangle(centeredCoord, halfSize, cornerRadii);
    
    // Pure center: no distortion
    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    
    // Edge: apply refraction
    float2 normal = gradSdRoundedRectangle(centeredCoord, halfSize, cornerRadii);
    float t = 1.0 - (-sd / refractionHeight);
    float dist = circleMap(t) * refractionAmount;
    
    float2 direction = normalize(normal + depthEffect * normalize(centeredCoord));
    float2 refractedCoord = coord + dist * direction;
    
    return content.eval(refractedCoord);
}
```

### 5.2 Shader Caching

All shaders are cached for reuse:

```kotlin
private val shaderCache = mutableMapOf<String, RuntimeShader>()

fun obtainRuntimeShader(key: String, source: String): RuntimeShader {
    return shaderCache.getOrPut(key) {
        RuntimeShader(source)
    }
}
```

---

## 6. API Level Support

### 6.1 Feature Matrix

| Feature | API 21-30 | API 31-32 | API 33+ |
|---------|-----------|-----------|---------|
| **Refraction** | ❌ BlurMaskFilter fallback | ✅ RenderEffect | ✅ RuntimeShader |
| **Blur** | ✅ Stack Blur (CPU) | ✅ RenderEffect (GPU) | ✅ RenderEffect (GPU) |
| **Vibrancy** | ✅ ColorMatrix | ✅ ColorMatrix | ✅ ColorMatrix |
| **Highlight** | ✅ Gradient | ✅ Gradient | ✅ Gradient |
| **Shadow** | ✅ BlurMaskFilter | ✅ BlurMaskFilter | ✅ BlurMaskFilter |
| **Dispersion** | ❌ Not supported | ❌ Not supported | ✅ RuntimeShader |
| **Hardware Acceleration** | ⚠️ Limited | ✅ Yes | ✅ Yes |

### 6.2 Graceful Degradation

The library automatically selects the best rendering path:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Use RuntimeShader for full effects
    drawAdvancedGlassEffect(canvas, bitmap, width, height)
} else {
    // Fallback to basic effects
    drawGlassEffect(canvas, width, height)
}
```

---

## 7. Performance Optimization

### 7.1 Shader Caching
- All RuntimeShaders cached in memory
- Reused across multiple views
- No recompilation overhead

### 7.2 Bitmap Recycling
```kotlin
override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    layerBitmap?.recycle()
    blurredLayerBitmap?.recycle()
}
```

### 7.3 Pre-Draw Listener Efficiency
- Only updates when `isDirty = true`
- Dirty tracking prevents unnecessary redraws
- 60fps target maintained

### 7.4 Hardware Acceleration
```kotlin
init {
    setLayerType(View.LAYER_TYPE_HARDWARE, null)
    clipToOutline = true
    outlineProvider = ViewOutlineProvider.BACKGROUND
}
```

---

## 8. Advanced Usage

### 8.1 Custom Backdrop Sources

Implement `XmlBackdrop` interface:

```kotlin
class CustomBackdrop : XmlBackdrop {
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float) {
        // Draw custom content
        canvas.drawColor(Color.BLUE)
        canvas.drawCircle(width / 2, height / 2, 100f, paint)
    }
}

glassView.setBackgroundSource(CustomBackdrop())
```

### 8.2 Dynamic Effect Animation

```kotlin
val animator = ValueAnimator.ofFloat(0f, 1f).apply {
    duration = 300
    interpolator = DecelerateInterpolator()
    addUpdateListener { animation ->
        val progress = animation.animatedValue as Float
        glassView.setRefractionEffect(RefractionEffect(
            height = 24f * density * progress,
            amount = 48f * density * progress,
            hasDepthEffect = true
        ))
    }
}
animator.start()
```

### 8.3 Multi-Layer Compositing

```kotlin
// Layer 1: Background with blur
backdropLayer1.setBackdropBlur(4f * density, 0.4f)

// Layer 2: Glass panel samples Layer 1
glassPanel.setBackgroundSource(backdropLayer1.getBackdrop())

// Layer 3: Another glass samples the panel
glassCard.setBackgroundSource(glassPanel.asBackdrop())
```

---

## 9. Debugging & Testing

### 9.1 Enable Shader Debugging

```kotlin
// In build.gradle
android {
    buildTypes {
        debug {
            shaders {
                glslcArgs += ['-g']
            }
        }
    }
}
```

### 9.2 Visual Debugging

```kotlin
// Disable effects one by one to isolate issues
glassView.setRefractionEffect(null)
glassView.setHighlightEffect(null)
glassView.setColorFilterEffect(null)
```

### 9.3 Performance Profiling

Use Android Studio Profiler:
- GPU Rendering: Check for overdraw
- Memory: Monitor bitmap allocation
- CPU: Profile blur algorithm efficiency

---

## 10. References

- [AGSL Documentation](https://developer.android.com/develop/ui/views/graphics/agsl)
- [RenderEffect API](https://developer.android.com/reference/android/graphics/RenderEffect)
- [iOS Glass Materials](https://github.com/ktiays/GlassExplorer)
- [Signed Distance Fields](https://iquilezles.org/articles/distfunctions2d/)
- [Stack Blur Algorithm](http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html)

---

## Questions?

Open an issue on GitHub or consult the [README](./README.md) for usage examples.
