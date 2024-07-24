#include "CvLoader.h"
#include "CvUtils.h"
#include "../model/SkinModelProcessor.h"
#include <android/log.h>

#define LOG_TAG "xuanTest"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

int CvLoader::storeBitmap(JNIEnv *env, jobject bitmap) {
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        LOGE("获取Bitmap信息错误!");
        return 1;
    }
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        LOGE("锁定Bitmap错误!");
        return 1;
    }
    size_t bufferSize = bitmapInfo.height * bitmapInfo.stride;
    void* pixelBuffer = malloc(bufferSize);
    if (pixelBuffer != nullptr) {
        memcpy(pixelBuffer, pixels, bufferSize);
        pixels = pixelBuffer;
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    LOGI("Bitmap加载成功");
    originalMat = CvUtils::bitmapToMat(bitmapInfo, pixels);
    if (originalMat.empty()) {
        LOGE("Bitmap转Mat时出现错误!");
        return 1;
    } else {
        LOGI("Bitmap转Mat成功");
        return 0;
    }
}

int CvLoader::releaseStoredBitmap() {
    if (pixels != nullptr) {
        free(pixels);
        pixels = nullptr;
        LOGI("Bitmap释放完成");
    }
    return 0;
}

int CvLoader::runSkinModelInference(const char *modelBuffer, off_t modelSize) {
    if (pixels == nullptr || originalMat.empty()) {
        LOGE("Bitmap未加载!");
        return 1;
    }
    auto preprocessResult = SkinModelProcessor::preprocess(originalMat);
    LOGI("皮肤模型预处理完成, 预处理结果大小: %zu", preprocessResult.size());
    return 0;
}
