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
  const std::string imageFileName;
  const bool isMirrored;
  const bool isFlipped;
  const float croppedLeft;
  const float croppedTop;
  const float croppedRight;
  const float croppedBottom;

  std::string toString() const override {
    return Util::str_format(
        "smoothLevel: %f, whiteLevel: %f, lipstickLevel: %f, blusherLevel: %f, "
        "eyeZoomLevel: %f, faceSlimLevel: %f, contrastLevel: %f, "
        "exposureLevel: %f, saturationLevel: %f, sharpnessLevel: %f, "
        "brightnessLevel: %f, customFilterType: %d, customFilterIntensity: %f, "
        "imageFileName: %s, isMirrored: %d, isFlipped: %d, "
        "croppedLeft: %f, croppedTop: %f, croppedRight: %f, croppedBottom: %f",
        smoothLevel, whiteLevel, lipstickLevel, blusherLevel, eyeZoomLevel,
        faceSlimLevel, contrastLevel, exposureLevel, saturationLevel,
        sharpnessLevel, brightnessLevel, customFilterType, customFilterIntensity,
        imageFileName.c_str(), isMirrored, isFlipped,
        croppedLeft, croppedTop, croppedRight, croppedBottom);
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
           brightnessLevel == record->brightnessLevel &&
           imageFileName == record->imageFileName &&
           isMirrored == record->isMirrored &&
           isFlipped == record->isFlipped &&
           croppedLeft == record->croppedLeft &&
           croppedTop == record->croppedTop &&
           croppedRight == record->croppedRight &&
           croppedBottom == record->croppedBottom &&
           customFilterEquals;
  }

  AbstractRecord* clone() const override {
    return new OpenPSRecord(smoothLevel, whiteLevel, lipstickLevel, blusherLevel,
                            eyeZoomLevel, faceSlimLevel, contrastLevel, exposureLevel,
                            saturationLevel, sharpnessLevel, brightnessLevel, customFilterType, customFilterIntensity,
                            imageFileName, isMirrored, isFlipped,
                            croppedLeft, croppedTop, croppedRight, croppedBottom);
  }

  OpenPSRecord(float smoothLevel, float whiteLevel, float lipstickLevel,
               float blusherLevel, float eyeZoomLevel, float faceSlimLevel,
               float contrastLevel, float exposureLevel, float saturationLevel,
               float sharpnessLevel, float brightnessLevel, int customFilterType, float customFilterIntensity,
               std::string imageFileName = "", bool isMirrored = false, bool isFlipped = false,
               float croppedLeft = 0, float croppedTop = 0, float croppedRight = 1, float croppedBottom = 1)
      : smoothLevel(smoothLevel), whiteLevel(whiteLevel),
        lipstickLevel(lipstickLevel), blusherLevel(blusherLevel),
        eyeZoomLevel(eyeZoomLevel), faceSlimLevel(faceSlimLevel),
        contrastLevel(contrastLevel), exposureLevel(exposureLevel),
        saturationLevel(saturationLevel), sharpnessLevel(sharpnessLevel),
        brightnessLevel(brightnessLevel), customFilterType(customFilterType), customFilterIntensity(customFilterIntensity),
        imageFileName(imageFileName), isMirrored(isMirrored), isFlipped(isFlipped),
        croppedLeft(croppedLeft), croppedTop(croppedTop), croppedRight(croppedRight), croppedBottom(croppedBottom) {};
};

NS_GPUPIXEL_END