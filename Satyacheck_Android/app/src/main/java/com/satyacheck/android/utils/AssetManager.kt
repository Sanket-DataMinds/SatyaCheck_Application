package com.satyacheck.android.utils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility to manage assets in the application
 */
@Singleton
class AssetManager @Inject constructor(private val context: Context) {
    
    /**
     * Copy all necessary assets to the app's private storage
     */
    fun copyAssetsToStorage() {
        try {
            // Copy privacy policy
            copyAssetToStorage("privacy_policy.md")
            
            // Copy terms of service
            copyAssetToStorage("terms_of_service.md")
            
            Log.d("AssetManager", "All assets copied successfully")
        } catch (e: Exception) {
            Log.e("AssetManager", "Error copying assets: ${e.message}")
        }
    }
    
    /**
     * Copy a specific asset to storage
     */
    private fun copyAssetToStorage(assetName: String) {
        val assetManager: AssetManager = context.assets
        val destinationFile = File(context.filesDir, assetName)
        
        // Skip if the file already exists
        if (destinationFile.exists()) {
            return
        }
        
        try {
            // Open the asset as an input stream
            assetManager.open(assetName).use { inputStream ->
                // Create the output stream to the destination file
                FileOutputStream(destinationFile).use { outputStream ->
                    // Copy the file
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }
            
            Log.d("AssetManager", "Asset copied: $assetName")
        } catch (e: IOException) {
            Log.e("AssetManager", "Failed to copy asset: $assetName", e)
            throw e
        }
    }
    
    /**
     * Get the absolute path to an asset in the app's private storage
     */
    fun getAssetPath(assetName: String): String {
        return File(context.filesDir, assetName).absolutePath
    }
    
    /**
     * Check if a specific asset exists in storage
     */
    fun assetExistsInStorage(assetName: String): Boolean {
        return File(context.filesDir, assetName).exists()
    }
}
