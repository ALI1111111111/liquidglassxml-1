# Backdrop (Liquid Glass)

![frontPhoto](artworks/banner.jpg)

Liquid Glass effect for Android - Available for both **Jetpack Compose** and **XML Views**.

## 📖 Documentation

[![JitPack Release](https://jitpack.io/v/Kyant0/AndroidLiquidGlass.svg)](https://jitpack.io/#Kyant0/AndroidLiquidGlass)

⚠️ The library is in early alpha stage, every API may be changed, use it on your own risk.

- [Official Documentation](https://kyant.gitbook.io/backdrop)
- **[XML Quick Start Guide](./XML_QUICK_START.md)** - Get started with XML views
- **[XML Enhancements Summary](./XML_ENHANCEMENTS.md)** - See what's new in the XML version

## 🎨 Features

### Compose Version (backdrop)
- ✅ Full Liquid Glass effects with RenderEffect and RuntimeShader
- ✅ Advanced effects: Refraction, Dispersion, Vibrancy, Color Controls
- ✅ Layer-based compositing system
- ✅ Highlight variations (Specular, Ambient)
- ✅ Shadow and Inner Shadow support
- ✅ Minimum SDK: **21** (Android 5.0)

### XML Version (backdrop-xml) - **NEW!**
- ✅ **Backward compatible to API 21** (Android 5.0+)
- ✅ Same glass effects as Compose version on modern devices
- ✅ **LayerBackdropView** - Capture and blur backdrop content for glass sampling
- ✅ **Physics-based refraction** - Edge-only distortion with pure center clarity
- ✅ **Hardware-accelerated blur** - RenderEffect (API 31+) + Stack Blur (API 21+)
- ✅ Graceful degradation on older devices
- ✅ High-level components: GlassButton, GlassSlider, GlassBottomTabs
- ✅ Interactive animations with spring physics
- ✅ Gesture handling and momentum effects
- ✅ Easy-to-use XML attributes and programmatic API
- ✅ Comprehensive documentation and examples

## 📱 API Level Compatibility (XML Version)

| API Level | Android Version | Support Level | Features Available |
|-----------|----------------|---------------|-------------------|
| 21-30 | 5.0 - 10 | ✅ Basic Glass | BlurMaskFilter, ColorMatrix, basic effects |
| 31-32 | 12 - 12L | ✅ Enhanced | RenderEffect, improved blur, color filters |
| 33+ | 13+ | ✅ **Full Featured** | **All effects**, RuntimeShader, perfect parity with Compose |

## 🚀 Quick Start

### Compose Version
See [Compose Documentation](https://kyant.gitbook.io/backdrop)

### XML Version

#### 1. LayerBackdropView - Capture & Blur Background
```xml
<!-- Step 1: Wrap your background content in LayerBackdropView -->
<com.kyant.backdrop.xml.backdrop.LayerBackdropView
    android:id="@+id/backdropLayer"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Your background content (image, gradient, etc.) -->
    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/wallpaper"
        android:scaleType="centerCrop" />
        
</com.kyant.backdrop.xml.backdrop.LayerBackdropView>
```

```kotlin
// Step 2: Apply blur to the backdrop layer (NOT to each glass tile)
val density = resources.displayMetrics.density
val blurRadius = 4f * density // 4dp blur
backdropLayer.setBackdropBlur(blurRadius, dimAlpha = 0.4f)

// Step 3: Get the backdrop to pass to glass views
val backdrop = backdropLayer.getBackdrop()
```

#### 2. LiquidGlassContainer - Glass Tiles
```xml
<!-- Glass tiles sample the blurred backdrop -->
<com.kyant.backdrop.xml.views.LiquidGlassContainer
    android:id="@+id/glassCard"
    android:layout_width="300dp"
    android:layout_height="200dp"
    app:cornerRadius="24dp"
    app:refractionHeight="24dp"
    app:refractionAmount="48dp"
    app:highlightAngle="2.5">
    
    <!-- Your content here -->
    
</com.kyant.backdrop.xml.views.LiquidGlassContainer>
```

```kotlin
// Connect glass tile to the blurred backdrop
glassCard.setBackgroundSource(backdrop)
```

#### 3. High-Level Components
```xml
<com.kyant.backdrop.catalog.xml.components.GlassButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Click Me"
    app:tint="#FF0088FF"
    app:interactive="true" />
```

For complete examples, see **[XML Quick Start Guide](./XML_QUICK_START.md)**

## 🎯 Components

### Compose Components
Demo components in the [Catalog](./catalog/release/catalog-release.apk) app:
- [LiquidButton](/catalog/src/main/java/com/kyant/backdrop/catalog/components/LiquidButton.kt)
- [LiquidSlider](/catalog/src/main/java/com/kyant/backdrop/catalog/components/LiquidSlider.kt)
- [LiquidBottomTabs](/catalog/src/main/java/com/kyant/backdrop/catalog/components/LiquidBottomTabs.kt)

### XML Components - **NEW!**
Enhanced components with interactive animations:
- **[GlassButton](/catalog-xml/src/main/java/com/kyant/backdrop/catalog/xml/components/GlassButton.kt)** - Interactive button with spring animations
- **[GlassSlider](/catalog-xml/src/main/java/com/kyant/backdrop/catalog/xml/components/GlassSlider.kt)** - Slider with momentum and gesture handling
- **[GlassBottomTabs](/catalog-xml/src/main/java/com/kyant/backdrop/catalog/xml/components/GlassBottomTabs.kt)** - Bottom navigation with smooth transitions
- **[GlassIconButton](/catalog-xml/src/main/java/com/kyant/backdrop/catalog/xml/components/GlassIconButton.kt)** - Icon button variant

## 📱 Demo Apps

### Compose Catalog
- **Download**: [Catalog APK](./catalog/release/catalog-release.apk)
- **Features**: 12 demo screens showcasing all Compose effects
- **Min SDK**: 21 (Android 5.0+)

![Catalog screenshot](artworks/catalog.jpg)

### XML Catalog - **NEW!**
- **Build Target**: `catalog-xml` module
- **Features**: **100% feature parity** with Compose catalog (12 demo screens)
- **Min SDK**: 21 (Android 5.0+)
- **See**: [Catalog Parity Report](./CATALOG_PARITY_REPORT.md) for detailed comparison

**Demo Screens in Both Catalogs:**
1. ✅ Buttons - Interactive glass buttons with animations
2. ✅ Slider - Smooth slider with momentum
3. ✅ Bottom Tabs - Tab navigation with glass effects
4. ✅ Dialog - Modal glass dialogs
5. ✅ Lock Screen - iOS-style lock screen UI
6. ✅ Control Center - Gesture-driven panel with sensor support
7. ✅ Magnifier - Magnification effects
8. ✅ Glass Playground - Interactive effect controls
9. ✅ Adaptive Luminance - Real-time color adaptation
10. ✅ Scroll Container - Scrollable glass items
11. ✅ Lazy Scroll Container - RecyclerView with glass
12. ✅ Home - Navigation menu

### Legacy Demo (Deprecated)
- **(Deprecated)** [Playground app](./app/release/app-release.apk), Android 13 and above is required.

![Playground screenshot](artworks/playground_app.jpg)

## Comparing effects with iOS

iOS device: iPhone 16 Pro Max (emulator), using [GlassExplorer](https://github.com/ktiays/GlassExplorer)

Android device: Google Pixel 4 XL (the smallest width is adjusted to 440 dp to match the density of the iOS device)

Glass size: 300 x 300, corner radius: 30

|                             iOS                              |                               Android                                |
|:------------------------------------------------------------:|:--------------------------------------------------------------------:|
| ![iOS inner refraction](./artworks/ios_inner_refraction.png) | ![Android inner refraction](./artworks/android_inner_refraction.png) |

Complete comparisons:

- [Inner refraction](https://github.com/Kyant0/AndroidLiquidGlass/blob/530bed05f8342bf607463a775dea93a531f73f42/docs/Inner%20refraction%20comparisons.md)
- [Bleed](https://github.com/Kyant0/AndroidLiquidGlass/blob/530bed05f8342bf607463a775dea93a531f73f42/docs/Bleed%20comparisons.md)

## 💡 XML Architecture: LayerBackdrop System

### How It Works (Matching Compose)

#### Compose Architecture:
```kotlin
// 1. Background layer with blur
Image(...)
    .graphicsLayer { renderEffect = BlurEffect(4.dp) }
    .layerBackdrop(backdrop)

// 2. Glass tiles sample the blurred backdrop
Box(Modifier.drawBackdrop(
    backdrop = backdrop,
    effects = { refraction(...) }
))
```

#### XML Architecture (1:1 Match):
```xml
<!-- 1. LayerBackdropView captures & blurs background -->
<com.kyant.backdrop.xml.backdrop.LayerBackdropView
    android:id="@+id/backdropLayer">
    <ImageView android:src="@drawable/wallpaper" />
</com.kyant.backdrop.xml.backdrop.LayerBackdropView>
```

```kotlin
// 2. Apply blur to backdrop layer (NOT to glass tiles)
backdropLayer.setBackdropBlur(4f * density, 0.4f)

// 3. Glass tiles sample the pre-blurred backdrop
glassCard.setBackgroundSource(backdropLayer.getBackdrop())
glassCard.setRefractionEffect(RefractionEffect(24f, 48f, true))
```

### Key Architecture Principles

✅ **Blur once, sample many**: Backdrop is blurred once, all glass tiles sample it  
✅ **Edge-only refraction**: Center is pure/clear, edges bend light (physics-accurate)  
✅ **Hardware acceleration**: RenderEffect (API 31+) or Stack Blur (API 21+)  
✅ **Real-time updates**: Pre-draw listener updates backdrop every frame  

### Wrong vs. Correct Approach

❌ **WRONG**: Apply blur to each glass tile
```kotlin
// Don't do this!
glassCard.setBlurEffect(BlurEffect(4f))  // Creates magnification everywhere
```

✅ **CORRECT**: Blur the backdrop layer, tiles sample it
```kotlin
// Do this instead!
backdropLayer.setBackdropBlur(4f * density, 0.4f)
glassCard.setBackgroundSource(backdropLayer.getBackdrop())
```

## 💡 XML Migration from Compose

If you're currently using the Compose version and want to support XML views:

**Compose:**
```kotlin
Modifier.drawBackdrop(
    backdrop = backdrop,
    shape = { ContinuousCapsule },
    effects = {
        vibrancy()
        blur(4f.dp.toPx())
        refraction(16f.dp.toPx(), 24f.dp.toPx())
    }
)
```

**XML Equivalent:**
```xml
<!-- 1. Setup backdrop layer -->
<com.kyant.backdrop.xml.backdrop.LayerBackdropView
    android:id="@+id/backdropLayer">
    <ImageView android:src="@drawable/wallpaper" />
</com.kyant.backdrop.xml.backdrop.LayerBackdropView>

<!-- 2. Glass container -->
<com.kyant.backdrop.xml.views.LiquidGlassContainer
    android:id="@+id/glassCard"
    app:cornerRadius="24dp"
    app:refractionHeight="16dp"
    app:refractionAmount="24dp" />
```

```kotlin
// 3. Connect and configure
val backdrop = backdropLayer.getBackdrop()
backdropLayer.setBackdropBlur(4f * density, 0.4f)  // Blur on backdrop
glassCard.setBackgroundSource(backdrop)             // Glass samples it
glassCard.setColorFilterEffect(ColorFilterEffect.vibrant())
```

## 🛠️ Usage Examples

### Complete Control Center Example

```xml
<FrameLayout>
    <!-- Step 1: LayerBackdropView captures & blurs background -->
    <com.kyant.backdrop.xml.backdrop.LayerBackdropView
        android:id="@+id/backdropLayer"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <ImageView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:src="@drawable/wallpaper"
            android:scaleType="centerCrop" />
    </com.kyant.backdrop.xml.backdrop.LayerBackdropView>
    
    <!-- Step 2: Glass cards sample the blurred backdrop -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <com.kyant.backdrop.xml.views.LiquidGlassContainer
            android:id="@+id/card1"
            android:layout_width="match_parent"
            android:layout_height="150dp"
            app:cornerRadius="24dp"
            app:refractionHeight="24dp"
            app:refractionAmount="48dp" />
            
        <com.kyant.backdrop.xml.views.LiquidGlassContainer
            android:id="@+id/card2"
            android:layout_width="match_parent"
            android:layout_height="150dp"
            android:layout_marginTop="16dp"
            app:cornerRadius="24dp"
            app:refractionHeight="24dp"
            app:refractionAmount="48dp" />
    </LinearLayout>
</FrameLayout>
```

```kotlin
// Step 3: Setup in Activity/Fragment
val backdropLayer = findViewById<LayerBackdropView>(R.id.backdropLayer)
val card1 = findViewById<LiquidGlassContainer>(R.id.card1)
val card2 = findViewById<LiquidGlassContainer>(R.id.card2)

val density = resources.displayMetrics.density

// Apply blur to backdrop layer (4dp blur, 40% dim)
backdropLayer.setBackdropBlur(4f * density, 0.4f)

// Get the backdrop reference
val backdrop = backdropLayer.getBackdrop()

// Connect cards to the blurred backdrop
listOf(card1, card2).forEach { card ->
    card.setBackgroundSource(backdrop)
    card.setRefractionEffect(RefractionEffect(24f * density, 48f * density, true))
    card.setHighlightEffect(HighlightEffect.topLeft(falloff = 2f))
    card.setShadowEffect(ShadowEffect(0f, 4f * density, 8f * density, Color.BLACK, 0.2f))
    card.setColorFilterEffect(ColorFilterEffect.vibrant()) // 1.5x saturation
}
```

### XML Usage (Individual Glass View)

#### Basic Glass View
```xml
<com.kyant.backdrop.xml.LiquidGlassView
    android:layout_width="300dp"
    android:layout_height="200dp"
    app:cornerRadius="24dp"
    app:refractionHeight="16dp"
    app:refractionAmount="24dp"
    app:highlightAngle="2.5"
    app:highlightType="specular">
    <!-- Your content here -->
</com.kyant.backdrop.xml.LiquidGlassView>
```

#### High-Level Components
```xml
<com.kyant.backdrop.catalog.xml.components.GlassButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Click Me"
    app:tint="#FF0088FF"
    app:interactive="true" />
```

#### Programmatic API (Full Control)
```kotlin
val glassView = findViewById<LiquidGlassContainer>(R.id.glassView)
val density = resources.displayMetrics.density

// Set corner radius
glassView.setCornerRadius(32f * density)

// Physics-based refraction (edge-only, pure center)
glassView.setRefractionEffect(RefractionEffect(
    height = 24f * density,  // How far edge effect extends inward
    amount = 48f * density,  // Intensity of light bending
    hasDepthEffect = true    // Add 3D perspective
))

// Lighting effects
glassView.setHighlightEffect(HighlightEffect.topLeft(falloff = 2f))
glassView.setShadowEffect(ShadowEffect(0f, 4f * density, 8f * density, Color.BLACK, 0.2f))
glassView.setInnerShadowEffect(InnerShadowEffect(0f, 2f * density, 3f * density, Color.BLACK, 0.15f))

// Vibrancy (1.5x color saturation)
glassView.setColorFilterEffect(ColorFilterEffect.vibrant())
```

#### Dynamic Backdrop Blur (Animated Panels)
```kotlin
// Animate backdrop blur based on panel progress (0.0 to 1.0)
fun updatePanelProgress(progress: Float) {
    val density = resources.displayMetrics.density
    val blurRadius = 4f * density * progress    // 0-4dp blur
    val dimAlpha = 0.4f * progress              // 0-40% dim
    
    backdropLayer.setBackdropBlur(blurRadius, dimAlpha)
    
    // Update glass tiles with animated refraction
    glassCards.forEach { card ->
        card.setRefractionEffect(RefractionEffect(
            height = 24f * density * progress,
            amount = 48f * density * progress,
            hasDepthEffect = true
        ))
    }
}
```

### Compose Usage

#### Modifier Example
```kotlin
Modifier.drawBackdrop(
    backdrop = backdrop,
    shape = { ContinuousCapsule },
    effects = {
        vibrancy()
        blur(4f.dp.toPx())
        refraction(16f.dp.toPx(), 24f.dp.toPx())
    }
)
```

#### Custom Effects Stack
```kotlin
val effects = mutableListOf<BackdropEffect>()
effects.add(BackdropEffect.Blur(8f, 8f))
effects.add(BackdropEffect.Vibrancy)
effects.add(BackdropEffect.ColorControls(
    brightness = 0.1f,
    contrast = 1.2f,
    saturation = 1.3f
))
liquidGlassView.setBackdropEffects(effects)
```

#### Interactive Animations
```kotlin
val gestureHelper = InteractiveGlassGestureHelper(view)
gestureHelper.onAnimationUpdate = { progress, tx, ty, sx, sy ->
    liquidGlassView.setBackdropScaleX(sx)
    liquidGlassView.setBackdropScaleY(sy)
}
```

#### Layer Compositing
```kotlin
glassView1.layerId = "backgroundLayer"
glassView2.setBackdropSource(BackdropSource.Layer("backgroundLayer"))
```

---

For more, see the [Developer Guide](./DEVELOPER_GUIDE.md) for in-depth technical details, physics, and architecture.

## 🎯 Visual Effect Breakdown

### Edge-Only Refraction (Correct Physics)

```
┌─────────────────────────┐
│~~~                   ~~~│ ← Refraction at edges only
│                         │
│   PURE CLEAR CONTENT    │ ← No distortion in center
│   (blurred backdrop     │    Glass is transparent here
│    shows through)       │
│                         │
│~~~                   ~~~│ ← Liquid glass edge effect
└─────────────────────────┘
```

**How it works:**
1. **Backdrop Layer** blurs the background content (4dp)
2. **Glass Tiles** sample the blurred content
3. **Refraction Shader** applies edge-only distortion:
   - **Center**: Pure, no distortion (pure blurred backdrop)
   - **Edges**: Light bending effect (refraction shader active)

### Common Mistakes

❌ **WRONG: Blur on each tile (creates magnification everywhere)**
```
┌─────────────────────────┐
│ ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲ │ ← Bent everywhere
│╱  CONTENT WARPED   ╲│ ← Magnified center
│╲  AND DISTORTED    ╱│ ← Wrong colors
│ ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱ │
└─────────────────────────┘
```

✅ **CORRECT: Blur on backdrop, refraction on edges only**
- Center: Crystal clear view of blurred backdrop
- Edges: Realistic light bending
- Performance: Blur once, reuse many times

## 🔧 Advanced Features (XML)

### Custom Effects
```kotlin
val effects = mutableListOf<BackdropEffect>()
effects.add(BackdropEffect.Blur(8f, 8f))
effects.add(BackdropEffect.Vibrancy)
effects.add(BackdropEffect.ColorControls(
    brightness = 0.1f,
    contrast = 1.2f,
    saturation = 1.3f
))
liquidGlassView.setBackdropEffects(effects)
```

### LayerBackdropView API
```kotlin
// Create and configure backdrop layer
val backdropLayer = LayerBackdropView(context)

// Set background content (image, gradient, etc.)
backdropLayer.setBackgroundImage(drawable)
backdropLayer.setBackgroundImage(bitmap)

// Apply blur effect (hardware-accelerated on API 31+)
backdropLayer.setBackdropBlur(
    radius = 4f * density,  // Blur radius in pixels
    dimAlpha = 0.4f         // Optional dim overlay (0.0 to 1.0)
)

// Get backdrop reference for glass tiles
val backdrop = backdropLayer.getBackdrop()

// Force update (useful during animations)
backdropLayer.invalidateLayer()

// Check if ready
if ((backdrop as? LayerXmlBackdrop)?.isReady() == true) {
    // Backdrop is captured and ready to use
}
```

### Interactive Animations
```kotlin
val gestureHelper = InteractiveGlassGestureHelper(view)
gestureHelper.onAnimationUpdate = { progress, tx, ty, sx, sy ->
    // Handle animation state
    liquidGlassView.setBackdropScaleX(sx)
    liquidGlassView.setBackdropScaleY(sy)
}
```

### Real-Time Backdrop Updates
```kotlin
// For animated backgrounds or dynamic content
override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    // Backdrop automatically updates via pre-draw listener
    // Manual update if needed:
    backdropLayer.invalidateLayer()
}
```

## 📊 Performance Considerations

- **Backdrop Blur Efficiency**: Blur applied once to backdrop layer, not per glass tile
- **Hardware Acceleration**: 
  - API 31+: `RenderEffect.createBlurEffect()` (GPU-accelerated)
  - API 21-30: Stack Blur algorithm (optimized CPU blur)
- **Shader Caching**: All RuntimeShaders are automatically cached for reuse
- **API-Level Optimization**: Effects automatically adjust based on device capabilities
- **Memory Management**: Bitmaps and resources are properly recycled
- **Animation Performance**: Uses hardware-accelerated ValueAnimators
- **Real-Time Updates**: Pre-draw listener updates backdrop at 60fps

## 🤝 Contributing

Contributions are welcome! The XML version is designed to match the Compose version's capabilities while supporting older Android versions.

## 📄 License

See LICENSE file for details.

## Star history

[![Star history chart](https://api.star-history.com/svg?repos=Kyant0/AndroidLiquidGlass&type=Date)](https://www.star-history.com/#Kyant0/AndroidLiquidGlass&Date)

