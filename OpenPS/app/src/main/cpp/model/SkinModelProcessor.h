#ifndef OPENPS_SKINMODELPROCESSOR_H
#define OPENPS_SKINMODELPROCESSOR_H

#include <opencv2/opencv.hpp>

class SkinModelProcessor {
public:
    static std::vector<float_t> preprocess(const cv::Mat& src_img);
};

#endif //OPENPS_SKINMODELPROCESSOR_H
