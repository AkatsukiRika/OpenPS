#include "image_compare_filter.h"

USING_NS_GPUPIXEL

const std::string kImageCompareFragmentShaderString = SHADER_STRING(
    varying highp vec2 textureCoordinate;
    precision highp float;

    uniform sampler2D inputImageTexture;
    uniform sampler2D originalImage;
    uniform float intensity;

    void main() {
      vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
      vec4 originalColor = texture2D(originalImage, textureCoordinate);
      gl_FragColor = mix(textureColor, originalColor, intensity);
    }
);

std::shared_ptr<ImageCompareFilter> gpupixel::ImageCompareFilter::create() {
  auto ret = std::shared_ptr<ImageCompareFilter>(new ImageCompareFilter());
  if (ret && !ret->init()) {
    ret.reset();
  }
  return ret;
}

void ImageCompareFilter::setIntensity(float newIntensity) {
  intensity = newIntensity;
}

void ImageCompareFilter::setOriginalImage(std::shared_ptr<SourceImage> image) {
  originalImage = image;
}

bool ImageCompareFilter::init() {
  if (!initWithFragmentShaderString(kImageCompareFragmentShaderString)) {
    return false;
  }
  return true;
}

bool ImageCompareFilter::proceed(bool bUpdateTargets, int64_t frameTime) {
  if (originalImage) {
    CHECK_GL(glActiveTexture(GL_TEXTURE3))
    CHECK_GL(glBindTexture(GL_TEXTURE_2D, originalImage->getFramebuffer()->getTexture()))
    _filterProgram->setUniformValue("originalImage", 3);
    _filterProgram->setUniformValue("intensity", intensity);
  }
  return Filter::proceed(bUpdateTargets, frameTime);
}
