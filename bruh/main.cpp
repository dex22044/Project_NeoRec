#include <opencv2/opencv.hpp>
#include <opencv2/face.hpp>
#include <iostream>
#include <fstream>
#include <unistd.h>
#include "http.h"
#include <sys/socket.h>
#include <arpa/inet.h>
#include <cassert>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <sys/types.h>
#include <filesystem>
#include <map>
#include "image_proc_handler.h"

using namespace cv;
using namespace cv::face;

int bufPtr = 0;
char* buf;

int main() {
    CascadeClassifier cc("./haarcascade_frontalface_alt.xml");
    Ptr<EigenFaceRecognizer> faceRecogn = EigenFaceRecognizer::create(80);
    std::vector<Mat> faces;
    std::vector<int> labels;

    std::map<int, std::string> names;
    names[-1] = "???";

    int cntNames = 0;
    for(auto& fIt : std::filesystem::directory_iterator("./training_data/")) {
        for(auto& imgFileIt : std::filesystem::directory_iterator(fIt.path())) {
            faces.push_back(imread(imgFileIt.path(), IMREAD_GRAYSCALE));
            labels.push_back(cntNames);
            std::cout << imgFileIt.path() << std::endl;
        }
        names[cntNames] = std::filesystem::path(fIt.path()).filename();
        cntNames++;
    }

    faceRecogn->train(faces, labels);

    int servSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    int reuseAddr = 1;
    assert(setsockopt(servSock, SOL_SOCKET, SO_REUSEADDR, &reuseAddr, sizeof(int)) >= 0);
    sockaddr_in sa = {0};
    sa.sin_family = AF_INET;
    sa.sin_port = htons(8091);
    sa.sin_addr.s_addr = INADDR_ANY;
    assert(bind(servSock, (sockaddr*)&sa, sizeof(sockaddr_in)) == 0);
    assert(listen(servSock, 10000) == 0);

    int flags = fcntl(servSock, F_GETFL, 0);
    flags |= O_NONBLOCK;
    fcntl(servSock, F_SETFL, flags);

    Mat img, gray;
    int imgscount = 0;
    buf = (char*)malloc(32 * 1024 * 1024);
    int imgcnt = 0;
    while(true) {
        int clientSock = accept(servSock, NULL, NULL);
        if(clientSock < 0) {
            waitKey(1);
            continue;
        }
        int flags = fcntl(clientSock, F_GETFL, 0);
        flags |= O_NONBLOCK;
        fcntl(clientSock, F_SETFL, flags);
        usleep(100000);
        int totalRd = 0;
        int rd = 0;
        while((rd = recv(clientSock, buf + totalRd, 32768, 0)) > 0) {
            totalRd += rd;
            usleep(1000);
        }
        std::string s(buf, buf + totalRd);
        HttpRequest req = ParseRequest(s);
        for(auto h : req.headers) {
            std::cout << "HEADER " << h.first << " = " << h.second << std::endl;
        }

        if(req.path == "/") {
            std::ifstream ifs("./index.html");
            ifs.seekg(0, std::ifstream::seekdir::_S_end);
            int len = ifs.tellg();
            ifs.seekg(0, std::ifstream::seekdir::_S_beg);
            ifs.read(buf, len);

            std::string outS(buf, buf + len);
            HttpResponse resp;
            resp.data = outS;
            resp.statusCode = 200;
            resp.status = "OK";
            resp.headers["Content-Type"] = "text/html";
            std::string respd = CreateHttpResponse(resp);
            send(clientSock, respd.c_str(), respd.size(), 0);
        }

        if(req.path == "/process_1") {
            std::string respd = CreateHttpResponse(img_proc_1(req, cc, faceRecogn, names));
            send(clientSock, respd.c_str(), respd.size(), 0);
        }

        if(req.path == "/process_2") {
            std::string respd = CreateHttpResponse(img_proc_2(req, cc, faceRecogn, names));
            send(clientSock, respd.c_str(), respd.size(), 0);
        }

        close(clientSock);
    }
}
