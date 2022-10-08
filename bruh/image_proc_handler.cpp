#include "image_proc_handler.h"
#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"
#include "image_write.h"

int imgscount = 0;

using namespace cv;
using namespace cv::face;

HttpResponse img_proc_1(HttpRequest request, CascadeClassifier& cc, Ptr<EigenFaceRecognizer> faceRecogn, std::map<int, std::string> names) {
    std::cout << "got shit image to process " << request.data.size() << std::endl;
    int x, y, ch;
    unsigned char* imgData = stbi_load_from_memory((const unsigned char*) request.data.c_str(), request.data.size(), &x, &y, &ch, 3);
    std::cout << "got shit image to process 2 " << x << " " << y << " " << ch << std::endl;
    Mat img, gray;
    if(!(x <= 0 || x >= 8192 || y <= 0 || y >= 8192)) {
        img.create(y, x, CV_8UC3);
        memcpy(img.ptr(), imgData, x * y * 3);
        //cvtColor(img, img, COLOR_RGBA2RGB);
        std::cout << "got shit image to process 3 " << x << " " << y << " " << ch << std::endl;

        cvtColor(img, img, COLOR_RGB2BGR);
        cvtColor(img, gray, COLOR_RGB2GRAY);
        std::vector<Rect2i> res;
        cc.detectMultiScale(gray, res, 1.01, 3, CASCADE_SCALE_IMAGE | CASCADE_DO_CANNY_PRUNING, Size(64, 64));
        for(auto bb : res) {
            if(bb.x < 0) bb.x = 0;
            if(bb.y < 0) bb.y = 0;
            if(bb.width >= img.cols - bb.x) bb.width = img.cols - bb.x;
            if(bb.height >= img.rows - bb.y) bb.height = img.rows - bb.y;

            Mat amogus = gray(bb);
            resize(amogus, amogus, Size(256, 256));
            int label = -1;
            double conf = 0;
            faceRecogn->predict(amogus, label, conf);

            if(conf > 10000) {
                label = -1;
                conf = 10000;
            }

            std::cout << "Label: " << label << "; confidence: " << conf << std::endl;
            Mat amogusrgb = img(bb);
            resize(amogusrgb, amogusrgb, Size(256, 256));
            imwrite("./dataset_new/" + std::to_string(imgscount++) + ".png", amogusrgb);
            rectangle(img, bb, Scalar(0, 0, 255), 5);
            putText(img, names[label], Point(bb.x + 8, bb.y + 24), FONT_HERSHEY_SIMPLEX, 0.6, Scalar(0, 0, 255), 1);
            putText(img, std::to_string((int)conf), Point(bb.x + 8, bb.y + 48), FONT_HERSHEY_SIMPLEX, 0.6, Scalar(0, 0, 255), 1);
        }
        cvtColor(img, img, COLOR_RGB2BGR);

        ImageWriteData wrdata = image_write_png(img.ptr(), img.cols, img.rows);
        std::string outS(wrdata.data, wrdata.data + wrdata.len);
        free(wrdata.data);

        HttpResponse resp;
        resp.data = outS;
        resp.statusCode = 200;
        resp.status = "OK";
        resp.headers["Content-Type"] = "image/png";
        return resp;
    }
    if(imgData != NULL) free(imgData);
    HttpResponse resp;
    resp.data = "Server has just cringed";
    resp.statusCode = 502;
    resp.status = "Bad request";
    resp.headers["Content-Type"] = "text/plain";
    return resp;
}

HttpResponse img_proc_2(HttpRequest request, CascadeClassifier& cc, Ptr<EigenFaceRecognizer> faceRecogn, std::map<int, std::string> names) {
    std::cout << "got shit image to process " << request.data.size() << std::endl;
    int x, y, ch;
    unsigned char* imgData = stbi_load_from_memory((const unsigned char*) request.data.c_str(), request.data.size(), &x, &y, &ch, 3);
    std::cout << "got shit image to process 2 " << x << " " << y << " " << ch << std::endl;
    Mat img, gray;
    if(!(x <= 0 || x >= 8192 || y <= 0 || y >= 8192)) {
        img.create(y, x, CV_8UC3);
        memcpy(img.ptr(), imgData, x * y * 3);
        //cvtColor(img, img, COLOR_RGBA2RGB);
        std::cout << "got shit image to process 3 " << x << " " << y << " " << ch << std::endl;

        std::vector<std::pair<std::pair<int, double>, Rect>> f;

        cvtColor(img, img, COLOR_RGB2BGR);
        cvtColor(img, gray, COLOR_RGB2GRAY);
        std::vector<Rect2i> res;
        cc.detectMultiScale(gray, res, 1.01, 3, CASCADE_SCALE_IMAGE | CASCADE_DO_CANNY_PRUNING, Size(64, 64));
        for(auto bb : res) {
            Point2i center(bb.x + bb.width / 2, bb.y + bb.height / 2);
            bb.x -= (center.x - bb.x) / 8;
            bb.y -= (center.y - bb.y) / 8;
            bb.width = (center.x - bb.x) * 2;
            bb.height = (center.y - bb.y) * 2;
            if(bb.x < 0) bb.x = 0;
            if(bb.y < 0) bb.y = 0;
            if(bb.width >= img.cols - bb.x) bb.width = img.cols - bb.x;
            if(bb.height >= img.rows - bb.y) bb.height = img.rows - bb.y;

            Mat amogus = gray(bb);
            resize(amogus, amogus, Size(256, 256));
            int label = -1;
            double conf = 0;
            faceRecogn->predict(amogus, label, conf);

            if(conf > 10000) {
                label = -1;
                conf = 10000;
            }

            std::cout << "Label: " << label << "; confidence: " << conf << std::endl;
            f.push_back(std::make_pair(std::make_pair(label, conf), bb));

            Mat amogusrgb = img(bb);
            resize(amogusrgb, amogusrgb, Size(256, 256));
            imwrite("./dataset_new/" + std::to_string(imgscount++) + ".png", amogusrgb);
        }

        std::stringstream ss;
        ss << "[\n";
        int i = 0;
        for(auto ff : f) {
            ss << "{";
            ss << "\"x\":" << ff.second.x << ", ";
            ss << "\"y\":" << ff.second.y << ", ";
            ss << "\"w\":" << ff.second.width << ", ";
            ss << "\"h\":" << ff.second.height << ", ";
            ss << "\"id\":" << ff.first.first << ", ";
            ss << "\"name\":\"" << names[ff.first.first] << "\", ";
            ss << "\"conf\":" << ff.first.second;
            ss << "}";
            if(i != f.size() - 1) ss << ",";
            i++;
            ss << "\n";
        }
        ss << "]";

        std::cout << ss.str() << std::endl;

        HttpResponse resp;
        resp.data = ss.str();
        resp.statusCode = 200;
        resp.status = "OK";
        resp.headers["Content-Type"] = "text/json";
        return resp;
    }
    if(imgData != NULL) free(imgData);
    HttpResponse resp;
    resp.data = "Server has just cringed";
    resp.statusCode = 502;
    resp.status = "Bad request";
    resp.headers["Content-Type"] = "text/plain";
    return resp;
}