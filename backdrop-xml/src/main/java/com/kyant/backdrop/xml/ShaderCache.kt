package com.kyant.backdrop.xml

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.ConcurrentHashMap

@RequiresApi(Build.VERSION_CODES.S)
internal object ShaderCache {
    private val cache = ConcurrentHashMap<String, RuntimeShader>()

    /**
     * Retrieves a compiled [RuntimeShader] from the cache.
     * If the shader is not in the cache, it will be compiled and stored.
     * @param agslString The AGSL shader code to use as the key.
     * @return A compiled [RuntimeShader] instance.
     */
    @RequiresApi(33)
    fun get(agslString: String): RuntimeShader {
        return cache.getOrPut(agslString) {
            RuntimeShader(agslString)
        }
    }
}
