package com.example.classping;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCRProcessor {
    private final ScheduleManager scheduleManager;
    private final Runnable refreshCallback;
    private final Context context;

    public OCRProcessor(ScheduleManager manager, Runnable refreshCallback, Context context) {
        this.scheduleManager = manager;
        this.refreshCallback = refreshCallback;
        this.context = context;
    }

    public void processImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            if (bitmap == null) {
                Toast.makeText(context, "Failed to load image.", Toast.LENGTH_SHORT).show();
                return;
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String recognizedText = visionText.getText();
                        if (recognizedText.trim().isEmpty()) {
                            Toast.makeText(context, "No text found in the image.", Toast.LENGTH_SHORT).show();
                        } else {
                            scheduleManager.parseFromText(recognizedText);
                            refreshCallback.run();
                            Toast.makeText(context, "Schedule parsed!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "OCR Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            Toast.makeText(context, "Error processing image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
