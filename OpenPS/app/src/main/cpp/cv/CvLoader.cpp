#include "CvLoader.h"
#include "CvUtils.h"
#include "../model/SkinModelProcessor.h"
#include <android/log.h>
#include <MNN/Interpreter.hpp>
#include <MNN/Tensor.hpp>
#include <MNN/ImageProcess.hpp>

#define LOG_TAG "xuanTest"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

int CvLoader::storeBitmap(JNIEnv *env, jobject bitmap) {
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        LOGE("获取Bitmap信息错误!");
        return 1;
    }
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        LOGE("锁定Bitmap错误!");
        return 1;
    }
    size_t bufferSize = bitmapInfo.height * bitmapInfo.stride;
    void* pixelBuffer = malloc(bufferSize);
    if (pixelBuffer != nullptr) {
        memcpy(pixelBuffer, pixels, bufferSize);
        pixels = pixelBuffer;
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    LOGI("Bitmap加载成功");
    originalMat = CvUtils::bitmapToMat(bitmapInfo, pixels);
    if (originalMat.empty()) {
        LOGE("Bitmap转Mat时出现错误!");
        return 1;
    } else {
        LOGI("Bitmap转Mat成功");
        return 0;
    }
}

int CvLoader::releaseStoredBitmap() {
    if (pixels != nullptr) {
        free(pixels);
        pixels = nullptr;
        LOGI("Bitmap释放完成");
    }
    return 0;
}

int CvLoader::runSkinModelInference(const char *modelBuffer, off_t modelSize) {
    if (pixels == nullptr || originalMat.empty()) {
        LOGE("Bitmap未加载!");
        return 1;
    }
    auto preprocessResult = SkinModelProcessor::preprocess(originalMat);
    LOGI("皮肤模型预处理完成, 预处理结果大小: %zu", preprocessResult.size());

    // 创建MNN解释器
    std::shared_ptr<MNN::Interpreter> interpreter(
        MNN::Interpreter::createFromBuffer(modelBuffer, modelSize)
    );
    if (!interpreter) {
        LOGE("加载MNN模型失败!");
        return 1;
    }

    // 创建会话配置
    MNN::ScheduleConfig config;
    config.numThread = 4;

    // 创建会话
    MNN::Session* session = interpreter->createSession(config);
    if (!session) {
        LOGE("创建会话失败!");
        return 1;
    }

    // 获取输入Tensor
    MNN::Tensor* inputTensor = interpreter->getSessionInput(session, nullptr);
    if (!inputTensor) {
        LOGE("获取输入Tensor失败!");
        return 1;
    }

    // 获取输入Tensor的维度信息
    auto inputDims = inputTensor->shape();
    LOGI("输入Tensor维度: ");
    for (int i = 0; i < inputDims.size(); ++i) {
        LOGI("维度 %d: %d", i, inputDims[i]);
    }

    // 准备输入数据
    std::unique_ptr<MNN::Tensor> inputTensorUser(
        MNN::Tensor::create<float>(inputDims, nullptr)
    );
    memcpy(inputTensorUser->host<float>(), preprocessResult.data(), preprocessResult.size() * sizeof(float));
    inputTensor->copyFromHostTensor(inputTensorUser.get());

    // 运行推理
    interpreter->runSession(session);

    // 获取输出Tensor
    MNN::Tensor* outputTensor = interpreter->getSessionOutput(session, nullptr);
    if (!outputTensor) {
        LOGE("获取输出Tensor失败!");
        return 1;
    }

    // 获取输出Tensor的维度信息
    auto outputDims = outputTensor->shape();
    LOGI("输出Tensor维度: ");
    for (int i = 0; i < outputDims.size(); ++i) {
        LOGI("维度 %d: %d", i, outputDims[i]);
    }

    // 获取输出数据
    std::unique_ptr<MNN::Tensor> outputTensorUser(
        MNN::Tensor::create<float>(outputDims, nullptr)
    );
    outputTensor->copyToHostTensor(outputTensorUser.get());
    std::vector<float> outputData(
        outputTensorUser->host<float>(),
        outputTensorUser->host<float>() + outputTensorUser->elementSize()
    );

    // 后处理
    cv::Mat outputMat(1, 19 * 512 * 512, CV_32F, outputData.data());
    outputMat = outputMat.reshape(1, {1, 19, 512, 512});
    auto postprocessResult = SkinModelProcessor::postprocess(outputMat, originalMat.rows, originalMat.cols);
    if (postprocessResult.size() != 4) {
        LOGE("皮肤模型后处理失败!");
        return 1;
    }

    // 设置后处理结果
    parseResult = postprocessResult[0];
    skinMask = postprocessResult[1];

    // 清理
    interpreter->releaseModel();
    interpreter->releaseSession(session);

    return 0;
}

jobject CvLoader::getSkinMaskBitmap(JNIEnv *env) {
    if (skinMask.empty()) {
        LOGE("皮肤掩膜为空!");
        return nullptr;
    }

    auto skinMaskBitmap = CvUtils::matToBitmap(env, skinMask);
    return skinMaskBitmap;
}
