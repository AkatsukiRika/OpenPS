#include "custom_filter.h"
#include <unordered_map>

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

  beautyFilter = BeautyFilter::create();
  addFilter(beautyFilter);

  skinWhitenFilter = SkinWhitenFilter::create();
  addFilter(skinWhitenFilter);

  healthyFilter = HealthyFilter::create();
  addFilter(healthyFilter);

  fairyTaleFilter
      ->addTarget(sunriseFilter)
      ->addTarget(sunsetFilter)
      ->addTarget(whiteCatFilter)
      ->addTarget(blackCatFilter)
      ->addTarget(beautyFilter)
      ->addTarget(skinWhitenFilter)
      ->addTarget(healthyFilter);

  return true;
}

void CustomFilter::setType(int newType) {
  type = newType;
}

int CustomFilter::getType() {
  return type;
}

void CustomFilter::setTexelSize(int textureWidth, int textureHeight) {
  if (beautyFilter) {
    beautyFilter->setTexelSize(textureWidth, textureHeight);
  }
  if (skinWhitenFilter) {
    skinWhitenFilter->setTexelSize(textureWidth, textureHeight);
  }
  if (healthyFilter) {
    healthyFilter->setTexelSize(textureWidth, textureHeight);
  }
}

void CustomFilter::setIntensity(float newIntensity) {
  intensity = newIntensity;

  std::unordered_map<int, std::function<void(float)>> intensitySetters = {
    {
      TYPE_FAIRY_TALE,
      [this](float i) { fairyTaleFilter->setIntensity(i); }
    },
    {
      TYPE_SUNRISE,
      [this](float i) { sunriseFilter->setIntensity(i); }
    },
    {
      TYPE_SUNSET,
      [this](float i) { sunsetFilter->setIntensity(i); }
    },
    {
      TYPE_WHITE_CAT,
      [this](float i) { whiteCatFilter->setIntensity(i); }
    },
    {
      TYPE_BLACK_CAT,
      [this](float i) { blackCatFilter->setIntensity(i); }
    },
    {
      TYPE_BEAUTY,
      [this](float i) { beautyFilter->setIntensity(i); }
    },
    {
      TYPE_SKIN_WHITEN,
      [this](float i) { skinWhitenFilter->setIntensity(i); }
    },
    {
      TYPE_HEALTHY,
      [this](float i) { healthyFilter->setIntensity(i); }
    }
  };

  for (const auto& setter : intensitySetters) {
    setter.second(0);
  }

  auto it = intensitySetters.find(type);
  if (it != intensitySetters.end()) {
    it->second(intensity);
  }
}

void CustomFilter::setInputFramebuffer(std::shared_ptr<Framebuffer> framebuffer,
                                       RotationMode rotationMode, int texIdx) {
  for (auto& filter : _filters) {
    filter->setInputFramebuffer(framebuffer, rotationMode, texIdx);
  }
}
