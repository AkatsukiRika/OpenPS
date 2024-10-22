#include "openps_helper.h"
#include "util.h"

gpupixel::OpenPSHelper::OpenPSHelper() {
  targetView = std::make_shared<TargetView>();
  undoRedoHelper = UndoRedoHelper();
}

gpupixel::OpenPSHelper::~OpenPSHelper() {
  gpuSourceImage.reset();
  beautyFaceFilter.reset();
  lipstickFilter.reset();
  blusherFilter.reset();
  faceReshapeFilter.reset();
  contrastFilter.reset();
  exposureFilter.reset();
  saturationFilter.reset();
  sharpenFilter.reset();
  brightnessFilter.reset();
  targetView.reset();
  targetRawDataOutput.reset();
  GPUPixelContext::destroy();
}

void gpupixel::OpenPSHelper::initWithImage(int width, int height,
                                           int channelCount,
                                           const unsigned char *pixels) {
  gpuSourceImage = SourceImage::create_from_memory(width, height, channelCount, pixels);
  imageWidth = width;
  imageHeight = height;
}

void gpupixel::OpenPSHelper::onTargetViewSizeChanged(int width, int height) {
  targetView->onSizeChanged(width, height);
}

void gpupixel::OpenPSHelper::getTargetViewInfo(float *info) {
  targetView->getViewInfo(info);
}

void gpupixel::OpenPSHelper::buildBasicRenderPipeline() {
  gpuSourceImage->addTarget(targetView);
}

void gpupixel::OpenPSHelper::buildRealRenderPipeline() {
  gpuSourceImage->removeAllTargets();
  beautyFaceFilter = BeautyFaceFilter::create();
  lipstickFilter = LipstickFilter::create();
  blusherFilter = BlusherFilter::create();
  faceReshapeFilter = FaceReshapeFilter::create();
  contrastFilter = ContrastFilter::create();
  exposureFilter = ExposureFilter::create();
  saturationFilter = SaturationFilter::create();
  sharpenFilter = SharpenFilter::create();
  sharpenFilter->setTexelSize(imageWidth, imageHeight);
  brightnessFilter = BrightnessFilter::create();
  targetRawDataOutput = TargetRawDataOutput::create();
  gpuSourceImage->RegLandmarkCallback([=](std::vector<float> landmarks, std::vector<float> rect) {
    lipstickFilter->SetFaceLandmarks(landmarks);
    blusherFilter->SetFaceLandmarks(landmarks);
    faceReshapeFilter->SetFaceLandmarks(landmarks);
  });
  gpuSourceImage
      ->addTarget(contrastFilter)
      ->addTarget(exposureFilter)
      ->addTarget(saturationFilter)
      ->addTarget(sharpenFilter)
      ->addTarget(brightnessFilter)
      ->addTarget(lipstickFilter)
      ->addTarget(blusherFilter)
      ->addTarget(faceReshapeFilter)
      ->addTarget(beautyFaceFilter)
      ->addTarget(targetView);
  beautyFaceFilter->addTarget(targetRawDataOutput);
}

void gpupixel::OpenPSHelper::buildNoFaceRenderPipeline() {
  if (gpuSourceImage) {
    gpuSourceImage->removeAllTargets();
    contrastFilter = ContrastFilter::create();
    exposureFilter = ExposureFilter::create();
    saturationFilter = SaturationFilter::create();
    sharpenFilter = SharpenFilter::create();
    sharpenFilter->setTexelSize(imageWidth, imageHeight);
    brightnessFilter = BrightnessFilter::create();
    targetRawDataOutput = TargetRawDataOutput::create();
    gpuSourceImage
        ->addTarget(contrastFilter)
        ->addTarget(exposureFilter)
        ->addTarget(saturationFilter)
        ->addTarget(sharpenFilter)
        ->addTarget(brightnessFilter)
        ->addTarget(targetView);
    brightnessFilter->addTarget(targetRawDataOutput);
  }
}

