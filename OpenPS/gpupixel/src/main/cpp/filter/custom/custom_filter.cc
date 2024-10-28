#include "custom_filter.h"

USING_NS_GPUPIXEL

CustomFilter::CustomFilter() {}

CustomFilter::~CustomFilter() {}

std::shared_ptr<CustomFilter> gpupixel::CustomFilter::create() {
  auto ret = std::shared_ptr<CustomFilter>(new CustomFilter());
  if (ret && !ret->init()) {
    ret.reset();
  }
  return ret;
}

bool CustomFilter::init() {
  if (!FilterGroup::init()) {
    return false;
  }

  fairyTaleFilter = FairyTaleFilter::create();
  addFilter(fairyTaleFilter);

  sunriseFilter = SunriseFilter::create();
  addFilter(sunriseFilter);

  return true;
}

void CustomFilter::setType(int newType) {
  if (newType == type) {
    return;
  }
  type = newType;
  removeAllFilters();
  fairyTaleFilter->removeAllTargets();
  sunriseFilter->removeAllTargets();
  switch (type) {
    case TYPE_FAIRY_TALE:
      addFilter(fairyTaleFilter);
      break;
    case TYPE_SUNRISE:
      addFilter(sunriseFilter);
      break;
    default:
      break;
  }
}

void CustomFilter::setIntensity(float newIntensity) {
  intensity = newIntensity;
  switch (type) {
    case TYPE_FAIRY_TALE:
      sunriseFilter->setIntensity(0);
      fairyTaleFilter->setIntensity(intensity);
      break;
    case TYPE_SUNRISE:
      fairyTaleFilter->setIntensity(0);
      sunriseFilter->setIntensity(intensity);
      break;
  }
}

void CustomFilter::setInputFramebuffer(std::shared_ptr<Framebuffer> framebuffer,
                                       RotationMode rotationMode, int texIdx) {
  for (auto& filter : _filters) {
    filter->setInputFramebuffer(framebuffer, rotationMode, texIdx);
  }
}
