#include "InpaintModelProcessor.h"
#include <onnxruntime_cxx_api.h>
#include <MNN/Interpreter.hpp>
#include <MNN/ImageProcess.hpp>
#include <MNN/expr/Module.hpp>
#include <MNN/expr/Executor.hpp>
#include <MNN/expr/ExprCreator.hpp>

cv::Mat InpaintModelProcessor::inpaint(const cv::Mat &image, const cv::Mat &mask, const char *modelBuffer, off_t modelSize) {
    // 创建 RuntimeManager
    MNN::ScheduleConfig sConfig;
    sConfig.type = MNN_FORWARD_CPU;
    sConfig.numThread = 4;

    std::shared_ptr<MNN::Express::Executor::RuntimeManager> runtimeManager(
        MNN::Express::Executor::RuntimeManager::createRuntimeManager(sConfig)
    );

    // 加载模型
    std::shared_ptr<MNN::Express::Module> expressModule(
        MNN::Express::Module::load(
            {"image", "mask"},
            {"result"},
            reinterpret_cast<const uint8_t*>(modelBuffer),
            modelSize,
            runtimeManager
        )
    );

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
    auto input_image = MNN::Express::_Input(
        {1, 3, image_rgb.rows, image_rgb.cols},
        MNN::Express::NCHW,
        halide_type_of<uint8_t>()
    );
    auto input_mask = MNN::Express::_Input(
        {1, 1, mask_gray.rows, mask_gray.cols},
        MNN::Express::NCHW,
        halide_type_of<uint8_t>()
    );

    // 复制数据到输入张量
    memcpy(input_image->writeMap<uint8_t>(), image_tensor.data(), image_tensor.size());
    memcpy(input_mask->writeMap<uint8_t>(), mask_tensor.data(), mask_tensor.size());

    // 运行推理
    auto outputs = expressModule->onForward({input_image, input_mask});

    // 获取输出张量
    auto output = outputs[0];
    auto outputPtr = output->readMap<uint8_t>();
    auto outputShape = output->getInfo()->dim;

    // 后处理并返回结果
    return postprocessResult(outputPtr, outputShape);
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
