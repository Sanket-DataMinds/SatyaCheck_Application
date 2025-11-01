package com.satyacheck.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Utility to generate launcher icons at runtime if they're missing
 */
public class IconGenerator {

    /**
     * Generate a simple shield icon with a solid background color
     * 
     * @param context Application context
     * @param size Icon size in pixels
     * @param backgroundColor Background color
     * @param foregroundColor Foreground color
     * @return Bitmap containing the generated icon
     */
    public static Bitmap generateShieldIcon(Context context, int size, int backgroundColor, int foregroundColor) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size/2f, size/2f, size/2f, backgroundPaint);
        
        // Draw shield shape
        Paint shieldPaint = new Paint();
        shieldPaint.setColor(foregroundColor);
        shieldPaint.setStyle(Paint.Style.FILL);
        shieldPaint.setAntiAlias(true);
        
        // Create shield path
        Path shieldPath = new Path();
        float margin = size * 0.2f;
        float width = size - 2 * margin;
        float height = size - 2 * margin;
        
        // Draw shield outline
        shieldPath.moveTo(margin + width/2, margin);
        shieldPath.lineTo(margin, margin + height * 0.25f);
        shieldPath.lineTo(margin, margin + height * 0.6f);
        shieldPath.cubicTo(
            margin, margin + height * 0.8f,
            margin + width * 0.3f, margin + height,
            margin + width/2, margin + height
        );
        shieldPath.cubicTo(
            margin + width * 0.7f, margin + height,
            margin + width, margin + height * 0.8f,
            margin + width, margin + height * 0.6f
        );
        shieldPath.lineTo(margin + width, margin + height * 0.25f);
        shieldPath.close();
        
        canvas.drawPath(shieldPath, shieldPaint);
        
        // Draw inner shape
        Paint innerPaint = new Paint();
        innerPaint.setColor(backgroundColor);
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setAntiAlias(true);
        
        Path innerPath = new Path();
        float innerMargin = margin + width * 0.15f;
        float innerWidth = width * 0.7f;
        float innerHeight = height * 0.7f;
        
        innerPath.moveTo(margin + width/2, margin + height * 0.3f);
        innerPath.lineTo(innerMargin, margin + height * 0.4f);
        innerPath.lineTo(innerMargin, margin + height * 0.65f);
        innerPath.cubicTo(
            innerMargin, margin + height * 0.75f,
            margin + width/2 - innerWidth * 0.15f, margin + height * 0.85f,
            margin + width/2, margin + height * 0.85f
        );
        innerPath.cubicTo(
            margin + width/2 + innerWidth * 0.15f, margin + height * 0.85f,
            margin + width - innerMargin, margin + height * 0.75f,
            margin + width - innerMargin, margin + height * 0.65f
        );
        innerPath.lineTo(margin + width - innerMargin, margin + height * 0.4f);
        innerPath.close();
        
        canvas.drawPath(innerPath, innerPaint);
        
        return bitmap;
    }
    
    /**
     * Save the icon to the app's internal storage
     * 
     * @param context Application context
     * @param bitmap Icon bitmap
     * @param fileName Filename to save as
     * @return True if successful, false otherwise
     */
    public static boolean saveIconToFile(Context context, Bitmap bitmap, String fileName) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Convert a bitmap to a drawable
     * 
     * @param context Application context
     * @param bitmap Source bitmap
     * @return Drawable representation of the bitmap
     */
    public static Drawable bitmapToDrawable(Context context, Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
