#include "CvLoader.h"
#include "CvUtils.h"
#include "../model/SkinModelProcessor.h"
#include <android/log.h>
#include <tensorflow/lite/c/c_api.h>

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

    // 加载TFLite模型
    TfLiteModel* model = TfLiteModelCreate(modelBuffer, modelSize);
    if (model == nullptr) {
        LOGE("加载TFLite模型失败!");
        return 1;
    }

    // 创建解释器选项
    TfLiteInterpreterOptions* options = TfLiteInterpreterOptionsCreate();
    TfLiteInterpreterOptionsSetNumThreads(options, 2);

    // 创建解释器
    TfLiteInterpreter* interpreter = TfLiteInterpreterCreate(model, options);
    if (interpreter == nullptr) {
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("创建解释器失败!");
        return 1;
    }

    // 分配Tensor Buffers
    if (TfLiteInterpreterAllocateTensors(interpreter) != kTfLiteOk) {
        TfLiteInterpreterDelete(interpreter);
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("分配Tensor Buffers失败!");
        return 1;
    }

    // 获取输入Tensor
    TfLiteTensor* inputTensor = TfLiteInterpreterGetInputTensor(interpreter, 0);
    if (inputTensor == nullptr) {
        TfLiteInterpreterDelete(interpreter);
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("获取输入Tensor失败!");
        return 1;
    }

    // 获取输入类型和维度
    TfLiteType tensorType = TfLiteTensorType(inputTensor);
    int32_t tensorDims = TfLiteTensorNumDims(inputTensor);
    LOGI("输入Tensor类型: %d", tensorType);
    for (int32_t i = 0; i < tensorDims; i++) {
        int32_t tensorDim = TfLiteTensorDim(inputTensor, i);
        LOGI("输入Tensor维度: %d, 大小: %d", i, tensorDim);
    }

    // 准备输入数据
    if (TfLiteTensorCopyFromBuffer(inputTensor, preprocessResult.data(), preprocessResult.size() * sizeof(float_t)) != kTfLiteOk) {
        TfLiteInterpreterDelete(interpreter);
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("准备输入数据失败!");
        return 1;
    }

    // 运行推理
    if (TfLiteInterpreterInvoke(interpreter) != kTfLiteOk) {
        TfLiteInterpreterDelete(interpreter);
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("运行推理失败!");
        return 1;
    }

    // 获取输出Tensor
    const TfLiteTensor* outputTensor = TfLiteInterpreterGetOutputTensor(interpreter, 0);
    if (!outputTensor) {
        TfLiteInterpreterDelete(interpreter);
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("获取输出Tensor失败!");
        return 1;
    }

    // 获取输出类型和维度
    tensorType = TfLiteTensorType(outputTensor);
    tensorDims = TfLiteTensorNumDims(outputTensor);
    LOGI("输出Tensor类型: %d", tensorType);
    for (int32_t i = 0; i < tensorDims; i++) {
        int32_t tensorDim = TfLiteTensorDim(outputTensor, i);
        LOGI("输出Tensor维度: %d, 大小: %d", i, tensorDim);
    }

    // 获取输出数据
    std::vector<float16_t> outputData(TfLiteTensorByteSize(outputTensor) / sizeof(float16_t));
    if (TfLiteTensorCopyToBuffer(outputTensor, outputData.data(), outputData.size() * sizeof(float16_t)) != kTfLiteOk) {
        TfLiteInterpreterDelete(interpreter);
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("获取输出数据失败!");
        return 1;
    }

    // 后处理
    cv::Mat outputMat(1, 19 * 512 * 512, CV_32F, outputData.data());
    outputMat = outputMat.reshape(1, {1, 19, 512, 512});
    auto postprocessResult = SkinModelProcessor::postprocess(outputMat, originalMat.rows, originalMat.cols);
    if (postprocessResult.size() != 4) {
        TfLiteInterpreterDelete(interpreter);
        TfLiteModelDelete(model);
        TfLiteInterpreterOptionsDelete(options);
        LOGE("皮肤模型后处理失败!");
        return 1;
    }

    // 设置后处理结果
    parseResult = postprocessResult[0];
    skinMask = postprocessResult[1];

    // 清理
    TfLiteInterpreterDelete(interpreter);
    TfLiteModelDelete(model);
    TfLiteInterpreterOptionsDelete(options);

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
