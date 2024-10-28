#include "fairy_tale_filter.h"
#include "gpupixel_context.h"

USING_NS_GPUPIXEL

const std::string kLookupVertexShaderString = SHADER_STRING(
    attribute vec4 position;
    attribute vec4 inputTextureCoordinate;

    varying vec2 textureCoordinate;

    void main() {
        gl_Position = position;
        textureCoordinate = inputTextureCoordinate.xy;
    }
);

const std::string kLookupFragmentShaderString = SHADER_STRING(
    varying highp vec2 textureCoordinate;
    uniform sampler2D inputImageTexture;
    uniform sampler2D lookupImageTexture;
    uniform lowp float intensity;

    void main() {
        lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);

        mediump float blueColor = textureColor.b * 63.0;

        mediump vec2 quad1;
        quad1.y = floor(floor(blueColor) / 8.0);
        quad1.x = floor(blueColor) - (quad1.y * 8.0);

        mediump vec2 quad2;
        quad2.y = floor(ceil(blueColor) / 8.0);
        quad2.x = ceil(blueColor) - (quad2.y * 8.0);

        highp vec2 texPos1;
        texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
        texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

        highp vec2 texPos2;
        texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
        texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

        lowp vec4 newColor1 = texture2D(lookupImageTexture, texPos1);
        lowp vec4 newColor2 = texture2D(lookupImageTexture, texPos2);

        lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
        gl_FragColor = vec4(mix(textureColor.rgb, newColor.rgb, intensity), textureColor.w);
    }
);

std::shared_ptr<FairyTaleFilter> FairyTaleFilter::create() {
  auto ret = std::shared_ptr<FairyTaleFilter>(new FairyTaleFilter());
  if (ret && !ret->init()) {
    ret.reset();
  }
  return ret;
}

bool FairyTaleFilter::init() {
  if (!initWithShaderString(kLookupVertexShaderString, kLookupFragmentShaderString)) {
    return false;
  }
  fairyTaleImage = SourceImage::create(Util::getResourcePath("lookup_fairy_tale.png"));
  return true;
}

void FairyTaleFilter::setIntensity(float newIntensity) {
  intensity = newIntensity;
}

bool FairyTaleFilter::proceed(bool bUpdateTargets, int64_t frameTime) {
  static const GLfloat imageVertices[] = {
      -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
  };

  GPUPixelContext::getInstance()->setActiveShaderProgram(_filterProgram);
  _framebuffer->active();
  CHECK_GL(glClearColor(_backgroundColor.r, _backgroundColor.g,
                        _backgroundColor.b, _backgroundColor.a));
  CHECK_GL(glClear(GL_COLOR_BUFFER_BIT));

  CHECK_GL(glActiveTexture(GL_TEXTURE2));
  CHECK_GL(glBindTexture(GL_TEXTURE_2D,
                         _inputFramebuffers[0].frameBuffer->getTexture()));
  _filterProgram->setUniformValue("inputImageTexture", 2);

  // texcoord attribute
  GLuint filterTexCoordAttribute =
      _filterProgram->getAttribLocation("inputTextureCoordinate");
  CHECK_GL(glEnableVertexAttribArray(filterTexCoordAttribute));
  CHECK_GL(glVertexAttribPointer(
      filterTexCoordAttribute, 2, GL_FLOAT, 0, 0,
      _getTexureCoordinate(_inputFramebuffers[0].rotationMode)));

  CHECK_GL(glActiveTexture(GL_TEXTURE3));
  CHECK_GL(glBindTexture(GL_TEXTURE_2D,fairyTaleImage->getFramebuffer()->getTexture()));
  _filterProgram->setUniformValue("lookupImageTexture", 3);

  _filterProgram->setUniformValue("intensity", intensity);

  // vertex position
  CHECK_GL(glVertexAttribPointer(_filterPositionAttribute, 2, GL_FLOAT, 0, 0,
                                 imageVertices));

  // draw
  CHECK_GL(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4));

  _framebuffer->inactive();

  return Source::proceed(bUpdateTargets, frameTime);
}
