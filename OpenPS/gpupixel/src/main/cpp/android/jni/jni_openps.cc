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
                                                     jobject bitmap) {
  AndroidBitmapInfo info;
  void *pixels;
  if ((AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
    return;
  }
  if ((AndroidBitmap_lockPixels(env, bitmap, &pixels)) >= 0) {
    if (openPSHelper) {
      openPSHelper->initWithImage(width, height, channelCount, (const unsigned char *) pixels);
    }
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
Java_com_pixpark_gpupixel_OpenPS_nativeSetSmoothLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setSmoothLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetWhiteLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setWhiteLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetLipstickLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setLipstickLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetBlusherLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setBlusherLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetEyeZoomLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setEyeZoomLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetFaceSlimLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setFaceSlimLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetContrastLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setContrastLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetExposureLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setExposureLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetSaturationLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setSaturationLevel(level);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetSharpenLevel(JNIEnv *env, jobject thiz, jfloat level) {
  if (openPSHelper) {
    openPSHelper->setSharpenLevel(level);
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
Java_com_pixpark_gpupixel_OpenPS_nativeSetScaleFactor(JNIEnv *env, jobject thiz, jfloat scale) {
  if (openPSHelper) {
    openPSHelper->setScaleFactor(scale);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeSetTranslateDistance(JNIEnv *env, jobject thiz, jfloat x, jfloat y) {
  if (openPSHelper) {
    openPSHelper->setTranslateDistance(x, y);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeResetMVPMatrix(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    openPSHelper->resetMVPMatrix();
  }
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeGetTranslateDistanceX(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    return openPSHelper->getDistanceX();
  }
  return 0;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_pixpark_gpupixel_OpenPS_nativeGetTranslateDistanceY(JNIEnv *env, jobject thiz) {
  if (openPSHelper) {
    return openPSHelper->getDistanceY();
  }
  return 0;
}

#endif
