/*
 * OpenPSRecord
 *
 * Created by Rika on 2024/9/26.
 * CopyRight © 2024 Rika. All rights reserved.
 */

#pragma once

#include "gpupixel_macros.h"
#include "util.h"
#include "abstract_record.h"
#include <string>

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API OpenPSRecord : public AbstractRecord {
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

  std::string toString() const override {
    return Util::str_format(
        "smoothLevel: %f, whiteLevel: %f, lipstickLevel: %f, blusherLevel: "
        "%f, eyeZoomLevel: %f, faceSlimLevel: %f, contrastLevel: %f, "
        "exposureLevel: %f, saturationLevel: %f, sharpnessLevel: %f, "
        "brightnessLevel: %f, customFilterType: %d, customFilterIntensity: %f",
        smoothLevel, whiteLevel, lipstickLevel, blusherLevel, eyeZoomLevel,
        faceSlimLevel, contrastLevel, exposureLevel, saturationLevel,
        sharpnessLevel, brightnessLevel, customFilterType, customFilterIntensity);
  }

  bool equals(const AbstractRecord& anotherRecord) const override {
    const OpenPSRecord* record = dynamic_cast<const OpenPSRecord*>(&anotherRecord);
    if (!record) {
      return false;
    }

    bool customFilterEquals = false;
    if (customFilterType == record->customFilterType) {
      if (customFilterType == 0) {
        customFilterEquals = true;
      } else {
        customFilterEquals = customFilterIntensity == record->customFilterIntensity;
      }
    }
    return smoothLevel == record->smoothLevel &&
           whiteLevel == record->whiteLevel &&
           lipstickLevel == record->lipstickLevel &&
           blusherLevel == record->blusherLevel &&
           eyeZoomLevel == record->eyeZoomLevel &&
           faceSlimLevel == record->faceSlimLevel &&
           contrastLevel == record->contrastLevel &&
           exposureLevel == record->exposureLevel &&
           saturationLevel == record->saturationLevel &&
           sharpnessLevel == record->sharpnessLevel &&
           brightnessLevel == record->brightnessLevel && customFilterEquals;
  }

  AbstractRecord* clone() const override {
    return new OpenPSRecord(smoothLevel, whiteLevel, lipstickLevel, blusherLevel,
                            eyeZoomLevel, faceSlimLevel, contrastLevel, exposureLevel,
                            saturationLevel, sharpnessLevel, brightnessLevel, customFilterType, customFilterIntensity);
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