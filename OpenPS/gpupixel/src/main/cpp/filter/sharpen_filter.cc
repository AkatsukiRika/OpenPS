/*
* Sharpen Filter
*
* Created by Akari on 2024/9/26.
* Copyright © 2024 Akari. All rights reserved.
 */

#include "sharpen_filter.h"

USING_NS_GPUPIXEL

const std::string kSharpenFragmentShaderString = R"(
uniform sampler2D inputImageTexture;
uniform lowp float sharpness;
uniform highp vec2 texelSize;
varying highp vec2 textureCoordinate;

void main() {
  lowp vec4 color = texture2D(inputImageTexture, textureCoordinate);
  lowp vec4 colorLeft = texture2D(inputImageTexture, textureCoordinate - vec2(texelSize.x, 0.0));
  lowp vec4 colorRight = texture2D(inputImageTexture, textureCoordinate + vec2(texelSize.x, 0.0));
  lowp vec4 colorUp = texture2D(inputImageTexture, textureCoordinate - vec2(0.0, texelSize.y));
  lowp vec4 colorDown = texture2D(inputImageTexture, textureCoordinate + vec2(0.0, texelSize.y));

  lowp vec4 sharpColor = color * (1.0 + 4.0 * sharpness)
                       - (colorLeft + colorRight + colorUp + colorDown) * sharpness;
  sharpColor = clamp(sharpColor, 0.0, 1.0);
  gl_FragColor = sharpColor;
}
)";

std::shared_ptr<SharpenFilter> gpupixel::SharpenFilter::create() {
  auto ret = std::shared_ptr<SharpenFilter>(new SharpenFilter());
  if (ret && !ret->init()) {
    ret.reset();
  }
  return ret;
}

bool SharpenFilter::init() {
  if (!initWithFragmentShaderString(kSharpenFragmentShaderString)) {
    return false;
  }
  _sharpness = 0;
  return true;
}

void SharpenFilter::setSharpness(float sharpness) {
  _sharpness = sharpness;
  if (_sharpness < 0) {
    _sharpness = 0;
  } else if (_sharpness > 1) {
    _sharpness = 1;
  }
}

void SharpenFilter::setTexelSize(int textureWidth, int textureHeight) {
  _texelSizeX = 1.0f / textureWidth;
  _texelSizeY = 1.0f / textureHeight;
}

bool SharpenFilter::proceed(bool bUpdateTargets, int64_t frametime) {
  _filterProgram->setUniformValue("sharpness", _sharpness);
  _filterProgram->setUniformValue("texelSize", Vector2(_texelSizeX, _texelSizeY));
  return Filter::proceed(bUpdateTargets, frametime);
}
