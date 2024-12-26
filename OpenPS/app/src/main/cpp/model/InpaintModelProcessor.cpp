#include "InpaintModelProcessor.h"
#include <onnxruntime_cxx_api.h>

cv::Mat InpaintModelProcessor::inpaint(const cv::Mat &image, const cv::Mat &mask, const char *modelBuffer, off_t modelSize) {
    // 创建 ONNX Runtime 环境和会话
    Ort::Env env(ORT_LOGGING_LEVEL_WARNING, "inpaint_model");
    Ort::SessionOptions session_options;
    std::vector<uint8_t> model_data(modelBuffer, modelBuffer + modelSize);
    Ort::Session session(env, model_data.data(), modelSize, session_options);

    // image由RGBA转换为RGB
    cv::Mat image_rgb;
    cv::cvtColor(image, image_rgb, cv::COLOR_RGBA2RGB);

    // mask由RGBA转换为灰度图
    cv::Mat mask_gray;
    cv::cvtColor(mask, mask_gray, cv::COLOR_RGBA2GRAY);

    // 准备输入数据
    std::vector<uint8_t> image_tensor;
    std::vector<uint8_t> mask_tensor;
    preprocessImage(image_rgb, image_tensor);
    preprocessMask(mask_gray, mask_tensor);

    // 设置输入维度
    const int64_t input_dims[] = {1, 3, image_rgb.rows, image_rgb.cols};
    const int64_t mask_dims[] = {1, 1, mask_gray.rows, mask_gray.cols};

    // 创建输入张量
    auto memory_info = Ort::MemoryInfo::CreateCpu(OrtArenaAllocator, OrtMemTypeDefault);
    std::vector<Ort::Value> input_tensors;
    input_tensors.push_back(Ort::Value::CreateTensor<uint8_t>(
        memory_info, image_tensor.data(), image_tensor.size(), input_dims, 4));
    input_tensors.push_back(Ort::Value::CreateTensor<uint8_t>(
        memory_info, mask_tensor.data(), mask_tensor.size(), mask_dims, 4));

    // 设置输入输出名称
    std::vector<const char*> input_names = {"image", "mask"};
    std::vector<const char*> output_names = {"result"};

    // 运行推理
    auto output_tensors = session.Run(Ort::RunOptions{},
                                      input_names.data(), input_tensors.data(), input_names.size(),
                                      output_names.data(), output_names.size());

    // 处理输出结果
    uint8_t* output_data = output_tensors[0].GetTensorMutableData<uint8_t>();
    auto output_dims = output_tensors[0].GetTensorTypeAndShapeInfo().GetShape();

    // 后处理并返回结果
    return postprocessResult(output_data, output_dims);
}

void InpaintModelProcessor::preprocessImage(const cv::Mat &input, std::vector<uint8_t> &output) {
    output.resize(input.rows * input.cols * 3);
    uint8_t* ptr = output.data();

    for (int c = 0; c < 3; c++) {
        for (int h = 0; h < input.rows; h++) {
            for (int w = 0; w < input.cols; w++) {
                ptr[c * input.rows * input.cols + h * input.cols + w] = input.at<cv::Vec3b>(h, w)[c];
            }
        }
    }
}

void InpaintModelProcessor::preprocessMask(const cv::Mat &input, std::vector<uint8_t> &output) {
    output.resize(input.rows * input.cols);
    uint8_t* ptr = output.data();

    for (int h = 0; h < input.rows; h++) {
        for (int w = 0; w < input.cols; w++) {
            ptr[h * input.cols + w] = input.at<uint8_t>(h, w);
        }
    }
}

cv::Mat InpaintModelProcessor::postprocessResult(const uint8_t *data, std::vector<int64_t> &dims) {
    int height = dims[2];
    int width = dims[3];
    cv::Mat result(height, width, CV_8UC3);

    for (int h = 0; h < height; h++) {
        for (int w = 0; w < width; w++) {
            for (int c = 0; c < 3; c++) {
                result.at<cv::Vec3b>(h, w)[c] = data[c * height * width + h * width + w];
            }
        }
    }

    return result;
}
