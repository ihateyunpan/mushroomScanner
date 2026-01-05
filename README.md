>
OCR部分参考[PaddleOcr官方安卓demo](https://github.com/PaddlePaddle/Paddle-Lite-Demo/tree/develop/ocr/android/app/cxx/ppocr_demo)

# Build

* CMAKE：`4.1.2`
* NDK：`21.4.7075529`

## 准备assets

所有asset的版本都是：`arm64-v8a`，在`app/build.gradle.kts`里配置：

```kts
ndk {
    abiFilters.add("arm64-v8a")
}
```

### Paddle动态库

版本：`v2.10`

跑这个脚本【[Paddle-Lite-Demo/libs/download.sh](https://github.com/PaddlePaddle/Paddle-Lite-Demo/blob/226f4b1378712c845537a8a3bd97944f89eb9220/libs/download.sh)
】，下载PaddleLite动态库

会下载下来3个文件夹，把`android/cxx`文件夹复制到`PaddleLite`下面，即最后有`PaddleLite/cxx`

`PaddleLite/cxx/libs/arm64-v8a`保留即可，其他版本本repo用不到

### OCR模型

版本： `v2`

* [config.txt](https://github.com/PaddlePaddle/Paddle-Lite-Demo/blob/226f4b1378712c845537a8a3bd97944f89eb9220/ocr/assets/config.txt)
  ，下载下来
*

跑【[Paddle-Lite-Demo/ocr/assets/download.sh](https://github.com/PaddlePaddle/Paddle-Lite-Demo/blob/226f4b1378712c845537a8a3bd97944f89eb9220/ocr/assets/download.sh)
】，下载模型资源

模型位置：

* `config.txt`: `src/main/assets/config.txt`
* 模型们：
    * `src/main/assets/models`：所有`.nb`模型
    * `src/main/assets/labels`：下载下来的label文件，本repo使用`ppocr_keys_v1.txt`

> 在`initPaddleOcr`里配置了模型位置，注意与你的assets文件夹保持一致
>
> 不使用方向检测时，把`config.txt`里的`use_direction_classify  1`删除

### OpenCV SDK

版本：[v4.5.5](https://sourceforge.net/projects/opencvlibrary/files/4.5.5/opencv-4.5.5-android-sdk.zip/download)

* 注意要找跟Paddle动态库的NDK版本能兼容的OpenCV版本，否则会报错

下载完后，把`sdk/native`文件夹，搬到`app/OpenCV/sdk/native`位置

# 原理

使用C++库，用JNL做Binding，调用C++库的glue代码在`src/main/cpp/Native.cc`里
