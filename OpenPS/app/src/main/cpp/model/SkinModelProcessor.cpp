#include "SkinModelProcessor.h"

// 定义 mean 和 std
const cv::Vec3f mean(0.485f, 0.456f, 0.406f);
const cv::Vec3f standard(0.229f, 0.224f, 0.225f);

std::vector<cv::hfloat> SkinModelProcessor::preprocess(const cv::Mat &src_img) {
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
            auto& pixel = img.at<cv::Vec3f>(i, j);
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

    // 将结果转换为 FP16
    std::vector<cv::hfloat> hfloat_result(1 * 3 * 512 * 512);
    cv::hal::cvt32f16f(result.data(), hfloat_result.data(), result.size());

    return hfloat_result;
}

std::vector<cv::Mat> SkinModelProcessor::postprocess(const cv::Mat &model_out, int src_img_height, int src_img_width) {
    // 首先确保输入数据被正确reshape为我们需要的维度
    cv::Mat input_reshaped;
    if (model_out.total() == 19 * 512 * 512) {
        input_reshaped = model_out.reshape(1, 19);  // 将数据重塑为 19 x (512*512)
    } else {
        throw std::runtime_error("Invalid input size for postprocess");
    }

    // 使用 C++ 的 std::memcpy 来安全地进行类型转换
    const size_t num_elements = model_out.total();
    std::vector<cv::hfloat> fp16_buffer(num_elements);
    std::memcpy(fp16_buffer.data(), input_reshaped.data, num_elements * sizeof(cv::hfloat));

    // 将 FP16 输出转换为 FP32
    cv::Mat model_out_fp32(19, 512 * 512, CV_32F);
    cv::hal::cvt16f32f(
        fp16_buffer.data(),
        model_out_fp32.ptr<float>(),
        num_elements
    );

    // 将数据分割为19个通道
    std::vector<cv::Mat> channels;
    for (int i = 0; i < 19; ++i) {
        // 创建一个指向当前行的Mat，然后reshape为512x512
        cv::Mat channel = model_out_fp32.row(i).reshape(1, 512);
        channels.push_back(channel.clone());  // 使用clone确保数据连续性
    }

    // 对每个像素找到最大值对应的通道
    cv::Mat parsing = cv::Mat::zeros(512, 512, CV_32S);
    for (int i = 0; i < 512; ++i) {
        for (int j = 0; j < 512; ++j) {
            float max_val = -FLT_MAX;
            int max_idx = -1;
            for (int c = 0; c < 19; ++c) {
                float val = channels[c].at<float>(i, j);
                if (val > max_val) {
                    max_val = val;
                    max_idx = c;
                }
            }
            parsing.at<int>(i, j) = max_idx;
        }
    }

    // 调整大小到原始图像尺寸
    cv::Mat resized_parsing;
    resize(parsing, resized_parsing, cv::Size(src_img_width, src_img_height), 0, 0, cv::INTER_NEAREST);

    // 创建各种掩码
    auto skin_mask = create_mask(resized_parsing, [](int val) { return val >= 1 && val <= 13; });
    auto teeth_mask = create_mask(resized_parsing, [](int val) { return val == 11; });
    auto eyes_mask = create_mask(resized_parsing, [](int val) { return val == 4 || val == 5 || val == 6; });

    return {resized_parsing, skin_mask, teeth_mask, eyes_mask};
}

cv::Mat SkinModelProcessor::create_mask(const cv::Mat &parsing_result, std::function<bool(int)> condition) {
    cv::Mat mask_image = cv::Mat::zeros(parsing_result.size(), CV_8UC3);
    for (int i = 0; i < parsing_result.rows; ++i) {
        for (int j = 0; j < parsing_result.cols; ++j) {
            if (condition(parsing_result.at<int>(i, j))) {
                mask_image.at<cv::Vec3b>(i, j) = cv::Vec3b(255, 255, 255);
            }
        }
    }
    return mask_image;
}