void gpupixel::OpenPSHelper::requestRender() {
  if (gpuSourceImage) {
    if (matrixUpdated && beautyFaceFilter && beautyFaceFilter->getFramebuffer()) {
      beautyFaceFilter->updateTargets(0, false);
      targetView->updateMatrixState();
      matrixUpdated = false;
    } else {
      gpuSourceImage->Render();
    }
  }
}

void gpupixel::OpenPSHelper::setLandmarkCallback(gpupixel::FaceDetectorCallback callback) {
  gpuSourceImage->RegLandmarkCallback(callback);
}

void gpupixel::OpenPSHelper::setRawOutputCallback(gpupixel::RawOutputCallback callback) {
  if (targetRawDataOutput) {
    targetRawDataOutput->setPixelsCallbck(callback);
  }
}

void gpupixel::OpenPSHelper::setSmoothLevel(float level, bool addRecord) {
  if (beautyFaceFilter) {
    smoothLevel = level;
    beautyFaceFilter->setBlurAlpha(level);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setWhiteLevel(float level, bool addRecord) {
  if (beautyFaceFilter) {
    whiteLevel = level / 2;
    beautyFaceFilter->setWhite(whiteLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setLipstickLevel(float level, bool addRecord) {
  if (lipstickFilter) {
    lipstickLevel = level;
    lipstickFilter->setBlendLevel(level);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setBlusherLevel(float level, bool addRecord) {
  if (blusherFilter) {
    blusherLevel = level;
    blusherFilter->setBlendLevel(level);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setEyeZoomLevel(float level, bool addRecord) {
  if (faceReshapeFilter) {
    eyeZoomLevel = level / 5;
    faceReshapeFilter->setEyeZoomLevel(eyeZoomLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setFaceSlimLevel(float level, bool addRecord) {
  if (faceReshapeFilter) {
    faceSlimLevel = level / 10;
    faceReshapeFilter->setFaceSlimLevel(faceSlimLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setContrastLevel(float level, bool addRecord) {
  if (contrastFilter) {
    // 滤镜本身支持0～4，为避免极端效果限制在0.5～2
    if (level < 0) {
      contrastLevel = 1.0 - 0.5 * abs(level);
    } else {
      contrastLevel = 1.0 + abs(level);
    }
    contrastFilter->setContrast(contrastLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setExposureLevel(float level, bool addRecord) {
  if (exposureFilter) {
    // 滤镜本身支持-10～10，为避免极端效果限制在-1.5～1.5
    exposureLevel = level * 1.5;
    exposureFilter->setExposure(exposureLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setSaturationLevel(float level, bool addRecord) {
  if (saturationFilter) {
    saturationLevel = level + 1.0;
    saturationFilter->setSaturation(saturationLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setSharpenLevel(float level, bool addRecord) {
  if (sharpenFilter) {
    sharpnessLevel = level * 2;
    sharpenFilter->setSharpness(sharpnessLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::setBrightnessLevel(float level, bool addRecord) {
  if (brightnessFilter) {
    // 滤镜支持-1～1，为避免极端效果限制在-0.5～0.5
    brightnessLevel = level * 0.5;
    brightnessFilter->setBrightness(brightnessLevel);
    if (addRecord) {
      addUndoRedoRecord();
    }
  }
}

void gpupixel::OpenPSHelper::onCompareBegin() {
  if (beautyFaceFilter) {
    beautyFaceFilter->setBlurAlpha(0);
    beautyFaceFilter->setWhite(0);
  }
  if (lipstickFilter) {
    lipstickFilter->setBlendLevel(0);
  }
  if (blusherFilter) {
    blusherFilter->setBlendLevel(0);
  }
  if (faceReshapeFilter) {
    faceReshapeFilter->setEyeZoomLevel(0);
    faceReshapeFilter->setFaceSlimLevel(0);
  }
  if (contrastFilter) {
    contrastFilter->setContrast(1);
  }
  if (exposureFilter) {
    exposureFilter->setExposure(0);
  }
  if (saturationFilter) {
    saturationFilter->setSaturation(1);
  }
  if (sharpenFilter) {
    sharpenFilter->setSharpness(0);
  }
  if (brightnessFilter) {
    brightnessFilter->setBrightness(0);
  }
}

void gpupixel::OpenPSHelper::onCompareEnd() {
  if (beautyFaceFilter) {
    beautyFaceFilter->setBlurAlpha(smoothLevel);
    beautyFaceFilter->setWhite(whiteLevel);
  }
  if (lipstickFilter) {
    lipstickFilter->setBlendLevel(lipstickLevel);
  }
  if (blusherFilter) {
    blusherFilter->setBlendLevel(blusherLevel);
  }
  if (faceReshapeFilter) {
    faceReshapeFilter->setEyeZoomLevel(eyeZoomLevel);
    faceReshapeFilter->setFaceSlimLevel(faceSlimLevel);
  }
  if (contrastFilter) {
    contrastFilter->setContrast(contrastLevel);
  }
  if (exposureFilter) {
    exposureFilter->setExposure(exposureLevel);
  }
  if (saturationFilter) {
    saturationFilter->setSaturation(saturationLevel);
  }
  if (sharpenFilter) {
    sharpenFilter->setSharpness(sharpnessLevel);
  }
  if (brightnessFilter) {
    brightnessFilter->setBrightness(brightnessLevel);
  }
}

void gpupixel::OpenPSHelper::updateMVPMatrix(float *matrix) {
  if (targetView) {
    targetView->setMVPMatrix(Matrix4(matrix));
    matrixUpdated = true;
  }
}

bool gpupixel::OpenPSHelper::canUndo() {
  return undoRedoHelper.canUndo();
}

bool gpupixel::OpenPSHelper::canRedo() {
  return undoRedoHelper.canRedo();
}

gpupixel::OpenPSRecord gpupixel::OpenPSHelper::undo() {
  bool check = canUndo();
  auto result = undoRedoHelper.undo();
  if (check) {
    setLevels(result);
  }
  return result;
}

gpupixel::OpenPSRecord gpupixel::OpenPSHelper::redo() {
  bool check = canRedo();
  auto result = undoRedoHelper.redo();
  if (check) {
    setLevels(result);
  }
  return result;
}

void gpupixel::OpenPSHelper::addUndoRedoRecord() {
  float smoothRecordLevel = smoothLevel;
  float whiteRecordLevel = whiteLevel * 2;
  float lipstickRecordLevel = lipstickLevel;
  float blusherRecordLevel = blusherLevel;
  float eyeZoomRecordLevel = eyeZoomLevel * 5;
  float faceSlimRecordLevel = faceSlimLevel * 10;
  float contrastRecordLevel;
  if (contrastLevel < 1) {
    contrastRecordLevel = (contrastLevel - 1) * 2;
  } else {
    contrastRecordLevel = contrastLevel - 1;
  }
  float exposureRecordLevel = exposureLevel / 1.5;
  float saturationRecordLevel = saturationLevel - 1;
  float sharpnessRecordLevel = sharpnessLevel / 2;
  float brightnessRecordLevel = brightnessLevel / 0.5;
  auto record = OpenPSRecord(
      smoothRecordLevel, whiteRecordLevel, lipstickRecordLevel,
      blusherRecordLevel, eyeZoomRecordLevel, faceSlimRecordLevel,
      contrastRecordLevel, exposureRecordLevel, saturationRecordLevel,
      sharpnessRecordLevel, brightnessRecordLevel);
  undoRedoHelper.addRecord(record);
}

void gpupixel::OpenPSHelper::setLevels(gpupixel::OpenPSRecord record) {
  setSmoothLevel(record.smoothLevel, false);
  setWhiteLevel(record.whiteLevel, false);
  setLipstickLevel(record.lipstickLevel, false);
  setBlusherLevel(record.blusherLevel, false);
  setEyeZoomLevel(record.eyeZoomLevel, false);
  setFaceSlimLevel(record.faceSlimLevel, false);
  setContrastLevel(record.contrastLevel, false);
  setExposureLevel(record.exposureLevel, false);
  setSaturationLevel(record.saturationLevel, false);
  setSharpenLevel(record.sharpnessLevel, false);
  setBrightnessLevel(record.brightnessLevel, false);
}
