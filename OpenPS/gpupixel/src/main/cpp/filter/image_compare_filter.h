#pragma once

#include "filter.h"
#include "gpupixel_macros.h"
#include "source_image.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API ImageCompareFilter : public Filter {
public:
  static std::shared_ptr<ImageCompareFilter> create();
  void setIntensity(float newIntensity);
  void setOriginalImage(std::shared_ptr<SourceImage> image);
  bool init();
  virtual bool proceed(bool bUpdateTargets = true, int64_t frameTime = 0) override;

private:
  std::shared_ptr<SourceImage> originalImage;
  float intensity = 0;
};

NS_GPUPIXEL_END