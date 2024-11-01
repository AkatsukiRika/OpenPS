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

  sunsetFilter = SunsetFilter::create();
  addFilter(sunsetFilter);

  whiteCatFilter = WhiteCatFilter::create();
  addFilter(whiteCatFilter);

  blackCatFilter = BlackCatFilter::create();
  addFilter(blackCatFilter);

  fairyTaleFilter
      ->addTarget(sunriseFilter)
      ->addTarget(sunsetFilter)
      ->addTarget(whiteCatFilter)
      ->addTarget(blackCatFilter);

  return true;
}

void CustomFilter::setType(int newType) {
  type = newType;
}

int CustomFilter::getType() {
  return type;
}

void CustomFilter::setIntensity(float newIntensity) {
  intensity = newIntensity;
  switch (type) {
    case TYPE_FAIRY_TALE:
      fairyTaleFilter->setIntensity(intensity);
      sunriseFilter->setIntensity(0);
      sunsetFilter->setIntensity(0);
      whiteCatFilter->setIntensity(0);
      blackCatFilter->setIntensity(0);
      break;
    case TYPE_SUNRISE:
      fairyTaleFilter->setIntensity(0);
      sunriseFilter->setIntensity(intensity);
      sunsetFilter->setIntensity(0);
      whiteCatFilter->setIntensity(0);
      blackCatFilter->setIntensity(0);
      break;
    case TYPE_SUNSET:
      fairyTaleFilter->setIntensity(0);
      sunriseFilter->setIntensity(0);
      sunsetFilter->setIntensity(intensity);
      whiteCatFilter->setIntensity(0);
      blackCatFilter->setIntensity(0);
      break;
    case TYPE_WHITE_CAT:
      fairyTaleFilter->setIntensity(0);
      sunriseFilter->setIntensity(0);
      sunsetFilter->setIntensity(0);
      whiteCatFilter->setIntensity(intensity);
      blackCatFilter->setIntensity(0);
      break;
    case TYPE_BLACK_CAT:
      fairyTaleFilter->setIntensity(0);
      sunriseFilter->setIntensity(0);
      sunsetFilter->setIntensity(0);
      whiteCatFilter->setIntensity(0);
      blackCatFilter->setIntensity(intensity);
      break;
    default:
      fairyTaleFilter->setIntensity(0);
      sunriseFilter->setIntensity(0);
      sunsetFilter->setIntensity(0);
      whiteCatFilter->setIntensity(0);
      blackCatFilter->setIntensity(0);
  }
}

void CustomFilter::setInputFramebuffer(std::shared_ptr<Framebuffer> framebuffer,
                                       RotationMode rotationMode, int texIdx) {
  for (auto& filter : _filters) {
    filter->setInputFramebuffer(framebuffer, rotationMode, texIdx);
  }
}
