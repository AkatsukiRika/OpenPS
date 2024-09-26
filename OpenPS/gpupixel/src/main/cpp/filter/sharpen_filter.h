/*
* Sharpen Filter
*
* Created by Akari on 2024/9/26.
* Copyright © 2024 Akari. All rights reserved.
*/

#pragma once

#include "filter.h"
#include "gpupixel_macros.h"

NS_GPUPIXEL_BEGIN
class GPUPIXEL_API SharpenFilter : public Filter {
public:
  static std::shared_ptr<SharpenFilter> create();
  bool init();
  void setSharpness(float sharpness);
  void setTexelSize(int textureWidth, int textureHeight);
  virtual bool proceed(bool bUpdateTargets = true,
                       int64_t frameTime = 0) override;

protected:
  float _sharpness;
  float _texelSizeX;
  float _texelSizeY;
};

NS_GPUPIXEL_END