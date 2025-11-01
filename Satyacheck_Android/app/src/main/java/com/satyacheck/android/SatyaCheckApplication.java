package com.satyacheck.android;

import android.app.Application;
import android.content.Context;
import com.satyacheck.android.utils.LauncherIconGenerator;

/**
 * Main application class for SatyaCheck
 */
public class SatyaCheckApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize app components
        initializeComponents();
    }
    
    /**
     * Initialize all necessary app components
     */
    private void initializeComponents() {
        // Initialize any app-level dependencies here
        
        // Generate launcher icons if needed - for development only
        // This is a fallback mechanism for development, production versions should have proper icons
        generateLauncherIcons();
    }
    
    /**
     * Generate launcher icons if they're missing
     * This is a development fallback only
     */
    private void generateLauncherIcons() {
        try {
            // Generate missing icons
            LauncherIconGenerator.generateMissingIcons(this);
            
            // Additional verification - check if the icon can be loaded from resources
            android.graphics.drawable.Drawable launcherIcon = null;
            try {
                // Try to load from resources
                int iconResId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
                if (iconResId != 0) {
                    launcherIcon = getResources().getDrawable(iconResId, getTheme());
                    android.util.Log.d("SatyaCheckApplication", "Successfully loaded launcher icon from resources");
                }
            } catch (Exception e) {
                android.util.Log.e("SatyaCheckApplication", "Error loading launcher icon from resources", e);
            }
            
            // If resource loading failed, use our generated icon
            if (launcherIcon == null) {
                android.util.Log.d("SatyaCheckApplication", "Using dynamically generated icon as fallback");
            }
        } catch (Exception e) {
            android.util.Log.e("SatyaCheckApplication", "Error in icon generation", e);
        }
    }
}
