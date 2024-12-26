#ifndef OPENPS_INPAINTLOADER_H
#define OPENPS_INPAINTLOADER_H

#include <jni.h>
#include <opencv2/opencv.hpp>

class InpaintLoader {
public:
    int storeBitmaps(JNIEnv* env, jobject imageBitmap, jobject maskBitmap);
    void releaseStoredBitmaps();

private:
    cv::Mat image;
    cv::Mat mask;
};

#endif //OPENPS_INPAINTLOADER_H
