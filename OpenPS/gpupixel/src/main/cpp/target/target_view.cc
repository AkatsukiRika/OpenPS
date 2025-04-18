/*
 * GPUPixel
 *
 * Created by PixPark on 2021/6/24.
 * Copyright © 2021 PixPark. All rights reserved.
 */

#include "target_view.h"
#include "gpupixel_context.h"
#include "util.h"
#include "filter.h"

USING_NS_GPUPIXEL

TargetView::TargetView()
    : _viewWidth(0),
      _viewHeight(0),
      _fillMode(FillMode::PreserveAspectRatio),
      _displayProgram(0),
      _positionAttribLocation(0),
      _texCoordAttribLocation(0),
      _colorMapUniformLocation(0),
      _mvpMatrixUniformLocation(0) {
  _backgroundColor.r = 0.0;
  _backgroundColor.g = 0.0;
  _backgroundColor.b = 0.0;
  _backgroundColor.a = 0.0;
  init();
}

TargetView::~TargetView() {
  if (_displayProgram) {
    delete _displayProgram;
    _displayProgram = 0;
  }
}

void TargetView::init() {
  _displayProgram = GLProgram::createByShaderString(kDefaultVertexShader,
                                                    kDefaultFragmentShader);
  _positionAttribLocation = _displayProgram->getAttribLocation("position");
  _texCoordAttribLocation =
      _displayProgram->getAttribLocation("inputTextureCoordinate");
  _colorMapUniformLocation =
      _displayProgram->getUniformLocation("textureCoordinate");
  _mvpMatrixUniformLocation =
      _displayProgram->getUniformLocation("mvpMatrix");
  GPUPixelContext::getInstance()->setActiveShaderProgram(_displayProgram);
  CHECK_GL(glEnableVertexAttribArray(_positionAttribLocation));
  CHECK_GL(glEnableVertexAttribArray(_texCoordAttribLocation));
};

void TargetView::setInputFramebuffer(
    std::shared_ptr<Framebuffer> framebuffer,
    RotationMode rotationMode /* = NoRotation*/,
    int texIdx /* = 0*/) {
  std::shared_ptr<Framebuffer> lastInputFramebuffer;
  RotationMode lastInputRotation = NoRotation;
  if (_inputFramebuffers.find(0) != _inputFramebuffers.end()) {
    lastInputFramebuffer = _inputFramebuffers[0].frameBuffer;
    lastInputRotation = _inputFramebuffers[0].rotationMode;
  }

  Target::setInputFramebuffer(framebuffer, rotationMode, texIdx);

  if (lastInputFramebuffer != framebuffer && framebuffer &&
      (!lastInputFramebuffer ||
       !(lastInputFramebuffer->getWidth() == framebuffer->getWidth() &&
         lastInputFramebuffer->getHeight() == framebuffer->getHeight() &&
         lastInputRotation == rotationMode))) {
    _updateDisplayVertices();
  } else if (_isCompare) {
    _updateDisplayVertices();
  }
}

void TargetView::setFillMode(FillMode fillMode) {
  if (_fillMode != fillMode) {
    _fillMode = fillMode;
    _updateDisplayVertices();
  }
}

void TargetView::setMirror(bool mirror) {
  if (_mirror != mirror) {
    _mirror = mirror;
  }
}


void TargetView::onSizeChanged(int width, int height) {
  if (_viewWidth != width || _viewHeight != height) {
    _viewWidth = width;
    _viewHeight = height;
    _updateDisplayVertices();
  }
}

void TargetView::getViewInfo(float *info) {
  info[0] = _viewWidth;
  info[1] = _viewHeight;
  info[2] = _scaledWidth;
  info[3] = _scaledHeight;
}

void TargetView::setMVPMatrix(const Matrix4 &mvpMatrix) {
  _mvpMatrix = mvpMatrix;
}

bool TargetView::updateMatrixState() {
  if (_inputFramebuffers.empty() || _inputFramebuffers[0].frameBuffer == nullptr) {
    return false;
  }

  GPUPixelContext::getInstance()->setActiveShaderProgram(_displayProgram);
  CHECK_GL(glUniformMatrix4fv(_mvpMatrixUniformLocation, 1, false, _mvpMatrix.m));

  // 可能需要触发一个最小化的重绘操作
  // 这里我们只更新与 MVP 矩阵相关的部分
  CHECK_GL(glBindFramebuffer(GL_FRAMEBUFFER, 0));
  CHECK_GL(glViewport(0, 0, _viewWidth, _viewHeight));

  CHECK_GL(glActiveTexture(GL_TEXTURE0));
  CHECK_GL(glBindTexture(GL_TEXTURE_2D,
                         _inputFramebuffers[0].frameBuffer->getTexture()));
  CHECK_GL(glUniform1i(_colorMapUniformLocation, 0));

  CHECK_GL(glVertexAttribPointer(_positionAttribLocation, 2, GL_FLOAT, 0, 0,
                                 _displayVertices));
  CHECK_GL(glVertexAttribPointer(
      _texCoordAttribLocation, 2, GL_FLOAT, 0, 0,
      _getTexureCoordinate(_inputFramebuffers[0].rotationMode)));

  CHECK_GL(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4));
  return true;
}

void TargetView::onCompareBegin() {
  _isCompare = true;
}

void TargetView::onCompareEnd() {
  _isCompare = false;
}

void TargetView::update(int64_t frameTime) {
  CHECK_GL(glBindFramebuffer(GL_FRAMEBUFFER, 0));

  CHECK_GL(glViewport(0, 0, _viewWidth, _viewHeight));
  CHECK_GL(glClearColor(_backgroundColor.r, _backgroundColor.g,
                        _backgroundColor.b, _backgroundColor.a));
  CHECK_GL(glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT));

  updateMatrixState();
}

