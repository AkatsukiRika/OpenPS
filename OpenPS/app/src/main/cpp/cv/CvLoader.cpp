#include "CvLoader.h"
#include "CvUtils.h"
#include "../model/SkinModelProcessor.h"
#include <android/log.h>
#include <onnxruntime_cxx_api.h>

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

    try {
        auto preprocessResult = SkinModelProcessor::preprocess(originalMat);
        LOGI("皮肤模型预处理完成, 预处理结果大小: %zu", preprocessResult.size());

        auto modelData = std::vector<uint8_t>(modelBuffer, modelBuffer + modelSize);

        Ort::SessionOptions sessionOptions;
        sessionOptions.SetIntraOpNumThreads(2);
        sessionOptions.SetGraphOptimizationLevel(GraphOptimizationLevel::ORT_ENABLE_ALL);

        Ort::MemoryInfo memoryInfo = Ort::MemoryInfo::CreateCpu(OrtDeviceAllocator, OrtMemTypeCPU);
        Ort::Env env(ORT_LOGGING_LEVEL_WARNING, "skin-model-inference");
        Ort::Session session(env, modelData.data(), modelData.size(), sessionOptions);

        Ort::AllocatorWithDefaultOptions allocator;
        auto inputName = session.GetInputNameAllocated(0, allocator);
        auto outputName = session.GetOutputNameAllocated(0, allocator);

        std::vector<int64_t> inputShape = {1, 3, 512, 512};
        std::vector<int64_t> outputShape = {1, 19, 512, 512};

        // 修改：使用通用的 CreateTensor 函数，明确指定 FP16 类型
        Ort::Value inputTensor = Ort::Value::CreateTensor(
            memoryInfo,
            preprocessResult.data(),
            preprocessResult.size() * sizeof(float16_t),  // FP16 的大小
            inputShape.data(),
            inputShape.size(),
            ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16  // 明确指定 FP16 类型
        );

        auto inputTensorInfo = inputTensor.GetTensorTypeAndShapeInfo();
        auto inputDims = inputTensorInfo.GetShape();
        LOGI("输入Tensor类型: %d", inputTensorInfo.GetElementType());
        for (size_t i = 0; i < inputDims.size(); i++) {
            LOGI("输入Tensor维度: %zu, 大小: %lld", i, inputDims[i]);
        }

        std::array<const char*, 1> inputNames = {inputName.get()};
        std::array<const char*, 1> outputNames = {outputName.get()};

        auto outputTensors = session.Run(
            Ort::RunOptions{nullptr},
            inputNames.data(),
            &inputTensor,
            1,
            outputNames.data(),
            1
        );

        if (outputTensors.empty() || !outputTensors[0].IsTensor()) {
            LOGE("推理输出无效!");
            return 1;
        }

        const auto& outputTensor = outputTensors[0];
        auto outputTensorInfo = outputTensor.GetTensorTypeAndShapeInfo();
        auto actualOutputShape = outputTensorInfo.GetShape();

        LOGI("实际输出Tensor类型: %d", outputTensorInfo.GetElementType());
        for (size_t i = 0; i < actualOutputShape.size(); i++) {
            LOGI("实际输出Tensor维度: %zu, 大小: %lld", i, actualOutputShape[i]);
        }

        size_t outputSize = 19 * 512 * 512;
        if (actualOutputShape[1] != 19 || actualOutputShape[2] != 512 || actualOutputShape[3] != 512) {
            LOGE("输出维度不符合预期!");
            return 1;
        }

        // 获取 FP16 输出数据并转换为 FP32
        const float16_t* outputFp16Data = outputTensor.GetTensorData<float16_t>();
        std::vector<float16_t> outputValues(outputFp16Data, outputFp16Data + outputSize);
        std::vector<float> outputFp32Data = CvUtils::convertToFloat(outputValues);

        const int dims[] = {1, 19, 512, 512};
        cv::Mat modelOut(4, dims, CV_32F, outputFp32Data.data());

        auto postprocessResult = SkinModelProcessor::postprocess(modelOut, originalMat.rows, originalMat.cols);

        if (postprocessResult.size() != 4) {
            LOGE("皮肤模型后处理失败!");
            return 1;
        }

        parseResult = postprocessResult[0];
        skinMask = postprocessResult[1];

        return 0;
    } catch (const Ort::Exception& e) {
        LOGE("ONNX Runtime错误: %s", e.what());
        return 1;
    } catch (const std::exception& e) {
        LOGE("发生错误: %s", e.what());
        return 1;
    }
}

jobject CvLoader::getSkinMaskBitmap(JNIEnv *env) {
    if (skinMask.empty()) {
        LOGE("皮肤掩膜为空!");
        return nullptr;
    }

    auto skinMaskBitmap = CvUtils::matToBitmap(env, skinMask);
    return skinMaskBitmap;
}
