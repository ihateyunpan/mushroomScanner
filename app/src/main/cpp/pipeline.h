#pragma once
#include "cls_process.h"
#include "det_process.h"
#include "paddle_api.h"
#include "rec_process.h"
#include <opencv2/core.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>
#include <string>
#include <vector>
#include <jni.h>

using namespace paddle::lite_api;

struct OcrResultCpp {
    std::string label;
    float confidence;
    std::vector<std::vector<int>> box;
};

class Pipeline {
public:
    Pipeline(const std::string &detModelDir, const std::string &clsModelDir,
             const std::string &recModelDir, const std::string &cPUPowerMode,
             const int cPUThreadNum, const std::string &config_path,
             const std::string &dict_path);

    // 只保留这一个核心方法
    std::vector<OcrResultCpp> RunOcr(cv::Mat &srcimg);

private:
    std::map<std::string, double> Config_;
    std::vector<std::string> charactor_dict_;
    std::shared_ptr<ClsPredictor> clsPredictor_;
    std::shared_ptr<DetPredictor> detPredictor_;
    std::shared_ptr<RecPredictor> recPredictor_;
};