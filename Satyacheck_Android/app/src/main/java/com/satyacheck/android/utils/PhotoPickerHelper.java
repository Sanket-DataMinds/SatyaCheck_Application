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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for handling photo selection with compatibility across
 * Android versions, including Android 14's selected photos access.
 */
public class PhotoPickerHelper {

    /**
     * Registers a photo picker launcher for a single image
     * 
     * @param activity The activity to register the launcher with
     * @param callback Callback with the selected image URI
     * @return The registered launcher
     */
    public static ActivityResultLauncher<PickVisualMediaRequest> registerSinglePhotoPickerLauncher(
            AppCompatActivity activity, 
            PhotoSelectedCallback callback) {
        
        return activity.registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        callback.onPhotoSelected(uri);
                    } else {
                        callback.onCancelled();
                    }
                });
    }
    
    /**
     * Registers a photo picker launcher for multiple images
     * 
     * @param activity The activity to register the launcher with
     * @param callback Callback with the selected image URIs
     * @return The registered launcher
     */
    public static ActivityResultLauncher<PickVisualMediaRequest> registerMultiplePhotoPickerLauncher(
            AppCompatActivity activity, 
            MultiplePhotosSelectedCallback callback) {
            
        return activity.registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        callback.onPhotosSelected(uris);
                    } else {
                        callback.onCancelled();
                    }
                });
    }
    
    /**
     * Launches the photo picker for selecting a single image
     * 
     * @param launcher The registered launcher
     */
    public static void pickSinglePhoto(ActivityResultLauncher<PickVisualMediaRequest> launcher) {
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
    
    /**
     * Launches the photo picker for selecting multiple images
     * 
     * @param launcher The registered launcher
     * @param maxItems Maximum number of items that can be selected
     */
    public static void pickMultiplePhotos(
            ActivityResultLauncher<PickVisualMediaRequest> launcher, 
            int maxItems) {
        
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
    
    /**
     * Callback for when a single photo is selected
     */
    public interface PhotoSelectedCallback {
        void onPhotoSelected(Uri uri);
        void onCancelled();
    }
    
    /**
     * Callback for when multiple photos are selected
     */
    public interface MultiplePhotosSelectedCallback {
        void onPhotosSelected(List<Uri> uris);
        void onCancelled();
    }
}
