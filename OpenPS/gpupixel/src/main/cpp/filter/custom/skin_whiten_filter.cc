#include "skin_whiten_filter.h"

USING_NS_GPUPIXEL

const std::string kSkinWhitenFragmentShaderString = SHADER_STRING(
    precision highp float;

    uniform sampler2D inputImageTexture;
    uniform sampler2D curve;

    uniform float texelWidthOffset;
    uniform float texelHeightOffset;
    uniform float intensity;

    varying mediump vec2 textureCoordinate;

    const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

    vec4 gaussianBlur(sampler2D sampler) {
      lowp float strength = 1.;
      vec4 color = vec4(0.);
      vec2 step  = vec2(0.);

      color += texture2D(sampler,textureCoordinate)* 0.25449 ;

      step.x = 1.37754 * texelWidthOffset  * strength;
      step.y = 1.37754 * texelHeightOffset * strength;
      color += texture2D(sampler,textureCoordinate+step) * 0.24797;
      color += texture2D(sampler,textureCoordinate-step) * 0.24797;

      step.x = 3.37754 * texelWidthOffset  * strength;
      step.y = 3.37754 * texelHeightOffset * strength;
      color += texture2D(sampler,textureCoordinate+step) * 0.09122;
      color += texture2D(sampler,textureCoordinate-step) * 0.09122;

      step.x = 5.37754 * texelWidthOffset  * strength;
      step.y = 5.37754 * texelHeightOffset * strength;

      color += texture2D(sampler,textureCoordinate+step) * 0.03356;
      color += texture2D(sampler,textureCoordinate-step) * 0.03356;

      return color;
    }

    void main() {
      vec4 blurColor;
      lowp vec4 textureColor;
      lowp float strength = -1.0 / 510.0;

      float xCoordinate = textureCoordinate.x;
      float yCoordinate = textureCoordinate.y;

      lowp float satura = 0.7;
      // naver skin
      textureColor = texture2D(inputImageTexture, textureCoordinate);
      blurColor = gaussianBlur(inputImageTexture);

      //saturation
      lowp float luminance = dot(blurColor.rgb, luminanceWeighting);
      lowp vec3 greyScaleColor = vec3(luminance);

      blurColor = vec4(mix(greyScaleColor, blurColor.rgb, satura), blurColor.w);

      lowp float redCurveValue = texture2D(curve, vec2(textureColor.r, 0.0)).r;
      lowp float greenCurveValue = texture2D(curve, vec2(textureColor.g, 0.0)).r;
      lowp float blueCurveValue = texture2D(curve, vec2(textureColor.b, 0.0)).r;

      redCurveValue = min(1.0, redCurveValue + strength);
      greenCurveValue = min(1.0, greenCurveValue + strength);
      blueCurveValue = min(1.0, blueCurveValue + strength);

      mediump vec4 overlay = blurColor;

      mediump vec4 base = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
      //gl_FragColor = overlay;

      // step4 overlay blending
      mediump float ra;
      if (base.r < 0.5) {
        ra = overlay.r * base.r * 2.0;
      } else {
        ra = 1.0 - ((1.0 - base.r) * (1.0 - overlay.r) * 2.0);
      }

      mediump float ga;
      if (base.g < 0.5) {
        ga = overlay.g * base.g * 2.0;
      } else {
        ga = 1.0 - ((1.0 - base.g) * (1.0 - overlay.g) * 2.0);
      }

      mediump float ba;
      if (base.b < 0.5) {
        ba = overlay.b * base.b * 2.0;
      } else {
        ba = 1.0 - ((1.0 - base.b) * (1.0 - overlay.b) * 2.0);
      }

      textureColor = vec4(ra, ga, ba, 1.0);

      vec4 originColor = texture2D(inputImageTexture, textureCoordinate);
      gl_FragColor = vec4(mix(originColor.rgb, textureColor.rgb, intensity), 1.0);
    }
);

std::shared_ptr<SkinWhitenFilter> SkinWhitenFilter::create() {
  auto ret = std::shared_ptr<SkinWhitenFilter>(new SkinWhitenFilter());
  if (ret && !ret->init()) {
    ret.reset();
  }
  return ret;
}

void SkinWhitenFilter::setIntensity(float newIntensity) {
  intensity = newIntensity;
}

bool SkinWhitenFilter::init() {
  if (!initWithFragmentShaderString(kSkinWhitenFragmentShaderString)) {
    return false;
  }
  curveImage = SourceImage::create(Util::getResourcePath("lookup_skin_whiten.png"));
  return true;
}

void SkinWhitenFilter::setTexelSize(int textureWidth, int textureHeight) {
  texelSizeX = textureWidth;
  texelSizeY = textureHeight;
}

bool SkinWhitenFilter::proceed(bool bUpdateTargets, int64_t frameTime) {
  CHECK_GL(glActiveTexture(GL_TEXTURE3))
  CHECK_GL(glBindTexture(GL_TEXTURE_2D, curveImage->getFramebuffer()->getTexture()))
  _filterProgram->setUniformValue("curve", 3);
  _filterProgram->setUniformValue("texelWidthOffset", 1.0f / texelSizeX);
  _filterProgram->setUniformValue("texelHeightOffset", 1.0f / texelSizeY);
  _filterProgram->setUniformValue("intensity", intensity);
  return Filter::proceed(bUpdateTargets, frameTime);
}
