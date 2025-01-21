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
#include "abstract_record.h"
#include "openps_record.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API OpenPSHelper {
public:
  OpenPSHelper();

  ~OpenPSHelper();

  void initWithImage(int width, int height, int channelCount, const unsigned char* pixels, const char* filename = nullptr);

  void changeImage(int width, int height, int channelCount, const unsigned char* pixels, const char* filename = nullptr);

  void changeImage(std::string filename);

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

  void applyCustomFilter(int type, float level = 1, bool addRecord = false);

  void onCompareBegin();

  void onCompareEnd();

  void updateMVPMatrix(float* matrix);

  bool canUndo();

  bool canRedo();

  std::shared_ptr<OpenPSRecord> undo();

  std::shared_ptr<OpenPSRecord> redo();

  std::string getCurrentImageFileName();

private:
  std::shared_ptr<SourceImage> gpuSourceImage;
  std::shared_ptr<SourceImage> initialSourceImage;
  std::shared_ptr<LipstickFilter> lipstickFilter;
  std::shared_ptr<BlusherFilter> blusherFilter;
  std::shared_ptr<FaceReshapeFilter> faceReshapeFilter;
  std::shared_ptr<BeautyFaceFilter> beautyFaceFilter;
  std::shared_ptr<ContrastFilter> contrastFilter;
  std::shared_ptr<ExposureFilter> exposureFilter;
  std::shared_ptr<SaturationFilter> saturationFilter;
  std::shared_ptr<SharpenFilter> sharpenFilter;
  std::shared_ptr<BrightnessFilter> brightnessFilter;
  std::shared_ptr<CustomFilter> customFilter;
  std::shared_ptr<ImageCompareFilter> imageCompareFilter;
  std::shared_ptr<TargetView> targetView;
  std::shared_ptr<TargetRawDataOutput> targetRawDataOutput;
  std::vector<std::shared_ptr<Filter>> filterList;

  static constexpr float DEFAULT_LEVEL = 0;
  static constexpr float DEFAULT_CONTRAST_LEVEL = 1;
  static constexpr float DEFAULT_SATURATION_LEVEL = 1;

  float smoothLevel = DEFAULT_LEVEL;
  float whiteLevel = DEFAULT_LEVEL;
  float lipstickLevel = DEFAULT_LEVEL;
  float blusherLevel = DEFAULT_LEVEL;
  float eyeZoomLevel = DEFAULT_LEVEL;
  float faceSlimLevel = DEFAULT_LEVEL;
  float contrastLevel = DEFAULT_CONTRAST_LEVEL;
  float exposureLevel = DEFAULT_LEVEL;
  float saturationLevel = DEFAULT_SATURATION_LEVEL;
  float sharpnessLevel = DEFAULT_LEVEL;
  float brightnessLevel = DEFAULT_LEVEL;
  float customFilterLevel = DEFAULT_LEVEL;
  std::string currentImageFileName = "";

  int imageWidth = 0;
  int imageHeight = 0;
  bool matrixUpdated = false;
  std::string initialImageFileName = "";

  UndoRedoHelper undoRedoHelper;
  void addUndoRedoRecord();
  void setLevels(OpenPSRecord record);
  void refreshRenderPipeline();
  /**
   * @return needRebuild
   */
  bool addOrRemoveFilter(bool needFilter, std::shared_ptr<Filter> filter);
};

NS_GPUPIXEL_END
