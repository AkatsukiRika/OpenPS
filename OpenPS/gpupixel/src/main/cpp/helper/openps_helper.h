/*
 * OpenPSHelper
 *
 * Created by Rika on 2024/8/7.
 * CopyRight © 2024 Rika. All rights reserved.
 */

#pragma once

#include "gpupixel_macros.h"
#include "gpupixel.h"
#include "undo_redo_helper.h"
#include "openps_record.h"

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

  void buildNoFaceRenderPipeline();

  void requestRender();

  void setLandmarkCallback(FaceDetectorCallback callback);

  void setRawOutputCallback(RawOutputCallback callback);

  void setSmoothLevel(float level, bool addRecord = false);

  void setWhiteLevel(float level, bool addRecord = false);

  void setLipstickLevel(float level, bool addRecord = false);

  void setBlusherLevel(float level, bool addRecord = false);

  void setEyeZoomLevel(float level, bool addRecord = false);

  void setFaceSlimLevel(float level, bool addRecord = false);

  void setContrastLevel(float level, bool addRecord = false);

  void setExposureLevel(float level, bool addRecord = false);

  void setSaturationLevel(float level, bool addRecord = false);

  void setSharpenLevel(float level, bool addRecord = false);

  void setBrightnessLevel(float level, bool addRecord = false);

  void onCompareBegin();

  void onCompareEnd();

  void updateMVPMatrix(float* matrix);

  bool canUndo();

  bool canRedo();

  OpenPSRecord undo();

  OpenPSRecord redo();

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
  std::shared_ptr<ContrastFilter> contrastFilter;
  std::shared_ptr<ExposureFilter> exposureFilter;
  std::shared_ptr<SaturationFilter> saturationFilter;
  std::shared_ptr<SharpenFilter> sharpenFilter;
  std::shared_ptr<BrightnessFilter> brightnessFilter;
  std::shared_ptr<TargetView> targetView;
  std::shared_ptr<TargetRawDataOutput> targetRawDataOutput;

  float smoothLevel = 0;
  float whiteLevel = 0;
  float lipstickLevel = 0;
  float blusherLevel = 0;
  float eyeZoomLevel = 0;
  float faceSlimLevel = 0;
  float contrastLevel = 1.0;
  float exposureLevel = 0;
  float saturationLevel = 1.0;
  float sharpnessLevel = 0;
  float brightnessLevel = 0;

  int imageWidth = 0;
  int imageHeight = 0;

  float scaleFactor = 1;
  float distanceX = 0;
  float distanceY = 0;
  float totalNormalizedDistanceX = 0;
  float totalNormalizedDistanceY = 0;
  bool needResetMatrix = false;

  Matrix4 modelMatrix;
  void handleMVPMatrix();

  UndoRedoHelper undoRedoHelper;
  void addUndoRedoRecord();
  void setLevels(OpenPSRecord record);
};

NS_GPUPIXEL_END