void TargetView::_updateDisplayVertices() {
  if (_inputFramebuffers.find(0) == _inputFramebuffers.end() ||
      _inputFramebuffers[0].frameBuffer == 0) {
    return;
  }

  std::shared_ptr<Framebuffer> inputFramebuffer =
      _inputFramebuffers[0].frameBuffer;
  RotationMode inputRotation = _inputFramebuffers[0].rotationMode;

  int rotatedFramebufferWidth = inputFramebuffer->getWidth();
  int rotatedFramebufferHeight = inputFramebuffer->getHeight();
  if (rotationSwapsSize(inputRotation)) {
    rotatedFramebufferWidth = inputFramebuffer->getHeight();
    rotatedFramebufferHeight = inputFramebuffer->getWidth();
  }

  float framebufferAspectRatio =
      rotatedFramebufferHeight / (float)rotatedFramebufferWidth;
  float viewAspectRatio = _viewHeight / (float)_viewWidth;

  float insetFramebufferWidth = 0.0;
  float insetFramebufferHeight = 0.0;
  if (framebufferAspectRatio > viewAspectRatio) {
    insetFramebufferWidth =
        _viewHeight / (float)rotatedFramebufferHeight * rotatedFramebufferWidth;
    insetFramebufferHeight = _viewHeight;
  } else {
    insetFramebufferWidth = _viewWidth;
    insetFramebufferHeight =
        _viewWidth / (float)rotatedFramebufferWidth * rotatedFramebufferHeight;
  }

  bool isNotInit = _initialScaledWidth == -1 && _initialScaledHeight == -1;

  float scaledWidth = 1.0;
  float scaledHeight = 1.0;
  if (_fillMode == FillMode::PreserveAspectRatio) {
    scaledWidth = insetFramebufferWidth / _viewWidth;
    scaledHeight = insetFramebufferHeight / _viewHeight;
  } else if (_fillMode == FillMode::PreserveAspectRatioAndFill) {
    scaledWidth = _viewWidth / insetFramebufferHeight;
    scaledHeight = _viewHeight / insetFramebufferWidth;
  }

  if (_isCompare && !isNotInit) {
    _displayVertices[0] = -_initialScaledWidth;
    _displayVertices[1] = -_initialScaledHeight;
    _displayVertices[2] = _initialScaledWidth;
    _displayVertices[3] = -_initialScaledHeight;
    _displayVertices[4] = -_initialScaledWidth;
    _displayVertices[5] = _initialScaledHeight;
    _displayVertices[6] = _initialScaledWidth;
    _displayVertices[7] = _initialScaledHeight;
  } else {
    _displayVertices[0] = -scaledWidth;
    _displayVertices[1] = -scaledHeight;
    _displayVertices[2] = scaledWidth;
    _displayVertices[3] = -scaledHeight;
    _displayVertices[4] = -scaledWidth;
    _displayVertices[5] = scaledHeight;
    _displayVertices[6] = scaledWidth;
    _displayVertices[7] = scaledHeight;
  }
  Util::Log("TargetView", "updateDisplayVertices, isNotInit=%d, isCompare=%d", isNotInit, _isCompare);

  _scaledWidth = scaledWidth;
  _scaledHeight = scaledHeight;
  if (isNotInit) {
    _initialScaledWidth = scaledWidth;
    _initialScaledHeight = scaledHeight;
    Util::Log("TargetView", "Set initialScaledWidth=%f, initialScaledHeight=%f", _initialScaledWidth, _initialScaledHeight);
  }
}

const GLfloat* TargetView::_getTexureCoordinate(RotationMode rotationMode) {
  static const GLfloat noRotationTextureCoordinates[] = {
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f,
  };

  static const GLfloat rotateRightTextureCoordinates[] = {
    1.0f, 1.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    0.0f, 0.0f,
  };

  static const GLfloat rotateLeftTextureCoordinates[] = {
    0.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 0.0f,
    1.0f, 1.0f,
  };

  static const GLfloat verticalFlipTextureCoordinates[] = {
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f,
  };

  static const GLfloat horizontalFlipTextureCoordinates[] = {
    1.0f, 1.0f,
    0.0f, 1.0f,
    1.0f, 0.0f,
    0.0f, 0.0f,
  };

  static const GLfloat rotateRightVerticalFlipTextureCoordinates[] = {
    1.0f, 0.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    0.0f, 1.0f,
  };

  static const GLfloat rotateRightHorizontalFlipTextureCoordinates[] = {
    0.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 1.0f,
    1.0f, 0.0f,
  };

  static const GLfloat rotate180TextureCoordinates[] = {
    1.0f, 0.0f,
    0.0f, 0.0f,
    1.0f, 1.0f,
    0.0f, 1.0f,
  };

  switch (rotationMode) {
    case NoRotation: {
      if(_mirror) {
        return horizontalFlipTextureCoordinates;
      } else {
        return noRotationTextureCoordinates;
      }
    }
    case RotateLeft:
      return rotateLeftTextureCoordinates;
    case RotateRight:
      return rotateRightTextureCoordinates;
    case FlipVertical:
      return verticalFlipTextureCoordinates;
    case FlipHorizontal:
      return horizontalFlipTextureCoordinates;
    case RotateRightFlipVertical:
      return rotateRightVerticalFlipTextureCoordinates;
    case RotateRightFlipHorizontal:
      return rotateRightHorizontalFlipTextureCoordinates;
    case Rotate180:
      return rotate180TextureCoordinates;
  }
}
