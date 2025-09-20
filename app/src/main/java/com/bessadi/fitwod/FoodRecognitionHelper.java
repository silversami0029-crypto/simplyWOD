package com.bessadi.fitwod;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FoodRecognitionHelper {

    private static final String MODEL_PATH = "model.tflite"; // Update with your model path
    private static final String LABEL_PATH = "labels.txt";   // Update with your labels path

    // Preprocessing constants
    private static final int IMAGE_WIDTH = 224;
    private static final int IMAGE_HEIGHT = 224;
    private static final int IMAGE_CHANNELS = 3;
    private static final int BYTES_PER_CHANNEL = 4; // Using float

    private Interpreter interpreter;

    private List<String> labels;
    private Context context;

    public FoodRecognitionHelper(Context context) {
        this.context = context;
        try {
            // 1. Create performance optimization options
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4); // Optimize for quad-core devices


            // Load model
            MappedByteBuffer model = loadModelFile(context, MODEL_PATH);
            interpreter = new Interpreter(model, options);
            // 2. Initialize interpreter with optimized options
           // interpreter = new Interpreter(loadModelFile(MODEL_PATH), options);
            // 3. Load food labels
           // labels = loadLabelList();


            // Load labels
            labels = loadLabelList(context, LABEL_PATH);
        } catch (IOException e) {
            Log.e("FoodRecognition", "Error initializing helper", e);
        }
        // Add a cleanup method to release resources

    }
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    // Load model from assets
    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Load labels from assets
    private List<String> loadLabelList(Context context, String labelPath) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    // Preprocess the image
    private ByteBuffer convertImageToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(IMAGE_WIDTH * IMAGE_HEIGHT * IMAGE_CHANNELS * BYTES_PER_CHANNEL);
        buffer.order(ByteOrder.nativeOrder());
        buffer.rewind();

        int[] pixels = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixel : pixels) {
            // Extract RGB values
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;

            // Normalize to [0,1] or [-1,1] depending on the model
            // Example: [0,1] normalization
            float r = red / 255.0f;
            float g = green / 255.0f;
            float b = blue / 255.0f;

            buffer.putFloat(r);
            buffer.putFloat(g);
            buffer.putFloat(b);
        }
        return buffer;
    }

    // Run inference
    public RecognitionResult recognize(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, true);
        ByteBuffer inputBuffer = convertImageToByteBuffer(scaledBitmap);

        // Output buffer
        float[][] output = new float[1][labels.size()];
        interpreter.run(inputBuffer, output);

        // Get the top result
        int maxIndex = 0;
        float maxConfidence = 0.0f;
        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > maxConfidence) {
                maxIndex = i;
                maxConfidence = output[0][i];
            }
        }

        String label = labels.get(maxIndex);
        return new RecognitionResult(label, maxConfidence);
    }

    // Class to hold the result
    public static class RecognitionResult {
        private String label;
        private float confidence;

        public RecognitionResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }




        public String getLabel() {
            return label;
        }

        public float getConfidence() {
            return confidence;
        }
    }
}