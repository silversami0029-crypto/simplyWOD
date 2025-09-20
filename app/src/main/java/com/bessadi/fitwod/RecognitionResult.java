package com.bessadi.fitwod;

public class RecognitionResult {
    private final String label;
    private final float confidence;

    public RecognitionResult(String label, float confidence) {
        this.label = label;
        this.confidence = confidence;
    }

    public String getLabel() { return label; }
    public float getConfidence() { return confidence; }
}
