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
