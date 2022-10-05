#pragma once

#include <iostream>
#include <fstream>
#include <opencv2/opencv.hpp>
#include <opencv2/face.hpp>
#include <string>
#include <map>
#include "http.h"

HttpResponse img_proc_1(HttpRequest request, cv::CascadeClassifier& cc, cv::Ptr<cv::face::EigenFaceRecognizer> faceRecogn, std::map<int, std::string> names);