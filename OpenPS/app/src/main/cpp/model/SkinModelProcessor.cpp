#include "SkinModelProcessor.h"

// 定义 mean 和 std
const cv::Vec3f mean(0.485f, 0.456f, 0.406f);
const cv::Vec3f standard(0.229f, 0.224f, 0.225f);

std::vector<float_t> SkinModelProcessor::preprocess(const cv::Mat &src_img) {
    // 将 RGBA 转换为 RGB
    cv::Mat img;
    cvtColor(src_img, img, cv::COLOR_RGBA2RGB);

    // 调整图像大小为 512x512
    resize(img, img, cv::Size(512, 512), 0, 0, cv::INTER_LINEAR);

    // 转换为 float 类型并归一化
    img.convertTo(img, CV_32F, 1.0 / 255.0);

    // 减去均值并除以标准差
    for (int i = 0; i < img.rows; ++i) {
        for (int j = 0; j < img.cols; ++j) {
            cv::Vec3f& pixel = img.at<cv::Vec3f>(i, j);
            pixel[0] = (pixel[0] - mean[0]) / standard[0];
            pixel[1] = (pixel[1] - mean[1]) / standard[1];
            pixel[2] = (pixel[2] - mean[2]) / standard[2];
        }
    }

    // 变换维度为 (1, 3, 512, 512)
    std::vector<float_t> result(1 * 3 * 512 * 512);
    int idx = 0;
    for (int c = 0; c < 3; ++c) {
        for (int i = 0; i < 512; ++i) {
            for (int j = 0; j < 512; ++j) {
                result[idx++] = img.at<cv::Vec3f>(i, j)[c];
            }
        }
    }

    return result;
}
