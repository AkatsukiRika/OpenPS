#include "black_cat_filter.h"

USING_NS_GPUPIXEL

const std::string kBlackCatFragmentShaderString = SHADER_STRING(
    varying highp vec2 textureCoordinate;
    precision highp float;

    uniform sampler2D inputImageTexture;
    uniform sampler2D curve;
    uniform float intensity;

    vec3 rgb2hsv(vec3 c) {
      vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
      vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
      vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

      float d = q.x - min(q.w, q.y);
      float e = 1.0e-10;
      return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
    }

    vec3 hsv2rgb(vec3 c) {
      vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
      vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
      return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
    }

    void main() {
      float GreyVal;
      lowp vec4 textureColor;
      lowp vec4 textureColorOri;
      float xCoordinate = textureCoordinate.x;
      float yCoordinate = textureCoordinate.y;

      highp float redCurveValue;
      highp float greenCurveValue;
      highp float blueCurveValue;
      textureColor = texture2D( inputImageTexture, vec2(xCoordinate, yCoordinate));
      // step1 curve
      redCurveValue = texture2D(curve, vec2(textureColor.r, 0.0)).r;
      greenCurveValue = texture2D(curve, vec2(textureColor.g, 0.0)).g;
      blueCurveValue = texture2D(curve, vec2(textureColor.b, 0.0)).b;


      //textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
      vec3 tColor = vec3(redCurveValue, greenCurveValue, blueCurveValue);
      tColor = rgb2hsv(tColor);

      tColor.g = tColor.g * 1.2;

      float dStrength = 1.0;
      float dSatStrength = 0.3;

      float dGap = 0.0;

      if (tColor.r >= 0.0 && tColor.r < 0.417) {
        tColor.g = tColor.g + (tColor.g * dSatStrength);
      } else if (tColor.r > 0.958 && tColor.r <= 1.0) {
        tColor.g = tColor.g + (tColor.g * dSatStrength);
      } else if (tColor.r >= 0.875 && tColor.r <= 0.958) {
        dGap = abs(tColor.r - 0.875);
        dStrength = (dGap / 0.0833);

        tColor.g = tColor.g + (tColor.g * dSatStrength * dStrength);
      } else if (tColor.r >= 0.0417 && tColor.r <= 0.125) {
        dGap = abs(tColor.r - 0.125);
        dStrength = (dGap / 0.0833);

        tColor.g = tColor.g + (tColor.g * dSatStrength * dStrength);
      }

      tColor = hsv2rgb(tColor);
      tColor = clamp(tColor, 0.0, 1.0);

      redCurveValue = texture2D(curve, vec2(tColor.r, 1.0)).r;
      greenCurveValue = texture2D(curve, vec2(tColor.g, 1.0)).r;
      blueCurveValue = texture2D(curve, vec2(tColor.b, 1.0)).r;

      redCurveValue = texture2D(curve, vec2(redCurveValue, 1.0)).g;
      greenCurveValue = texture2D(curve, vec2(greenCurveValue, 1.0)).g;
      blueCurveValue = texture2D(curve, vec2(blueCurveValue, 1.0)).g;

      textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);

      vec4 originColor = texture2D(inputImageTexture, textureCoordinate);
      gl_FragColor = vec4(mix(originColor.rgb, textureColor.rgb, intensity), 1.0);
    }
);

std::shared_ptr<BlackCatFilter> gpupixel::BlackCatFilter::create() {
  auto ret = std::shared_ptr<BlackCatFilter>(new BlackCatFilter());
  if (ret && !ret->init()) {
    ret.reset();
  }
  return ret;
}

void BlackCatFilter::setIntensity(float newIntensity) {
  intensity = newIntensity;
}

bool BlackCatFilter::init() {
  if (!initWithFragmentShaderString(kBlackCatFragmentShaderString)) {
    return false;
  }
  curveImage = SourceImage::create(Util::getResourcePath("lookup_black_cat.png"));
  return true;
}

bool BlackCatFilter::proceed(bool bUpdateTargets, int64_t frameTime) {
  CHECK_GL(glActiveTexture(GL_TEXTURE3))
  CHECK_GL(glBindTexture(GL_TEXTURE_2D, curveImage->getFramebuffer()->getTexture()))
  _filterProgram->setUniformValue("curve", 3);

  _filterProgram->setUniformValue("intensity", intensity);

  return Filter::proceed(bUpdateTargets, frameTime);
}
