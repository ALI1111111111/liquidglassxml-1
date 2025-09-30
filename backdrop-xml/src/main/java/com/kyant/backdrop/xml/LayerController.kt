package com.kyant.backdrop.xml

import java.util.concurrent.ConcurrentHashMap

internal object LayerController {
    private val layers = ConcurrentHashMap<String, LayerBackdrop>()

    fun getLayer(id: String): LayerBackdrop {
        return layers.getOrPut(id) { LayerBackdrop() }
    }

    fun releaseLayer(id: String) {
        layers.remove(id)?.release()
    }
}
