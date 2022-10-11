#include <opencv2/opencv.hpp>
#include <cstdio>

using namespace std;
using namespace cv;

pair<int, int> directions[] {
    {0, -1}, {1, -1}, {1, 0}, {1, 1},
    {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, 0}
};

struct Vector2f {
public:
    float x, y;
    Vector2f() : x(0), y(0) {}
    Vector2f(float x, float y) : x(x), y(y) {}
};

int main() {
    int divSize = 16;
    //VideoCapture cap(0);

    while(true) {
        Mat img, gray;
        img = imread("./image.jpeg");
        resize(img, img, Size(512, 512));
        //cap >> img;
        cvtColor(img, gray, COLOR_BGR2GRAY);
        gray = gray(Rect(0, 0, gray.cols - (gray.cols % divSize), gray.rows - (gray.rows % divSize)));
        imshow("raw", img);
        imshow("gray", gray);

        vector<vector<char>> hog_1px(gray.cols, vector<char>(gray.rows, 0));

        for(int x = 1; x < gray.cols - 1; x++) {
            for(int y = 1; y < gray.rows - 1; y++) {
                int targDir = 0, targRazn = -9999;
                for(int dir = 0; dir < 8; dir++) {
                    int razn = *gray.ptr(y, x) - *gray.ptr(y + directions[dir].second, x + directions[dir].first);
                    if(razn > targRazn) {
                        targRazn = razn;
                        targDir = dir;
                    }
                }
                hog_1px[x][y] = targDir;
                if(targRazn == 0) hog_1px[x][y] = 8;
            }
        }

        vector<vector<Vector2f>> hog_16px(gray.cols / divSize, vector<Vector2f>(gray.rows / divSize));

        for(int sx = 0; sx < gray.cols; sx += divSize) {
            for(int sy = 0; sy < gray.rows; sy += divSize) {
                float avgX = 0, avgY = 0;
                for(int x = 0; x < divSize; x++) {
                    for(int y = 0; y < divSize; y++) {
                        avgX += directions[hog_1px[sx + x][sy + y]].first;
                        avgY += directions[hog_1px[sx + x][sy + y]].second;
                    }
                }
                // float dist = sqrt(avgX * avgX + avgY * avgY);
                avgX /= divSize * divSize;
                avgY /= divSize * divSize;
                hog_16px[sx / divSize][sy / divSize] = Vector2f(avgX, avgY);
            }
        }

        Mat out(Size(gray.cols, gray.rows), CV_8UC1);
        rectangle(out, Rect(0, 0, out.cols, out.rows), Scalar(0), 1000);
        
        for(int x = 0; x < gray.cols / divSize; x++) {
            for(int y = 0; y < gray.rows / divSize; y++) {
                int centerX = x * divSize + 8, centerY = y * divSize + 8;
                Vector2f v = hog_16px[x][y];
                v.x *= divSize / 2; v.y *= divSize / 2;
                line(out, Point(centerX, centerY), Point(centerX + v.x, centerY + v.y), Scalar(255), 1);
            }
        }

        imshow("out", out);

        waitKey(0);
    }
}