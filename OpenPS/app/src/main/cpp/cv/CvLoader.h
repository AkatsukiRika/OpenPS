#ifndef OPENPS_CVLOADER_H
#define OPENPS_CVLOADER_H

#include <jni.h>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

class CvLoader {
public:
    int storeBitmap(JNIEnv* env, jobject bitmap);
    int releaseStoredBitmap();
    int runSkinModelInference(const char* modelBuffer, off_t modelSize);
    jobject getSkinMaskBitmap(JNIEnv* env);

private:
    AndroidBitmapInfo bitmapInfo;    // Bitmap 元数据，如宽度、高度等
    void* pixels = nullptr;          // Bitmap 像素数据
    cv::Mat originalMat;             // Bitmap 转换后的 Mat 对象


    cv::Mat parseResult;            // 皮肤分割模型输出结果
    cv::Mat skinMask;               // 皮肤区域的掩膜
};

#endif //OPENPS_CVLOADER_H
