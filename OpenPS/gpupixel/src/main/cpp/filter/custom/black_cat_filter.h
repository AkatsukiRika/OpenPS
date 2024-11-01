#pragma once

#include "filter.h"
#include "gpupixel_macros.h"
#include "source_image.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API BlackCatFilter : public Filter {
public:
  static std::shared_ptr<BlackCatFilter> create();
  void setIntensity(float newIntensity);
  bool init();
  virtual bool proceed(bool bUpdateTargets = true, int64_t frameTime = 0) override;

private:
  std::shared_ptr<SourceImage> curveImage;
  float intensity = 1;
};

NS_GPUPIXEL_END
