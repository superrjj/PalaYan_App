package com.example.palayan.Helper;

import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ModelValidator {
    private static final String TAG = "ModelValidator";

    public static boolean validateStage1Model(Interpreter interpreter) {
        try {
            Log.d(TAG, "Validating Stage 1 model...");

            // Test with dummy input
            ByteBuffer testInput = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
            testInput.order(ByteOrder.nativeOrder());

            // Fill with random normalized data
            for (int i = 0; i < 224 * 224 * 3; i++) {
                testInput.putFloat((float) (Math.random() * 2 - 1)); // [-1, 1] range
            }

            float[][] testOutput = new float[1][2];
            interpreter.run(testInput, testOutput);

            // Check if output is valid (sum should be close to 1.0 for softmax)
            float sum = testOutput[0][0] + testOutput[0][1];
            boolean isValid = Math.abs(sum - 1.0f) < 0.1f;

            Log.d(TAG, "Stage 1 validation - Output sum: " + sum + ", Valid: " + isValid);
            Log.d(TAG, "Stage 1 output - Rice: " + testOutput[0][0] + ", NonRice: " + testOutput[0][1]);

            return isValid;

        } catch (Exception e) {
            Log.e(TAG, "Stage 1 validation failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateStage2Model(Interpreter interpreter, int outputSize) {
        try {
            Log.d(TAG, "Validating Stage 2 model with output size: " + outputSize);

            // Test with dummy input
            ByteBuffer testInput = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
            testInput.order(ByteOrder.nativeOrder());

            // Fill with random normalized data
            for (int i = 0; i < 224 * 224 * 3; i++) {
                testInput.putFloat((float) (Math.random() * 2 - 1)); // [-1, 1] range
            }

            float[][] testOutput = new float[1][outputSize];
            interpreter.run(testInput, testOutput);

            // Check if output is valid (sum should be close to 1.0 for softmax)
            float sum = 0;
            for (float value : testOutput[0]) {
                sum += value;
            }
            boolean isValid = Math.abs(sum - 1.0f) < 0.1f;

            Log.d(TAG, "Stage 2 validation - Output sum: " + sum + ", Valid: " + isValid);
            Log.d(TAG, "Stage 2 output: " + java.util.Arrays.toString(testOutput[0]));

            return isValid;

        } catch (Exception e) {
            Log.e(TAG, "Stage 2 validation failed: " + e.getMessage());
            return false;
        }
    }

    public static void logModelInfo(Interpreter interpreter, String modelName) {
        try {
            Log.d(TAG, "=== " + modelName + " Model Info ===");

            // Input tensor info
            int[] inputShape = interpreter.getInputTensor(0).shape();
            Log.d(TAG, "Input shape: " + java.util.Arrays.toString(inputShape));

            // Output tensor info
            int[] outputShape = interpreter.getOutputTensor(0).shape();
            Log.d(TAG, "Output shape: " + java.util.Arrays.toString(outputShape));

            Log.d(TAG, "=== End " + modelName + " Info ===");

        } catch (Exception e) {
            Log.e(TAG, "Failed to log model info: " + e.getMessage());
        }
    }
}