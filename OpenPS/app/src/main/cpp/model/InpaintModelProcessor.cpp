#include "InpaintModelProcessor.h"
#include <onnxruntime_cxx_api.h>
#include <MNN/Interpreter.hpp>
#include <MNN/ImageProcess.hpp>

cv::Mat InpaintModelProcessor::inpaint(const cv::Mat &image, const cv::Mat &mask, const char *modelBuffer, off_t modelSize) {
    // 创建 MNN 解释器
    std::unique_ptr<MNN::Interpreter> interpreter(MNN::Interpreter::createFromBuffer(modelBuffer, modelSize));
    MNN::ScheduleConfig config;
    config.type = MNN_FORWARD_CPU;
    config.numThread = 4;
    MNN::BackendConfig backendConfig;
    backendConfig.precision = MNN::BackendConfig::Precision_Low;
    config.backendConfig = &backendConfig;
    auto session = interpreter->createSession(config);

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

    // 获取输入张量
    auto input_image = interpreter->getSessionInput(session, "image");
    auto input_mask = interpreter->getSessionInput(session, "mask");

    // 调整输入张量尺寸
    std::vector<int> imageDims = {1, 3, image_rgb.rows, image_rgb.cols};
    std::vector<int> maskDims = {1, 1, mask_gray.rows, mask_gray.cols};

    interpreter->resizeTensor(input_image, imageDims);
    interpreter->resizeTensor(input_mask, maskDims);

    // 调整尺寸后重新创建Session
    interpreter->resizeSession(session);

    // 创建输入张量
    std::unique_ptr<MNN::Tensor> imageTensor(MNN::Tensor::createHostTensorFromDevice(input_image));
    std::unique_ptr<MNN::Tensor> maskTensor(MNN::Tensor::createHostTensorFromDevice(input_mask));

    // 复制数据到输入张量
    memcpy(imageTensor->host<uint8_t>(), image_tensor.data(), image_tensor.size());
    memcpy(maskTensor->host<uint8_t>(), mask_tensor.data(), mask_tensor.size());

    input_image->copyFromHostTensor(imageTensor.get());
    input_mask->copyFromHostTensor(maskTensor.get());

    // 运行推理
    interpreter->runSession(session);

    // 获取输出张量
    auto output = interpreter->getSessionOutput(session, "result");
    std::unique_ptr<MNN::Tensor> outputTensor(MNN::Tensor::create(
        output->shape(),
        halide_type_of<uint8_t>()
    ));
    output->copyToHostTensor(outputTensor.get());

    // 后处理并返回结果
    return postprocessResult(outputTensor->host<uint8_t>(), outputTensor->shape());
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

cv::Mat InpaintModelProcessor::postprocessResult(const uint8_t *data, std::vector<int> dims) {
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
