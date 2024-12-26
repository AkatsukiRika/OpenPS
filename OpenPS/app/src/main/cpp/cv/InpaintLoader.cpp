#include "InpaintLoader.h"
#include "CvUtils.h"
#include <android/bitmap.h>
#include <malloc.h>
#include <memory>

int InpaintLoader::storeBitmaps(JNIEnv *env, jobject imageBitmap, jobject maskBitmap) {
    AndroidBitmapInfo bitmapInfo;
    void* pixels = nullptr;
    if (AndroidBitmap_getInfo(env, imageBitmap, &bitmapInfo) < 0) {
        return 1;
    }
    if (AndroidBitmap_lockPixels(env, imageBitmap, &pixels) < 0) {
        return 1;
    }
    size_t bufferSize = bitmapInfo.height * bitmapInfo.stride;
    std::unique_ptr<uint8_t[]> pixelBuffer(new uint8_t[bufferSize]);
    if (pixelBuffer) {
        memcpy(pixelBuffer.get(), pixels, bufferSize);
        pixels = pixelBuffer.get();
    }
    AndroidBitmap_unlockPixels(env, imageBitmap);
    image = CvUtils::bitmapToMat(bitmapInfo, pixels);
    if (image.empty()) {
        return 1;
    }

    if (AndroidBitmap_getInfo(env, maskBitmap, &bitmapInfo) < 0) {
        return 1;
    }
    if (AndroidBitmap_lockPixels(env, maskBitmap, &pixels) < 0) {
        return 1;
    }
    bufferSize = bitmapInfo.height * bitmapInfo.stride;
    std::unique_ptr<uint8_t[]> maskBuffer(new uint8_t[bufferSize]);
    if (pixelBuffer) {
        memcpy(pixelBuffer.get(), pixels, bufferSize);
        pixels = pixelBuffer.get();
    }
    AndroidBitmap_unlockPixels(env, maskBitmap);
    mask = CvUtils::bitmapToMat(bitmapInfo, pixels);
    if (mask.empty()) {
        return 1;
    }
    return 0;
}

void InpaintLoader::releaseStoredBitmaps() {
    image.release();
    mask.release();
}
