package com.satyacheck.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Helper class for handling Android 14+ selected photos access.
 * This uses the Photo Picker API which is the recommended way to
 * access photos on modern Android versions.
 */
public class SelectedPhotosAccessHelper {

    /**
     * Register a photo picker launcher for a single photo
     */
    public static ActivityResultLauncher<PickVisualMediaRequest> registerPhotoPickerLauncher(
            AppCompatActivity activity,
            PhotoPickerCallback callback) {
        
        return activity.registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        callback.onPhotoSelected(uri);
                    } else {
                        callback.onSelectionCancelled();
                    }
                });
    }
    
    /**
     * Register a photo picker launcher for a single photo in a fragment
     */
    public static ActivityResultLauncher<PickVisualMediaRequest> registerPhotoPickerLauncher(
            Fragment fragment,
            PhotoPickerCallback callback) {
        
        return fragment.registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        callback.onPhotoSelected(uri);
                    } else {
                        callback.onSelectionCancelled();
                    }
                });
    }
    
    /**
     * Launch the photo picker to select a single image
     */
    public static void pickImage(ActivityResultLauncher<PickVisualMediaRequest> launcher) {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
        launcher.launch(request);
    }
    
    /**
     * Interface for photo picker callbacks
     */
    public interface PhotoPickerCallback {
        void onPhotoSelected(Uri uri);
        void onSelectionCancelled();
    }
    
    /**
     * Check if the device supports the modern photo picker
     */
    public static boolean isPhotoPickerAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }
}
