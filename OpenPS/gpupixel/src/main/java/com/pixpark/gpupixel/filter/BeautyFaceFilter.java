package com.pixpark.gpupixel.filter;

import com.pixpark.gpupixel.GPUPixel;

// 美颜 美白
public final class BeautyFaceFilter extends GPUPixelFilter {
    private static final String name = "BeautyFaceFilter";
    private static final String propSmoothLevel = "skin_smoothing";
    private static final String propWhiteLevel = "whiteness";

    private float smoothLevel = 0.0f;
    private float whiteLevel = 0.0f;

    public BeautyFaceFilter() {
        super(name);
    }

    public float getSmoothLevel() {
        return smoothLevel;
    }

    public void setSmoothLevel(float smoothLevel) {
        this.smoothLevel = smoothLevel;
        setProperty(propSmoothLevel, smoothLevel);
    }

    public float getWhiteLevel() {
        return whiteLevel;
    }

    public void setWhiteLevel(float whiteLevel) {
        this.whiteLevel = whiteLevel;
        setProperty(propWhiteLevel, whiteLevel);
    }

    public void setSkinMaskTexture(int width, int height, int channelCount, byte[] data) {
        GPUPixel.getInstance().runOnDraw(() -> {
            if (mNativeClassID != 0) {
                GPUPixel.nativeSetSkinMaskTexture(mNativeClassID, width, height, channelCount, data);
            }
        });
    }
}