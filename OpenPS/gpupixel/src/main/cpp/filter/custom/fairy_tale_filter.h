#pragma once

#include "filter.h"
#include "gpupixel_macros.h"
#include "source_image.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API FairyTaleFilter : public Filter {
public:
  static std::shared_ptr<FairyTaleFilter> create();
  bool init();
  void setIntensity(float newIntensity);
  virtual bool proceed(bool bUpdateTargets = true, int64_t frameTime = 0) override;

protected:
  std::shared_ptr<SourceImage> fairyTaleImage;
  float intensity = 1;
};

NS_GPUPIXEL_END