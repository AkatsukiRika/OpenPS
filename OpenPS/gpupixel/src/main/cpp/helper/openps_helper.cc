#include "openps_helper.h"
#include "util.h"

gpupixel::OpenPSHelper::OpenPSHelper() {
  targetView = std::make_shared<TargetView>();
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
    gpuSourceImage->Render();
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

void gpupixel::OpenPSHelper::setSmoothLevel(float level) {
  if (beautyFaceFilter) {
    smoothLevel = level;
    beautyFaceFilter->setBlurAlpha(level);
  }
}

void gpupixel::OpenPSHelper::setWhiteLevel(float level) {
  if (beautyFaceFilter) {
    whiteLevel = level / 2;
    beautyFaceFilter->setWhite(whiteLevel);
  }
}

void gpupixel::OpenPSHelper::setLipstickLevel(float level) {
  if (lipstickFilter) {
    lipstickLevel = level;
    lipstickFilter->setBlendLevel(level);
  }
}

void gpupixel::OpenPSHelper::setBlusherLevel(float level) {
  if (blusherFilter) {
    blusherLevel = level;
    blusherFilter->setBlendLevel(level);
  }
}

void gpupixel::OpenPSHelper::setEyeZoomLevel(float level) {
  if (faceReshapeFilter) {
    eyeZoomLevel = level / 5;
    faceReshapeFilter->setEyeZoomLevel(eyeZoomLevel);
  }
}

void gpupixel::OpenPSHelper::setFaceSlimLevel(float level) {
  if (faceReshapeFilter) {
    faceSlimLevel = level / 10;
    faceReshapeFilter->setFaceSlimLevel(faceSlimLevel);
  }
}

void gpupixel::OpenPSHelper::setContrastLevel(float level) {
  if (contrastFilter) {
    // 滤镜本身支持0～4，为避免极端效果限制在0.5～2
    if (level < 0) {
      contrastLevel = 1.0 - 0.5 * abs(level);
    } else {
      contrastLevel = 1.0 + abs(level);
    }
    contrastFilter->setContrast(contrastLevel);
  }
}

void gpupixel::OpenPSHelper::setExposureLevel(float level) {
  if (exposureFilter) {
    // 滤镜本身支持-10～10，为避免极端效果限制在-1.5～1.5
    exposureLevel = level * 1.5;
    exposureFilter->setExposure(exposureLevel);
  }
}

void gpupixel::OpenPSHelper::setSaturationLevel(float level) {
  if (saturationFilter) {
    saturationLevel = level + 1.0;
    saturationFilter->setSaturation(saturationLevel);
  }
}

void gpupixel::OpenPSHelper::setSharpenLevel(float level) {
  if (sharpenFilter) {
    sharpnessLevel = level * 2;
    sharpenFilter->setSharpness(sharpnessLevel);
  }
}

void gpupixel::OpenPSHelper::setBrightnessLevel(float level) {
  if (brightnessFilter) {
    // 滤镜支持-1～1，为避免极端效果限制在-0.5～0.5
    brightnessLevel = level * 0.5;
    brightnessFilter->setBrightness(brightnessLevel);
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

void gpupixel::OpenPSHelper::setScaleFactor(float scale) {
  scaleFactor *= scale;
  if (scaleFactor < 0.1) {
    scaleFactor = 0.1;
  } else if (scaleFactor > 10) {
    scaleFactor = 10;
  }
  handleMVPMatrix();
}

void gpupixel::OpenPSHelper::setTranslateDistance(float x, float y) {
  distanceX = x;
  distanceY = y;
  handleMVPMatrix();
}

void gpupixel::OpenPSHelper::resetMVPMatrix() {
  needResetMatrix = true;
  handleMVPMatrix();
}

void gpupixel::OpenPSHelper::handleMVPMatrix() {
  modelMatrix = Matrix4::IDENTITY;
  float* targetViewInfo = new float[4];
  if (targetView) {
    targetView->getViewInfo(targetViewInfo);
    float viewWidth = targetViewInfo[0];
    float viewHeight = targetViewInfo[1];
    float scaledWidth = targetViewInfo[2];
    float scaledHeight = targetViewInfo[3];

    float minDistanceX = -abs(scaledWidth * scaleFactor - 1.0);
    float maxDistanceX = abs(scaledWidth * scaleFactor - 1.0);
    float minDistanceY = -abs(scaledHeight * scaleFactor - 1.0);
    float maxDistanceY = abs(scaledHeight * scaleFactor - 1.0);

    if (needResetMatrix) {
      totalNormalizedDistanceX = 0;
      totalNormalizedDistanceY = 0;
      scaleFactor = 1;
      distanceX = 0;
      distanceY = 0;
      needResetMatrix = false;
    }

    float normalizedDistanceX = 2.0 * distanceX / viewWidth;
    float normalizedDistanceY = 2.0 * distanceY / viewHeight;
    float beforeNormalizedDistanceX = totalNormalizedDistanceX;
    float realNormalizedDistanceX = normalizedDistanceX;
    totalNormalizedDistanceX -= normalizedDistanceX;
    if (totalNormalizedDistanceX < minDistanceX) {
      totalNormalizedDistanceX = minDistanceX;
      realNormalizedDistanceX = beforeNormalizedDistanceX - minDistanceX;
    } else if (totalNormalizedDistanceX > maxDistanceX) {
      totalNormalizedDistanceX = maxDistanceX;
      realNormalizedDistanceX = maxDistanceX - beforeNormalizedDistanceX;
    }
    float beforeNormalizedDistanceY = totalNormalizedDistanceY;
    float realNormalizedDistanceY = normalizedDistanceY;
    totalNormalizedDistanceY += normalizedDistanceY;
    if (totalNormalizedDistanceY < minDistanceY) {
      totalNormalizedDistanceY = minDistanceY;
      realNormalizedDistanceY = minDistanceY - beforeNormalizedDistanceY;
    } else if (totalNormalizedDistanceY > maxDistanceY) {
      totalNormalizedDistanceY = maxDistanceY;
      realNormalizedDistanceY = maxDistanceY - beforeNormalizedDistanceY;
    }
    distanceX = (realNormalizedDistanceX * viewWidth) / 2;
    distanceY = (realNormalizedDistanceY * viewHeight) / 2;

    modelMatrix.translate(0, totalNormalizedDistanceX, totalNormalizedDistanceY, 0);
    modelMatrix.scale(0, scaleFactor, scaleFactor, 1);
    targetView->setMVPMatrix(modelMatrix);
  }
  delete[] targetViewInfo;
}

float gpupixel::OpenPSHelper::getDistanceX() {
  return distanceX;
}

float gpupixel::OpenPSHelper::getDistanceY() {
  return distanceY;
}
