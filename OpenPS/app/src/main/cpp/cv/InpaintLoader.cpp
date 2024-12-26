#include "InpaintLoader.h"
#include "../model/InpaintModelProcessor.h"
#include "CvUtils.h"
#include <android/bitmap.h>
#include <malloc.h>
#include <memory>

jobject InpaintLoader::runInference(JNIEnv* env, jobject imageBitmap, jobject maskBitmap, const char *modelBuffer, off_t modelSize) {
    AndroidBitmapInfo bitmapInfo;
    void* pixels = nullptr;
    if (AndroidBitmap_getInfo(env, imageBitmap, &bitmapInfo) < 0) {
        return nullptr;
    }
    if (AndroidBitmap_lockPixels(env, imageBitmap, &pixels) < 0) {
        return nullptr;
    }
    size_t bufferSize = bitmapInfo.height * bitmapInfo.stride;
    std::unique_ptr<uint8_t[]> pixelBuffer(new uint8_t[bufferSize]);
    if (pixelBuffer) {
        memcpy(pixelBuffer.get(), pixels, bufferSize);
        pixels = pixelBuffer.get();
    }
    AndroidBitmap_unlockPixels(env, imageBitmap);
    auto image = CvUtils::bitmapToMat(bitmapInfo, pixels);
    if (image.empty()) {
        return nullptr;
    }

    if (AndroidBitmap_getInfo(env, maskBitmap, &bitmapInfo) < 0) {
        return nullptr;
    }
    if (AndroidBitmap_lockPixels(env, maskBitmap, &pixels) < 0) {
        return nullptr;
    }
    bufferSize = bitmapInfo.height * bitmapInfo.stride;
    std::unique_ptr<uint8_t[]> maskBuffer(new uint8_t[bufferSize]);
    if (pixelBuffer) {
        memcpy(pixelBuffer.get(), pixels, bufferSize);
        pixels = pixelBuffer.get();
    }
    AndroidBitmap_unlockPixels(env, maskBitmap);
    auto mask = CvUtils::bitmapToMat(bitmapInfo, pixels);
    if (mask.empty()) {
        return nullptr;
    }
    cv::Mat result = InpaintModelProcessor::inpaint(image, mask, modelBuffer, modelSize);
    return CvUtils::matToBitmap(env, result);
}
