#ifndef OPENPS_SKINMODELPROCESSOR_H
#define OPENPS_SKINMODELPROCESSOR_H

#include <opencv2/opencv.hpp>
#include <opencv2/core/hal/hal.hpp>
#include <arm_fp16.h>

class SkinModelProcessor {
public:
    static std::vector<float_t> preprocess(const cv::Mat& src_img);
    static std::vector<cv::Mat> postprocess(const cv::Mat& model_out, int src_img_height, int src_img_width);

private:
    static cv::Mat create_mask(const cv::Mat& parsing_result, std::function<bool(int)> condition);
};

#endif //OPENPS_SKINMODELPROCESSOR_H
