#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "cv/CvLoader.h"
#include "cv/InpaintLoader.h"

CvLoader* cvLoader;

extern "C"
JNIEXPORT jint JNICALL
Java_com_akatsukirika_openps_interop_NativeLib_loadBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    if (cvLoader == nullptr) {
        cvLoader = new CvLoader();
    }
    return cvLoader->storeBitmap(env, bitmap);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_akatsukirika_openps_interop_NativeLib_releaseBitmap(JNIEnv *env, jobject thiz) {
    if (cvLoader != nullptr) {
        int result = cvLoader->releaseStoredBitmap();
        delete cvLoader;
        cvLoader = nullptr;
        return result;
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_akatsukirika_openps_interop_NativeLib_runSkinModelInference(JNIEnv *env, jobject thiz, jobject asset_manager, jstring model_file) {
    if (cvLoader == nullptr) {
        return 1;
    }
    AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);
    const char* model_file_path = env->GetStringUTFChars(model_file, 0);

    AAsset* asset = AAssetManager_open(mgr, model_file_path, AASSET_MODE_BUFFER);
    if (asset) {
        off_t length = AAsset_getLength(asset);
        char* buffer = new char[length];
        AAsset_read(asset, buffer, length);
        AAsset_close(asset);
        cvLoader->runSkinModelInference(buffer, length);
        delete[] buffer;
    }

    env->ReleaseStringUTFChars(model_file, model_file_path);
    return 0;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_akatsukirika_openps_interop_NativeLib_getSkinMaskBitmap(JNIEnv *env, jobject thiz) {
    if (cvLoader == nullptr) {
        return nullptr;
    }

    return cvLoader->getSkinMaskBitmap(env);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_akatsukirika_openps_interop_NativeLib_runInpaint(
    JNIEnv *env,
    jobject thiz,
    jobject image_bitmap,
    jobject mask_bitmap,
    jobject asset_manager,
    jstring model_file
) {
    AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);
    const char* model_file_path = env->GetStringUTFChars(model_file, 0);

    AAsset* asset = AAssetManager_open(mgr, model_file_path, AASSET_MODE_BUFFER);
    if (asset) {
        off_t length = AAsset_getLength(asset);
        std::unique_ptr<char[]> buffer(new char[length]);
        AAsset_read(asset, buffer.get(), length);
        AAsset_close(asset);

        auto inpaintLoader = std::make_unique<InpaintLoader>();
        return inpaintLoader->runInference(env, image_bitmap, mask_bitmap, buffer.get(), length);
    }

    return nullptr;
}