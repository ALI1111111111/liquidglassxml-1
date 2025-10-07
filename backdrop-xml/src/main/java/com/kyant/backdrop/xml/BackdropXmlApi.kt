
/**
 * Public API for the Backdrop XML library.
 * 
 * This file serves as the main entry point and exports all public components
 * of the XML version of the backdrop library, providing 1:1 feature parity
 * with the original Compose implementation.
 * 
 * ## Core Components
 * 
 * ### Views
 * - [LiquidGlassView]: Main custom view for liquid glass effects
 * - [LiquidGlassContainer]: ViewGroup container for backdrop effects
 * 
 * ### Effects
 * - [RefractionEffect]: Creates liquid glass distortion
 * - [DispersionEffect]: Creates chromatic color separation
 * - [BlurEffect]: Creates background blur
 * - [HighlightEffect]: Creates lighting highlights
 * - [ShadowEffect]: Creates drop shadows
 * - [InnerShadowEffect]: Creates inset shadows
 * 
 * ### Backdrops
 * - [XmlBackdrop]: Core backdrop interface
 * - [EmptyXmlBackdrop]: Empty backdrop implementation
 * - [CanvasXmlBackdrop]: Custom canvas drawing backdrop
 * - [DrawableXmlBackdrop]: Drawable-based backdrop
 * - [ViewXmlBackdrop]: View-based backdrop
 * - [CombinedXmlBackdrop]: Multiple backdrop combination
 * 
 * ### Utilities
 * - [LiquidGlassEffectBuilder]: Fluent API for effect configuration
 * - [LiquidGlassPresets]: Pre-configured effect combinations
 * - [BackdropXml]: Main API entry point with factories
 * 
 * ## Quick Start
 * 
 * ### Using LiquidGlassView in XML:
 * ```xml
 * <com.kyant.backdrop.xml.views.LiquidGlassView
 *     android:layout_width="200dp"
 *     android:layout_height="100dp"
 *     app:glassPreset="iosButton"
 *     app:cornerRadius="12dp" />
 * ```
 * 
 * ### Using LiquidGlassView programmatically:
 * ```kotlin
 * val glassView = LiquidGlassView(context)
 * glassView.iosGlassButton(cornerRadius = 12f)
 * glassView.setBackgroundSource(backgroundDrawable)
 * ```
 * 
 * ### Using effect builder:
 * ```kotlin
 * glassView.liquidGlass {
 *     cornerRadius(16f)
 *     strongRefraction(12f)
 *     subtleDispersion(8f)
 *     topLeftHighlight()
 *     dropShadow()
 * }
 * ```
 * 
 * ### Using LiquidGlassContainer:
 * ```kotlin
 * val container = LiquidGlassContainer(context)
 * container.frostedGlassOverlay()
 * container.addView(childView)
 * ```
 */

@file:JvmName("BackdropXmlApi")

package com.kyant.backdrop.xml

// Export main API class
typealias LiquidGlass = BackdropXml

// Export core views
typealias LiquidGlassView = com.kyant.backdrop.xml.views.LiquidGlassView
typealias LiquidGlassContainer = com.kyant.backdrop.xml.views.LiquidGlassContainer

// Export effects
typealias RefractionEffect = com.kyant.backdrop.xml.effects.RefractionEffect
typealias DispersionEffect = com.kyant.backdrop.xml.effects.DispersionEffect
typealias BlurEffect = com.kyant.backdrop.xml.effects.BlurEffect
typealias HighlightEffect = com.kyant.backdrop.xml.effects.HighlightEffect
typealias ShadowEffect = com.kyant.backdrop.xml.effects.ShadowEffect
typealias InnerShadowEffect = com.kyant.backdrop.xml.effects.InnerShadowEffect
typealias ColorFilterEffect = com.kyant.backdrop.xml.effects.ColorFilterEffect
typealias GammaAdjustmentEffect = com.kyant.backdrop.xml.effects.GammaAdjustmentEffect
typealias ExposureAdjustmentEffect = com.kyant.backdrop.xml.effects.ExposureAdjustmentEffect

// Export backdrop types
typealias XmlBackdrop = com.kyant.backdrop.xml.backdrop.XmlBackdrop
typealias EmptyXmlBackdrop = com.kyant.backdrop.xml.backdrop.EmptyXmlBackdrop
typealias CanvasXmlBackdrop = com.kyant.backdrop.xml.backdrop.CanvasXmlBackdrop
typealias DrawableXmlBackdrop = com.kyant.backdrop.xml.backdrop.DrawableXmlBackdrop
typealias ViewXmlBackdrop = com.kyant.backdrop.xml.backdrop.ViewXmlBackdrop
typealias CombinedXmlBackdrop = com.kyant.backdrop.xml.backdrop.CombinedXmlBackdrop

// Export utilities
typealias LiquidGlassEffectBuilder = com.kyant.backdrop.xml.utils.LiquidGlassEffectBuilder
typealias LiquidGlassPresets = com.kyant.backdrop.xml.presets.LiquidGlassPresets

// Export extension functions
// Note: Extensions are imported automatically when the package is imported