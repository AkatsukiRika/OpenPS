#if PLATFORM == PLATFORM_ANDROID

#include "gpupixel_context.h"
#include "openps_helper.h"
#include <android/bitmap.h>

USING_NS_GPUPIXEL

std::unique_ptr<OpenPSHelper> openPSHelper;

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeInit(JNIEnv *env, jobject thiz) {
  openPSHelper = std::make_unique<OpenPSHelper>();
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeInitWithImage(JNIEnv *env, jobject thiz,
                                                     jint width, jint height,
                                                     jint channelCount,
                                                     jobject bitmap,
                                                     jstring filename) {
  AndroidBitmapInfo info;
  void *pixels;
  if ((AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
    return;
  }
  const char* filenameStr = nullptr;
  if (filename != nullptr) {
    filenameStr = env->GetStringUTFChars(filename, nullptr);
  }
  if ((AndroidBitmap_lockPixels(env, bitmap, &pixels)) >= 0) {
    if (openPSHelper) {
      openPSHelper->initWithImage(width, height, channelCount, (const unsigned char *) pixels, filenameStr);
    }
  }
  if (filenameStr != nullptr) {
    env->ReleaseStringUTFChars(filename, filenameStr);
  }
  AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeUpdateTransform(JNIEnv *env, jobject thiz, jboolean mirrored, jboolean flipped) {
  if (openPSHelper) {
    openPSHelper->updateTransform(mirrored, flipped);
  }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeChangeImage(JNIEnv *env, jobject thiz,
                                                   jint width, jint height,
                                                   jint channel_count,
                                                   jobject bitmap,
                                                   jstring filename) {
  AndroidBitmapInfo info;
  void *pixels;
  if ((AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
    return;
  }
  const char* filenameStr = nullptr;
  if (filename != nullptr) {
    filenameStr = env->GetStringUTFChars(filename, nullptr);
  }
  if ((AndroidBitmap_lockPixels(env, bitmap, &pixels)) >= 0) {
    if (openPSHelper) {
      openPSHelper->changeImage(width, height, channel_count, (const unsigned char *) pixels, filenameStr);
    }
  }
  if (filenameStr != nullptr) {
    env->ReleaseStringUTFChars(filename, filenameStr);
  }
  AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeDestroy(JNIEnv *env, jobject thiz) {
  openPSHelper.reset();
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeTargetViewSizeChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
  if (openPSHelper) {
    openPSHelper->onTargetViewSizeChanged(width, height);
  }
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeTargetViewGetInfo(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    float info[4];
    openPSHelper->getTargetViewInfo(info);
    jfloatArray jinfo = env->NewFloatArray(4);
    env->SetFloatArrayRegion(jinfo, 0, 4, info);
    return jinfo;
  }
  return nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeBuildBasicRenderPipeline(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    openPSHelper->buildBasicRenderPipeline();
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeBuildRealRenderPipeline(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    openPSHelper->buildRealRenderPipeline();
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeBuildNoFaceRenderPipeline(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    openPSHelper->buildNoFaceRenderPipeline();
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeRequestRender(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    openPSHelper->requestRender();
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetLandmarkCallback(JNIEnv *env, jobject thiz, jobject receiver) {
  if (openPSHelper) {
    jobject globalReceiver = env->NewGlobalRef(receiver);
    openPSHelper->setLandmarkCallback([env, globalReceiver](std::vector<float> landmarks, std::vector<float> rect) {
      jclass receiverClass = env->GetObjectClass(globalReceiver);
      jmethodID methodId = env->GetMethodID(receiverClass, "onLandmarkDetected", "([F[F)V");

      jfloatArray arr = env->NewFloatArray(landmarks.size());
      env->SetFloatArrayRegion( arr, 0, landmarks.size(), landmarks.data());
      jfloatArray rectArr = env->NewFloatArray(rect.size());
      env->SetFloatArrayRegion(rectArr, 0, rect.size(), rect.data());
      env->CallVoidMethod(globalReceiver, methodId, arr, rectArr);

      env->DeleteLocalRef(arr);
      env->DeleteLocalRef(rectArr);
    });
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetRawOutputCallback(JNIEnv *env, jobject thiz, jobject receiver) {
  if (openPSHelper) {
    jobject globalReceiver = env->NewGlobalRef(receiver);
    openPSHelper->setRawOutputCallback([env, globalReceiver](const uint8_t* data, int width, int height, int64_t ts) {
      jclass receiverClass = env->GetObjectClass(globalReceiver);
      jmethodID methodId = env->GetMethodID(receiverClass, "onResultPixels", "([BIIJ)V");

      size_t length = width * height * 4;
      jbyteArray byteArray = env->NewByteArray(length);
      env->SetByteArrayRegion(byteArray, 0, length, reinterpret_cast<const jbyte*>(data));
      env->CallVoidMethod(globalReceiver, methodId, byteArray, width, height, ts);

      env->DeleteLocalRef(byteArray);
    });
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetSmoothLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setSmoothLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetWhiteLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setWhiteLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetLipstickLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setLipstickLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetBlusherLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setBlusherLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetEyeZoomLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setEyeZoomLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetFaceSlimLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setFaceSlimLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetContrastLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setContrastLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetExposureLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setExposureLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetSaturationLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setSaturationLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetSharpenLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setSharpenLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetBrightnessLevel(JNIEnv *env, jobject thiz, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->setBrightnessLevel(level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeApplyCustomFilter(JNIEnv *env, jobject thiz, jint type, jfloat level, jboolean addRecord) {
  if (openPSHelper) {
    openPSHelper->applyCustomFilter(type, level, addRecord);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeCompareBegin(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    openPSHelper->onCompareBegin();
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeCompareEnd(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    openPSHelper->onCompareEnd();
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeUpdateMVPMatrix(JNIEnv *env, jobject thiz, jfloatArray matrix) {
  if (openPSHelper) {
    jfloat *matrixData = env->GetFloatArrayElements(matrix, nullptr);
    openPSHelper->updateMVPMatrix(matrixData);
    env->ReleaseFloatArrayElements(matrix, matrixData, 0);
  }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeCanUndo(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    return openPSHelper->canUndo();
  }
  return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeCanRedo(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    return openPSHelper->canRedo();
  }
  return false;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeUndo(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    auto record = openPSHelper->undo();
    if (!record) {
      return nullptr;
    }
    jclass kotlinClass = env->FindClass("com/pixpark/gpupixel/model/OpenPSRecord");
    jmethodID constructor = env->GetMethodID(kotlinClass, "<init>", "(FFFFFFFFFFFIFZZ)V");
    jobject kotlinObject = env->NewObject(kotlinClass, constructor, record->smoothLevel, record->whiteLevel,
                                          record->lipstickLevel, record->blusherLevel, record->eyeZoomLevel,
                                          record->faceSlimLevel, record->contrastLevel, record->exposureLevel,
                                          record->saturationLevel, record->sharpnessLevel, record->brightnessLevel,
                                          record->customFilterType, record->customFilterIntensity, record->isMirrored, record->isFlipped);
    return kotlinObject;
  }
  return nullptr;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeRedo(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    auto record = openPSHelper->redo();
    if (!record) {
      return nullptr;
    }
    jclass kotlinClass = env->FindClass("com/pixpark/gpupixel/model/OpenPSRecord");
    jmethodID constructor = env->GetMethodID(kotlinClass, "<init>", "(FFFFFFFFFFFIFZZ)V");
    jobject kotlinObject = env->NewObject(kotlinClass, constructor, record->smoothLevel, record->whiteLevel,
                                          record->lipstickLevel, record->blusherLevel, record->eyeZoomLevel,
                                          record->faceSlimLevel, record->contrastLevel, record->exposureLevel,
                                          record->saturationLevel, record->sharpnessLevel, record->brightnessLevel,
                                          record->customFilterType, record->customFilterIntensity, record->isMirrored, record->isFlipped);
    return kotlinObject;
  }
  return nullptr;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeGetCurrentImageFileName(JNIEnv* env, jobject thiz) {
  if (openPSHelper) {
    return env->NewStringUTF(openPSHelper->getCurrentImageFileName().c_str());
  }
  return nullptr;
}

#endif
