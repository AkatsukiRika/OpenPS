# OpenPS (Open Photo Studio)

> An open-source Android image processing application using GPU rendering. GUI is built with Jetpack Compose according to Google's latest development specifications.

## Information

| Key | Value |
| --- | ----- |
| Supported Platforms | Android 6.0 or above |
| Latest Version | 1.4.0 |
| GUI Language | English |

## Update Notes

### Version 1.4.0

- Composition Feature
    - Supports image cropping with various aspect ratios, including original ratio, free ratio, square, rectangle, etc
    - Supports image rotation in 90-degree increments
    - Supports horizontal mirroring and vertical flipping of the image

### Version 1.3.1

- Local AI Model Optimization
    - Local AI models converted to MNN format
    - Package size reduced from 110 MB in the previous version to 79 MB
- Bug Fixes
    - Fixed a bug where the Generate button in the Eliminate module was unresponsive for a period after being clicked on large images
    - Fixed a bug where the Eliminate area, face frame, and actual image area were misaligned
- Performance and Experience Optimization
    - After setting the Photo Size Limit, small images selected from the gallery will not be enlarged
    - Optimized time consumption for comparison after Eliminate operations
    - Optimized time consumption for gesture zoom & move operations when Eliminate and other image operations are stacked

### Version 1.3.0

- AI Elimination Feature
    - Remove unwanted area from images using the MI-GAN model
- UI Update
    - Operation area divided into two modules: `Eliminate Pen` and `Image Effect`
    - Seamless switching between modules is allowed, all effects can be stacked

### Version 1.2.0

- Package Size Optimization
    - Reduced package size to about half of previous version through model quantization
- Performance Optimization
    - Improved scrolling smoothness when first entering built-in gallery
- Filter Updates
    - Added 3 new full-image filters

### Version 1.1.0

- Built-in Photo Gallery
    - Preview support
    - Information display: file type, size and resolution
- Improved Preview Clarity
    - MVP matrix introduced for gesture operations
    - Preview in original size supported
- Custom Image Filters
    - 5 different full-image filter effects
- Bug Fix & Optimization
    - Undo / redo optimization

## Introduction

The GUI layer is built with Jetpack Compose + MVVM architecture for native Android. The rendering layer implements GPU filters based on C++ 11 and OpenGL ES, with built-in open-source face detection and skin segmentation models. It supports various portrait and image editing effects, allowing arbitrary layering of effects and intensity adjustment for each effect. The app supports real-time preview and result saving, with adjustable rendering resolution to ensure smooth performance. During preview, users can zoom in/out and pan the image using gesture controls. The overall workflow is as follows:

### 1. Import Image and Adjust Resolution (Optional)

![img](https://www.tang-ping.top/assets/assets/images/downloads/img_openps_1.webp)

Click the 'Select Photo' button on the home page to open the gallery and choose any image for processing. Click the settings button in the top-right corner to select from 4 resolution limit modes according to your device's performance: 1K, 2K, 4K, and Unlimited.

### 2. Image Processing (Beautify and Adjust Effects)

![img](https://www.tang-ping.top/assets/assets/images/downloads/img_openps_2.webp)

Top Toolbar: Supports exiting the current page, hiding/showing face detection frames, and saving the current processing result to gallery;

Image Area: Displays real-time rendering effects, supports two-finger zoom and single-finger drag gestures, shows face detection frames by default;

Operation Bar: Supports single-step Undo and Redo, and includes a 'Compare Original' button on the right side that temporarily shows the unprocessed original image when held down;

Feature Area: Divided into two main categories - Beautify and Adjust. The beautify features process different areas of the face, while editing features apply to the entire image. For images where no face is detected, beauty features are unavailable, but editing features can be used normally. All effects can be adjusted using sliders to control intensity and can be layered. There are 11 effects in total as follows:

| No. | Name |
| --- | ------- |
| Beautify 1 | Smooth |
| Beautify 2 | White |
| Beautify 3 | Lipstick |
| Beautify 4 | Blusher |
| Beautify 5 | Eye Zoom |
| Beautify 6 | Face Slim |
| Adjust 1 | Contrast |
| Adjust 2 | Exposure |
| Adjust 3 | Saturation |
| Adjust 4 | Sharpen |
| Adjust 5 | Brightness |

### 3. Save Results

![img](https://www.tang-ping.top/assets/assets/images/downloads/img_openps_3.webp)

Click the button in the top-right corner of the editing page to save the processed image to your phone's gallery. From the gallery page, you can return to the home page to edit another photo.

## References

The following open-source projects were referenced, studied, and used during software development. Special thanks to all open-source contributors.

[gpupixel: Cross-platform OpenGL rendering engine based on C++11](https://github.com/pixpark/gpupixel)

[face-parsing.PyTorch: Open-source skin segmentation model](https://github.com/zllrunning/face-parsing.PyTorch)

[MI-GAN: Image inpainting model on mobile devices](https://github.com/Picsart-AI-Research/MI-GAN)

The following technical documentation summarizes the development process for reference:

[Part 1](https://www.tang-ping.top/documents?id=100200)

[Part 2](https://www.tang-ping.top/documents?id=100210)