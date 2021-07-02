//
// Created by xionghao on 2021/7/3.
//

#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <android/log.h>

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "NativeLib", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "NativeLib", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "NativeLib", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "NativeLib", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "NativeLib", __VA_ARGS__)

using namespace std;
using namespace cv;

//void vector_Mat_to_Mat(std::vector<cv::Mat> &v_mat, cv::Mat &mat) {
//    int count = (int) v_mat.size();
//    mat.create(count, 1, CV_32SC2);
//    for (int i = 0; i < count; i++) {
//        long long addr = (long long) new Mat(v_mat[i]);
//        mat.at<Vec<int, 2> >(i, 0) = Vec<int, 2>(addr >> 32, addr & 0xffffffff);
//    }
//}



