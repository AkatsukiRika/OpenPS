#pragma once

#include "filter.h"
#include "gpupixel_macros.h"
#include "source_image.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API HealthyFilter : public Filter {
public:
  static std::shared_ptr<HealthyFilter> create();
  void setIntensity(float newIntensity);
  bool init();
  void setTexelSize(int textureWidth, int textureHeight);
  virtual bool proceed(bool bUpdateTargets = true, int64_t frameTime = 0) override;

private:
  std::shared_ptr<SourceImage> maskImage;
  std::shared_ptr<SourceImage> curveImage;
  float intensity = 1;
  int texelSizeX;
  int texelSizeY;
};

NS_GPUPIXEL_END