#pragma once

#include "filter_group.h"
#include "gpupixel_macros.h"
#include "fairy_tale_filter.h"
#include "sunrise_filter.h"
#include "sunset_filter.h"
#include "white_cat_filter.h"
#include "black_cat_filter.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API CustomFilter : public FilterGroup {
public:
  static std::shared_ptr<CustomFilter> create();
  ~CustomFilter();
  bool init();
  void setType(int newType);
  void setIntensity(float newIntensity);

  virtual void setInputFramebuffer(std::shared_ptr<Framebuffer> framebuffer,
                                   RotationMode rotationMode /* = NoRotation*/,
                                   int texIdx /* = 0*/) override;

protected:
  CustomFilter();

  static constexpr int TYPE_ORIGINAL = 0;
  static constexpr int TYPE_FAIRY_TALE = 1;
  static constexpr int TYPE_SUNRISE = 2;
  static constexpr int TYPE_SUNSET = 3;
  static constexpr int TYPE_WHITE_CAT = 4;
  static constexpr int TYPE_BLACK_CAT = 5;

  std::shared_ptr<FairyTaleFilter> fairyTaleFilter;
  std::shared_ptr<SunriseFilter> sunriseFilter;
  std::shared_ptr<SunsetFilter> sunsetFilter;
  std::shared_ptr<WhiteCatFilter> whiteCatFilter;
  std::shared_ptr<BlackCatFilter> blackCatFilter;

  int type = TYPE_ORIGINAL;
  float intensity = 0;
};

NS_GPUPIXEL_END