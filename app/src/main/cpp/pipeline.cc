#include "pipeline.h"
#include <iostream>
#include <android/log.h> // 引入日志库
#include <algorithm>     // for min, max

#define LOG_TAG "PaddleDebug"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// 【修改后的安全裁剪函数】
cv::Mat GetRotateCropImage(const cv::Mat &srcimage, std::vector<std::vector<int>> box) {
    // 1. 安全检查：框的点数必须是 4
    if (box.size() != 4) {
        LOGE("❌ Skip box: Invalid point size %d", (int) box.size());
        return cv::Mat();
    }

    // 2. 打印坐标，找出是谁在搞鬼
     LOGE("Box Coords: (%d,%d), (%d,%d), (%d,%d), (%d,%d)",
          box[0][0], box[0][1], box[1][0], box[1][1],
          box[2][0], box[2][1], box[3][0], box[3][1]);

    std::vector<std::vector<int>> points = box;
    int x_collect[4] = {box[0][0], box[1][0], box[2][0], box[3][0]};
    int y_collect[4] = {box[0][1], box[1][1], box[2][1], box[3][1]};

    int left = int(*std::min_element(x_collect, x_collect + 4));
    int right = int(*std::max_element(x_collect, x_collect + 4));
    int top = int(*std::min_element(y_collect, y_collect + 4));
    int bottom = int(*std::max_element(y_collect, y_collect + 4));

    // 3. 修正坐标越界 (Clip to image bounds)
    left = std::max(0, left);
    top = std::max(0, top);
    right = std::min(srcimage.cols, right);
    bottom = std::min(srcimage.rows, bottom);

    // 4. 检查裁剪区域是否有效
    if (right <= left || bottom <= top) {
        // 这种框通常是误检，直接丢弃，不要强行处理
        LOGE("⚠️ Skip invalid crop area: w=%d, h=%d", right - left, bottom - top);
        return cv::Mat();
    }

    cv::Mat img_crop;
    // 使用 clone 确保内存连续，防止 ROI 引用导致的潜在问题
    srcimage(cv::Rect(left, top, right - left, bottom - top)).copyTo(img_crop);

    cv::imwrite("/storage/emulated/0/Android/data/in.co.washing_machine.mushroomscanner/files/debug_images/crop_step.jpg", img_crop);

    for (int i = 0; i < points.size(); i++) {
        points[i][0] -= left;
        points[i][1] -= top;
    }

    // 计算透视变换后的宽高
    float dist01 = pow(points[0][0] - points[1][0], 2) + pow(points[0][1] - points[1][1], 2);
    float dist03 = pow(points[0][0] - points[3][0], 2) + pow(points[0][1] - points[3][1], 2);
    int img_crop_width = static_cast<int>(sqrt(dist01));
    int img_crop_height = static_cast<int>(sqrt(dist03));

    // 5. 检查目标尺寸是否离谱
    // 如果一个字框算出几千像素宽，或者为0，肯定有问题
    if (img_crop_width <= 0 || img_crop_height <= 0 || img_crop_width > 5000 ||
        img_crop_height > 5000) {
        LOGE("⚠️ Skip invalid warp size: w=%d, h=%d", img_crop_width, img_crop_height);
        return cv::Mat();
    }

    cv::Point2f pts_std[4];
    pts_std[0] = cv::Point2f(0., 0.);
    pts_std[1] = cv::Point2f(img_crop_width, 0.);
    pts_std[2] = cv::Point2f(img_crop_width, img_crop_height);
    pts_std[3] = cv::Point2f(0.f, img_crop_height);

    cv::Point2f pointsf[4];
    pointsf[0] = cv::Point2f(points[0][0], points[0][1]);
    pointsf[1] = cv::Point2f(points[1][0], points[1][1]);
    pointsf[2] = cv::Point2f(points[2][0], points[2][1]);
    pointsf[3] = cv::Point2f(points[3][0], points[3][1]);

    // 6. 获取变换矩阵并检查
    cv::Mat M = cv::getPerspectiveTransform(pointsf, pts_std);
    if (M.empty()) {
        LOGE("⚠️ Skip: Perspective Matrix is empty");
        return cv::Mat();
    }

    cv::Mat dst_img;
    try {
        // 7. 最后的防线：捕获 OpenCV 内部异常
        cv::warpPerspective(img_crop, dst_img, M,
                            cv::Size(img_crop_width, img_crop_height),
                            cv::BORDER_REPLICATE);
    } catch (const cv::Exception &e) {
        LOGE("❌ OpenCV warpPerspective Exception: %s", e.what());
        return cv::Mat();
    } catch (...) {
        LOGE("❌ Unknown Crash in warpPerspective");
        return cv::Mat();
    }

    const float ratio = 1.5;
    if (static_cast<float>(dst_img.rows) >= static_cast<float>(dst_img.cols) * ratio) {
        cv::Mat srcCopy = cv::Mat(dst_img.rows, dst_img.cols, dst_img.depth());
        cv::transpose(dst_img, srcCopy);
        cv::flip(srcCopy, srcCopy, 0);
        return srcCopy;
    } else {
        return dst_img;
    }
}

