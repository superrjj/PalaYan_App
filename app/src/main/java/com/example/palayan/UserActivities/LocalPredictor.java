package com.example.palayan.UserActivities;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public final class LocalPredictor {
    private final Interpreter interpreter;
    private final List<String> labels;

    // From assets (fallback / first run)
    public LocalPredictor(Context ctx) throws IOException {
        MappedByteBuffer model = loadModelFromAssets(ctx, "rice_disease_model.tflite");
        Interpreter.Options opts = new Interpreter.Options();
        this.interpreter = new Interpreter(model, opts);
        this.labels = loadLabelsFromAssets(ctx, "rice_disease_labels.txt");
    }

    // From files (downloaded from Firebase Storage)
    public LocalPredictor(File modelFile, File labelsFile) throws IOException {
        Interpreter.Options opts = new Interpreter.Options();
        MappedByteBuffer mbb = mapFile(modelFile);
        this.interpreter = new Interpreter(mbb, opts);
        this.labels = readLabelsFromFile(labelsFile);
    }

    public boolean isReady() { return interpreter != null && labels != null && !labels.isEmpty(); }

    public PredictionResult predict(Bitmap source) {
        if (source == null) throw new IllegalArgumentException("Bitmap is null");
        Bitmap bmp = Bitmap.createScaledBitmap(source, 224, 224, true);

        float[][][][] input = new float[1][224][224][3];
        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int px = bmp.getPixel(x, y);
                float r = ((px >> 16) & 0xFF);
                float g = ((px >> 8) & 0xFF);
                float b = (px & 0xFF);
                // EfficientNet preprocess_input: scale to [-1, 1]
                input[0][y][x][0] = (r / 127.5f) - 1.0f;
                input[0][y][x][1] = (g / 127.5f) - 1.0f;
                input[0][y][x][2] = (b / 127.5f) - 1.0f;
            }
        }

        float[][] output = new float[1][labels.size()];
        interpreter.run(input, output);

        int best = 0;
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > output[0][best]) best = i;
        }
        return new PredictionResult(labels.get(best), output[0][best], output[0]);
    }

    public void close() {
        try { interpreter.close(); } catch (Throwable ignore) {}
    }

    private static MappedByteBuffer loadModelFromAssets(Context ctx, String assetPath) throws IOException {
        try (AssetFileDescriptor fd = ctx.getAssets().openFd(assetPath);
             FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
             FileChannel channel = fis.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getLength());
        }
    }

    private static MappedByteBuffer mapFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }

    private static List<String> readLabelsFromFile(File labelsFile) throws IOException {
        List<String> list = new ArrayList<>();
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(new FileInputStream(labelsFile), "UTF-8");
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) list.add(line);
            }
        } finally {
            try { if (br != null) br.close(); } catch (Exception ignore) {}
            try { if (isr != null) isr.close(); } catch (Exception ignore) {}
        }
        return list;
    }

    private static List<String> loadLabelsFromAssets(Context ctx, String asset) throws IOException {
        List<String> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(ctx.getAssets().open(asset)));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) list.add(line);
            }
        } finally {
            try { if (br != null) br.close(); } catch (Exception ignore) {}
        }
        return list;
    }

    public static final class PredictionResult {
        public final String label;
        public final float confidence;
        public final float[] allProbs;
        public PredictionResult(String label, float confidence, float[] allProbs) {
            this.label = label; this.confidence = confidence; this.allProbs = allProbs;
        }
    }
}