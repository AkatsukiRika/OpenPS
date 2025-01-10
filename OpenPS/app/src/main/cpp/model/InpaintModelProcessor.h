#ifndef OPENPS_INPAINTMODELPROCESSOR_H
#define OPENPS_INPAINTMODELPROCESSOR_H

#include <opencv2/opencv.hpp>

class InpaintModelProcessor {
public:
    /**
     * @param image RGBA
     * @param mask RGBA
     * @param modelBuffer 模型二进制数据
     * @param modelSize 模型大小
     * @return RGB
     */
    static cv::Mat inpaint(const cv::Mat& image, const cv::Mat& mask, const char* modelBuffer, off_t modelSize);

private:
    static void preprocessImage(const cv::Mat& input, std::vector<uint8_t>& output);

    static void preprocessMask(const cv::Mat& input, std::vector<uint8_t>& output);

    static cv::Mat postprocessResult(const uint8_t* data, std::vector<int> dims);
};

#endif //OPENPS_INPAINTMODELPROCESSOR_H
