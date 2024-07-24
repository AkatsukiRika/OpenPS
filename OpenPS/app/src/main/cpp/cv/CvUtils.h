#ifndef OPENPS_CVUTILS_H
#define OPENPS_CVUTILS_H

#include <opencv2/opencv.hpp>
#include <android/bitmap.h>

class CvUtils {
public:
    static cv::Mat bitmapToMat(const AndroidBitmapInfo bitmapInfo, const void* pixels);
    static jobject matToBitmap(JNIEnv* env, const cv::Mat mat);
};

#endif //OPENPS_CVUTILS_H
