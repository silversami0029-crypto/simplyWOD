package com.example.fitwod;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bessadi.fitwod.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraXActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private long workoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_x);

        previewView = findViewById(R.id.previewView);
        workoutId = getIntent().getLongExtra("workoutId", -1);

        if (checkPermissions()) {
            startCamera();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        permissionsNeeded.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        ActivityCompat.requestPermissions(this,
                permissionsNeeded.toArray(new String[0]),
                101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetResolution(new Size(4032, 3024)) // High resolution
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

                setupControls();

            } catch (Exception e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setupControls() {
        findViewById(R.id.btnCapture).setOnClickListener(v -> takePhoto());
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = createImageFile();
        if (photoFile == null) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String savedPath = photoFile.getAbsolutePath();
                        Log.d("CameraX", "Photo saved: " + savedPath);

                        // Return result to calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("photoPath", savedPath);
                        resultIntent.putExtra("workoutId", workoutId);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(CameraXActivity.this, "Photo capture failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "FITWOD_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }

            return new File(storageDir, imageFileName);
        } catch (Exception e) {
            Log.e("CameraX", "Error creating image file", e);
            return null;
        }
    }
}
