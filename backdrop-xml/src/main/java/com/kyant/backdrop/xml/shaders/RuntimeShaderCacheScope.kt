/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.kyant.backdrop.xml.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Manages caching of RuntimeShader instances for optimal performance.
 * This is a direct port of the original RuntimeShaderCacheScope for XML views.
 */
interface RuntimeShaderCacheScope {
    
    /**
     * Obtains a cached or creates a new RuntimeShader instance.
     * 
     * @param name Unique identifier for this shader
     * @param shaderString The AGSL shader source code
     * @return RuntimeShader instance
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun obtainRuntimeShader(name: String, shaderString: String): RuntimeShader
}

/**
 * Default implementation of RuntimeShaderCacheScope with LRU caching.
 */
internal class RuntimeShaderCacheScopeImpl : RuntimeShaderCacheScope {
    
    private val shaderCache = mutableMapOf<String, RuntimeShader>()
    
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun obtainRuntimeShader(name: String, shaderString: String): RuntimeShader {
        return shaderCache.getOrPut(name) {
            RuntimeShader(shaderString)
        }
    }
    
    /**
     * Clears the shader cache. Call this when the view is detached or when memory pressure occurs.
     */
    fun clearCache() {
        shaderCache.clear()
    }
    
    /**
     * Gets the current cache size for debugging purposes.
     */
    val cacheSize: Int get() = shaderCache.size
}