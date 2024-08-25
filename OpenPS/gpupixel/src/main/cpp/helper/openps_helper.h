/*
 * OpenPSHelper
 *
 * Created by Rika on 2024/8/7.
 * CopyRight © 2024 Rika. All rights reserved.
 */

#pragma once

#include "gpupixel_macros.h"
#include "gpupixel.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API OpenPSHelper {
public:
  OpenPSHelper();

  ~OpenPSHelper();

  void initWithImage(int width, int height, int channelCount, const unsigned char* pixels);

  void onTargetViewSizeChanged(int width, int height);

  void getTargetViewInfo(float *info);

  void buildBasicRenderPipeline();

  void buildRealRenderPipeline();

  void requestRender();

  void setLandmarkCallback(FaceDetectorCallback callback);

  void setRawOutputCallback(RawOutputCallback callback);

  void setSmoothLevel(float level);

  void setWhiteLevel(float level);

  void setLipstickLevel(float level);

  void setBlusherLevel(float level);

  void setEyeZoomLevel(float level);

  void setFaceSlimLevel(float level);

  void onCompareBegin();

  void onCompareEnd();

  void setScaleFactor(float scale);

  void setTranslateDistance(float x, float y);

  void resetMVPMatrix();

  float getDistanceX();

  float getDistanceY();

private:
  std::shared_ptr<SourceImage> gpuSourceImage;
  std::shared_ptr<LipstickFilter> lipstickFilter;
  std::shared_ptr<BlusherFilter> blusherFilter;
  std::shared_ptr<FaceReshapeFilter> faceReshapeFilter;
  std::shared_ptr<BeautyFaceFilter> beautyFaceFilter;
  std::shared_ptr<TargetView> targetView;
  std::shared_ptr<TargetRawDataOutput> targetRawDataOutput;

  float smoothLevel = 0;
  float whiteLevel = 0;
  float lipstickLevel = 0;
  float blusherLevel = 0;
  float eyeZoomLevel = 0;
  float faceSlimLevel = 0;

  float scaleFactor = 1;
  float distanceX = 0;
  float distanceY = 0;
  float totalNormalizedDistanceX = 0;
  float totalNormalizedDistanceY = 0;
  bool needResetMatrix = false;

  Matrix4 modelMatrix;
  void handleMVPMatrix();
};

NS_GPUPIXEL_END
