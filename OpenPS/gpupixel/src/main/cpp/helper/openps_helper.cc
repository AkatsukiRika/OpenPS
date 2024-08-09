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
  targetView.reset();
  GPUPixelContext::destroy();
}

void gpupixel::OpenPSHelper::initWithImage(int width, int height,
                                           int channelCount,
                                           const unsigned char *pixels) {
  gpuSourceImage = SourceImage::create_from_memory(width, height, channelCount, pixels);
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
  targetRawDataOutput = TargetRawDataOutput::create();
  gpuSourceImage->RegLandmarkCallback([=](std::vector<float> landmarks, std::vector<float> rect) {
    lipstickFilter->SetFaceLandmarks(landmarks);
    blusherFilter->SetFaceLandmarks(landmarks);
    faceReshapeFilter->SetFaceLandmarks(landmarks);
  });
  gpuSourceImage
      ->addTarget(lipstickFilter)
      ->addTarget(blusherFilter)
      ->addTarget(faceReshapeFilter)
      ->addTarget(beautyFaceFilter)
      ->addTarget(targetView);
  beautyFaceFilter->addTarget(targetRawDataOutput);
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
}
