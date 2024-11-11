#include "CvUtils.h"
#include <android/log.h>

#define LOG_TAG "xuanTest"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

cv::Mat CvUtils::bitmapToMat(const AndroidBitmapInfo bitmapInfo, const void *pixels) {
    cv::Mat resultMat;
    if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        resultMat.create(bitmapInfo.height, bitmapInfo.width, CV_8UC4);
        memcpy(resultMat.data, pixels, bitmapInfo.height * bitmapInfo.stride);
        return resultMat;
    } else {
        LOGE("传入的Bitmap必须是RGBA格式!");
        return cv::Mat();
    }
}

jobject CvUtils::matToBitmap(JNIEnv* env, const cv::Mat mat) {
    jobject bitmap;
    AndroidBitmapInfo info;
    void* pixels = nullptr;

    int width = mat.cols;
    int height = mat.rows;
    int bitmapFormat = ANDROID_BITMAP_FORMAT_RGBA_8888;

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOfMethod = env->GetStaticMethodID(bitmapConfigClass, "valueOf",
                                                     "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass, valueOfMethod, configName);
    env->DeleteLocalRef(configName);
    bitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, width, height, bitmapConfig);

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        LOGE("获取Bitmap信息失败!");
        return nullptr;
    }
    if (info.format != bitmapFormat) {
        LOGE("Bitmap格式设置失败!");
        return nullptr;
    }
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        LOGE("锁定Bitmap像素失败!");
        return nullptr;
    }

    cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
    cv::cvtColor(mat, tmp, cv::COLOR_RGB2RGBA);

    AndroidBitmap_unlockPixels(env, bitmap);
    return bitmap;
}

std::vector<float> CvUtils::convertToFloat(const std::vector<float16_t> &input) {
    std::vector<float> output(input.size());

    for (size_t i = 0; i < input.size(); ++i) {
        output[i] = static_cast<float>(input[i]);
    }

    return output;
}
