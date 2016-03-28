#ifndef FACERECOGNITION_H
#define FACERECOGNITION_H
//#include "opencv2/face.hpp"
#include "opencv2/opencv.hpp"
#include "opencv2/core.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/objdetect.hpp"
#include <iostream>
#include <fstream>
#include <sstream>
using namespace cv;
//using namespace cv::face;
using namespace std;

class FaceRecognition
{
    void read_csv(const string& filename, vector<Mat>& images, vector<int>& labels, char separator);
    bool cutFace(Mat &img);
    void resizeimg(Mat &img);
    string face_cascade_classifier = "C://Users//Dasha//Documents//haarcascade_frontalface_alt2.xml";
    double k1 = 0.2;
    double k2 = 1.2;
public:
    void turnimg(Mat &src);
    FaceRecognition();
    int FaceDetection(string path);
    bool cutFace(Mat &img, string path, string pathout);
    string fn_csv = "C://BD//csv.txt";
    string csv = "C:/BD/csv.txt";
};
#endif //FACERECOGNITION_H