std::vector<std::string> ReadDict(std::string path) {
    std::ifstream in(path);
    std::string filename;
    std::string line;
    std::vector<std::string> m_vec;
    if (in) {
        while (getline(in, line)) {
            m_vec.push_back(line);
        }
    } else {
        std::cout << "no such file" << std::endl;
    }
    return m_vec;
}

std::vector<std::string> split(const std::string &str,
                               const std::string &delim) {
    std::vector<std::string> res;
    if ("" == str)
        return res;
    char *strs = new char[str.length() + 1];
    std::strcpy(strs, str.c_str());

    char *d = new char[delim.length() + 1];
    std::strcpy(d, delim.c_str());

    char *p = std::strtok(strs, d);
    while (p) {
        std::string s = p;
        res.push_back(s);
        p = std::strtok(NULL, d);
    }

    return res;
}

std::map<std::string, double> LoadConfigTxt(std::string config_path) {
    auto config = ReadDict(config_path);

    std::map<std::string, double> dict;
    for (int i = 0; i < config.size(); i++) {
        std::vector<std::string> res = split(config[i], " ");
        dict[res[0]] = stod(res[1]);
    }
    return dict;
}

// 构造函数
Pipeline::Pipeline(const std::string &detModelDir,
                   const std::string &clsModelDir,
                   const std::string &recModelDir,
                   const std::string &cpuPowerMode, const int cpuThreadNum,
                   const std::string &config_path,
                   const std::string &dict_path) {

    Config_ = LoadConfigTxt(config_path);
    charactor_dict_ = ReadDict(dict_path);
    charactor_dict_.insert(charactor_dict_.begin(), "#");
    charactor_dict_.push_back(" ");

    if (!clsModelDir.empty()) {
        clsPredictor_.reset(new ClsPredictor(clsModelDir, cpuThreadNum, cpuPowerMode));
    }
    detPredictor_.reset(new DetPredictor(detModelDir, cpuThreadNum, cpuPowerMode));
    recPredictor_.reset(new RecPredictor(recModelDir, cpuThreadNum, cpuPowerMode));
}

// RunOcr 函数
std::vector<OcrResultCpp> Pipeline::RunOcr(cv::Mat &srcimg) {
    std::vector<OcrResultCpp> results;

    cv::Mat img_for_det;
    srcimg.copyTo(img_for_det);

    // 1. 检测
    auto boxes = detPredictor_->Predict(img_for_det, Config_, nullptr, nullptr, nullptr);

    // 2. 识别
    cv::Mat crop_img;
    for (int i = boxes.size() - 1; i >= 0; i--) {
        // 调用我们增加过安全检查的函数
        crop_img = GetRotateCropImage(img_for_det, boxes[i]);

        // 【关键修复】如果裁剪结果为空（说明框无效），直接跳过，防止后续崩溃
        if (crop_img.empty()) {
            continue;
        }

        cv::imwrite("/storage/emulated/0/Android/data/in.co.washing_machine.mushroomscanner/files/debug_images/crop_step2.jpg", crop_img);

        // 方向分类
        if (int(Config_["use_direction_classify"]) >= 1 && clsPredictor_) {
            crop_img = clsPredictor_->Predict(crop_img, nullptr, nullptr, nullptr, 0.9);
        }

        // 文字识别
        auto res = recPredictor_->Predict(crop_img, nullptr, nullptr, nullptr, charactor_dict_);

        if (res.second > 0.0f) {
            OcrResultCpp item;
            item.label = res.first;
            item.confidence = res.second;
            item.box = boxes[i];
            results.push_back(item);
        }
    }
    return results;
}
