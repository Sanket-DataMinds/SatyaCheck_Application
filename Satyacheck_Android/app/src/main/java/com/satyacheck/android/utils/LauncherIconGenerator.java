package com.satyacheck.android.utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates missing app icons at runtime if needed
 */
public class LauncherIconGenerator {
    /**
     * Create and save launcher icons in the mipmap directories
     * This is a utility method to ensure icons exist at runtime
     * @param context Application context
     */
    public static void generateMissingIcons(Context context) {
        // Verify XML resources exist
        verifyIconResourcesExist(context);
        
        // Generate icons in different sizes
        generateAndSaveIcon(context, "mipmap-mdpi", 48, "ic_launcher.png");
        generateAndSaveIcon(context, "mipmap-hdpi", 72, "ic_launcher.png");
        generateAndSaveIcon(context, "mipmap-xhdpi", 96, "ic_launcher.png");
        generateAndSaveIcon(context, "mipmap-xxhdpi", 144, "ic_launcher.png");
        generateAndSaveIcon(context, "mipmap-xxxhdpi", 192, "ic_launcher.png");
        
        // Also generate round icons
        generateAndSaveIcon(context, "mipmap-mdpi", 48, "ic_launcher_round.png");
        generateAndSaveIcon(context, "mipmap-hdpi", 72, "ic_launcher_round.png");
        generateAndSaveIcon(context, "mipmap-xhdpi", 96, "ic_launcher_round.png");
        generateAndSaveIcon(context, "mipmap-xxhdpi", 144, "ic_launcher_round.png");
        generateAndSaveIcon(context, "mipmap-xxxhdpi", 192, "ic_launcher_round.png");
        
        android.util.Log.d("LauncherIconGenerator", "All icons generated successfully");
    }
    
    /**
     * Verify that necessary icon resources exist in the app
     * @param context Application context
     */
    private static void verifyIconResourcesExist(Context context) {
        try {
            // Verify foreground drawable resource exists
            int foregroundId = context.getResources().getIdentifier(
                "ic_launcher_home_screen", "drawable", context.getPackageName());
            
            // Verify background color resource exists
            int backgroundColorId = context.getResources().getIdentifier(
                "ic_launcher_background", "color", context.getPackageName());
            
            // Log results
            android.util.Log.d("LauncherIconGenerator", "Icon resources check - " +
                            "Foreground: " + (foregroundId != 0 ? "exists" : "missing") + ", " +
                            "Background color: " + (backgroundColorId != 0 ? "exists" : "missing"));
            
        } catch (Exception e) {
            android.util.Log.e("LauncherIconGenerator", "Error verifying icon resources", e);
        }
    }
    
    /**
     * Generate and save an icon to a specific directory
     */
    private static void generateAndSaveIcon(Context context, String directory, int size, String filename) {
        try {
            // Generate a simple shield icon
            Bitmap iconBitmap = createShieldIcon(size);
            
            // Save to both cache and files directory for redundancy
            // 1. Save to cache directory (temporary, can be cleared by system)
            String cachePath = context.getCacheDir().getAbsolutePath() + "/" + directory;
            java.io.File cacheDir = new java.io.File(cachePath);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            java.io.File cacheIconFile = new java.io.File(cacheDir, filename);
            FileOutputStream cacheOut = new FileOutputStream(cacheIconFile);
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, cacheOut);
            cacheOut.flush();
            cacheOut.close();
            
            // 2. Save to files directory (more permanent)
            String filesPath = context.getFilesDir().getAbsolutePath() + "/" + directory;
            java.io.File filesDir = new java.io.File(filesPath);
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
            
            java.io.File filesIconFile = new java.io.File(filesDir, filename);
            FileOutputStream filesOut = new FileOutputStream(filesIconFile);
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, filesOut);
            filesOut.flush();
            filesOut.close();
            
            // Log success
            android.util.Log.d("LauncherIconGenerator", "Generated icons: " + 
                              cacheIconFile.getAbsolutePath() + ", " + 
                              filesIconFile.getAbsolutePath());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create a shield icon with SatyaCheck colors
     */
    private static Bitmap createShieldIcon(int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Fill with primary color (blue)
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(63, 81, 181)); // Material Indigo 500
        canvas.drawCircle(size/2f, size/2f, size/2f, backgroundPaint);
        
        // Draw a shield shape
        Paint shieldPaint = new Paint();
        shieldPaint.setColor(Color.WHITE);
        shieldPaint.setAntiAlias(true);
        
        float padding = size * 0.2f;
        float shieldWidth = size - (padding * 2);
        float shieldHeight = shieldWidth * 1.2f;
        
        Path shieldPath = new Path();
        float centerX = size / 2f;
        float topY = padding;
        
        // Draw the shield outline
        shieldPath.moveTo(centerX, topY);
        shieldPath.lineTo(padding, topY + shieldHeight * 0.3f);
        shieldPath.lineTo(padding, topY + shieldHeight * 0.7f);
        shieldPath.cubicTo(
                padding, topY + shieldHeight * 0.9f,
                centerX - shieldWidth * 0.2f, padding + shieldHeight,
                centerX, padding + shieldHeight
        );
        shieldPath.cubicTo(
                centerX + shieldWidth * 0.2f, padding + shieldHeight,
                centerX + shieldWidth/2, topY + shieldHeight * 0.9f,
                centerX + shieldWidth/2, topY + shieldHeight * 0.7f
        );
        shieldPath.lineTo(centerX + shieldWidth/2, topY + shieldHeight * 0.3f);
        shieldPath.close();
        
        canvas.drawPath(shieldPath, shieldPaint);
        
        // Draw a checkmark or "S" inside the shield
        Paint textPaint = new Paint();
        textPaint.setColor(Color.rgb(63, 81, 181));
        textPaint.setTextSize(size * 0.5f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        
        // Draw "S" for SatyaCheck
        canvas.drawText("S", centerX, centerX + (size * 0.15f), textPaint);
        
        return bitmap;
    }
    
    /**
     * Load a launcher icon from the generated files
     * This can be used as a runtime fallback if resources fail to load
     * 
     * @param context Application context
     * @param isRoundIcon Whether to get the round icon variant
     * @return A drawable containing the launcher icon, or null if not found
     */
    public static android.graphics.drawable.Drawable loadGeneratedLauncherIcon(Context context, boolean isRoundIcon) {
        try {
            String filename = isRoundIcon ? "ic_launcher_round.png" : "ic_launcher.png";
            
            // Try files directory first (more permanent storage)
            String filesPath = context.getFilesDir().getAbsolutePath() + "/mipmap-xxxhdpi";
            java.io.File filesIconFile = new java.io.File(filesPath, filename);
            
            if (filesIconFile.exists()) {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(filesIconFile.getAbsolutePath());
                return new android.graphics.drawable.BitmapDrawable(context.getResources(), bitmap);
            }
            
            // Try cache directory as fallback
            String cachePath = context.getCacheDir().getAbsolutePath() + "/mipmap-xxxhdpi";
            java.io.File cacheIconFile = new java.io.File(cachePath, filename);
            
            if (cacheIconFile.exists()) {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(cacheIconFile.getAbsolutePath());
                return new android.graphics.drawable.BitmapDrawable(context.getResources(), bitmap);
            }
            
            // If neither exists, generate one on the fly
            android.graphics.Bitmap bitmap = createShieldIcon(192);  // Use highest resolution
            return new android.graphics.drawable.BitmapDrawable(context.getResources(), bitmap);
            
        } catch (Exception e) {
            android.util.Log.e("LauncherIconGenerator", "Error loading generated icon", e);
            return null;
        }
    }
}
