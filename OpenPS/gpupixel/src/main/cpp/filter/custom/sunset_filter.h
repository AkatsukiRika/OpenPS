#pragma once

#include "filter.h"
#include "gpupixel_macros.h"
#include "source_image.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API SunsetFilter : public Filter {
public:
  static std::shared_ptr<SunsetFilter> create();
  void setIntensity(float newIntensity);
  bool init();
  virtual bool proceed(bool bUpdateTargets = true, int64_t frameTime = 0) override;

private:
  std::shared_ptr<SourceImage> curveImage;
  std::shared_ptr<SourceImage> greyMaskImage1;
  std::shared_ptr<SourceImage> greyMaskImage2;
  float intensity = 1;
};

NS_GPUPIXEL_END
