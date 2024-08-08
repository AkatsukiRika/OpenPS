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

#endif
