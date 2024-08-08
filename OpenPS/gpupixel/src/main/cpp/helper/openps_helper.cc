#include "openps_helper.h"
#include "util.h"

gpupixel::OpenPSHelper::OpenPSHelper() {
  targetView = std::make_shared<TargetView>();
}

gpupixel::OpenPSHelper::~OpenPSHelper() {
  gpuSourceImage.reset();
  beautyFaceFilter.reset();
  lipstickFilter.reset();
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
  gpuSourceImage->RegLandmarkCallback([=](std::vector<float> landmarks, std::vector<float> rect) {
    lipstickFilter->SetFaceLandmarks(landmarks);
    blusherFilter->SetFaceLandmarks(landmarks);
  });
  gpuSourceImage
      ->addTarget(lipstickFilter)
      ->addTarget(blusherFilter)
      ->addTarget(beautyFaceFilter)
      ->addTarget(targetView);
}

void gpupixel::OpenPSHelper::requestRender() {
  if (gpuSourceImage) {
    gpuSourceImage->Render();
  }
}

void gpupixel::OpenPSHelper::setLandmarkCallback(gpupixel::FaceDetectorCallback callback) {
  gpuSourceImage->RegLandmarkCallback(callback);
}

void gpupixel::OpenPSHelper::setSmoothLevel(float level) {
  if (beautyFaceFilter) {
    beautyFaceFilter->setBlurAlpha(level);
  }
}

void gpupixel::OpenPSHelper::setWhiteLevel(float level) {
  if (beautyFaceFilter) {
    beautyFaceFilter->setWhite(level);
  }
}

void gpupixel::OpenPSHelper::setLipstickLevel(float level) {
  if (lipstickFilter) {
    lipstickFilter->setBlendLevel(level);
  }
}

void gpupixel::OpenPSHelper::setBlusherLevel(float level) {
  if (blusherFilter) {
    blusherFilter->setBlendLevel(level);
  }
}
