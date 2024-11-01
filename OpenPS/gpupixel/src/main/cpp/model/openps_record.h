/*
 * OpenPSRecord
 *
 * Created by Rika on 2024/9/26.
 * CopyRight © 2024 Rika. All rights reserved.
 */

#pragma once

#include "gpupixel_macros.h"
#include "util.h"
#include <string>

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API OpenPSRecord {
public:
  const float smoothLevel;
  const float whiteLevel;
  const float lipstickLevel;
  const float blusherLevel;
  const float eyeZoomLevel;
  const float faceSlimLevel;
  const float contrastLevel;
  const float exposureLevel;
  const float saturationLevel;
  const float sharpnessLevel;
  const float brightnessLevel;
  const int customFilterType;
  const float customFilterIntensity;

  std::string toString() const {
    return Util::str_format(
        "smoothLevel: %f, whiteLevel: %f, lipstickLevel: %f, blusherLevel: "
        "%f, eyeZoomLevel: %f, faceSlimLevel: %f, contrastLevel: %f, "
        "exposureLevel: %f, saturationLevel: %f, sharpnessLevel: %f, "
        "brightnessLevel: %f, customFilterType: %d, customFilterIntensity: %f",
        smoothLevel, whiteLevel, lipstickLevel, blusherLevel, eyeZoomLevel,
        faceSlimLevel, contrastLevel, exposureLevel, saturationLevel,
        sharpnessLevel, brightnessLevel, customFilterType, customFilterIntensity);
  }

  bool equals(const OpenPSRecord& anotherRecord) const {
    bool customFilterEquals = false;
    if (customFilterType == anotherRecord.customFilterType) {
      if (customFilterType == 0) {
        customFilterEquals = true;
      } else {
        customFilterEquals = customFilterIntensity == anotherRecord.customFilterIntensity;
      }
    }
    return smoothLevel == anotherRecord.smoothLevel &&
           whiteLevel == anotherRecord.whiteLevel &&
           lipstickLevel == anotherRecord.lipstickLevel &&
           blusherLevel == anotherRecord.blusherLevel &&
           eyeZoomLevel == anotherRecord.eyeZoomLevel &&
           faceSlimLevel == anotherRecord.faceSlimLevel &&
           contrastLevel == anotherRecord.contrastLevel &&
           exposureLevel == anotherRecord.exposureLevel &&
           saturationLevel == anotherRecord.saturationLevel &&
           sharpnessLevel == anotherRecord.sharpnessLevel &&
           brightnessLevel == anotherRecord.brightnessLevel && customFilterEquals;
  }

  OpenPSRecord(float smoothLevel, float whiteLevel, float lipstickLevel,
               float blusherLevel, float eyeZoomLevel, float faceSlimLevel,
               float contrastLevel, float exposureLevel, float saturationLevel,
               float sharpnessLevel, float brightnessLevel, int customFilterType, float customFilterIntensity)
      : smoothLevel(smoothLevel), whiteLevel(whiteLevel),
        lipstickLevel(lipstickLevel), blusherLevel(blusherLevel),
        eyeZoomLevel(eyeZoomLevel), faceSlimLevel(faceSlimLevel),
        contrastLevel(contrastLevel), exposureLevel(exposureLevel),
        saturationLevel(saturationLevel), sharpnessLevel(sharpnessLevel),
        brightnessLevel(brightnessLevel), customFilterType(customFilterType), customFilterIntensity(customFilterIntensity) {};
};

NS_GPUPIXEL_END