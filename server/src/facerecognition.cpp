#include "facerecognition.h"
#include "opencv2/opencv.hpp"
#include "opencv2/core.hpp"
#include "opencv2/face.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/objdetect.hpp"
#include "opencv2/videoio/videoio_c.h"
#include "opencv2/highgui/highgui_c.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <QDebug>

using namespace cv;
using namespace cv::face;
using namespace std;

FaceRecognition::FaceRecognition()
{
}

void FaceRecognition::read_csv(const string& filename, vector<Mat>& images, vector<int>& labels, char separator = ';') {
    std::ifstream file(filename.c_str(), ifstream::in);
    if (!file) {
        string error_message = "No valid input file was given, please check the given filename.";
        CV_Error(CV_StsBadArg, error_message);
    }
    string line, path, classlabel;
    while (getline(file, line)) {
        stringstream liness(line);
        getline(liness, path, separator);
        getline(liness, classlabel);
        if(!path.empty() && !classlabel.empty()) {
            images.push_back(imread(path, 0));
            labels.push_back(atoi(classlabel.c_str()));
        }
    }
}
//when add new image or download image
bool FaceRecognition::cutFace(Mat &img, string path, string pathout)
{
    CascadeClassifier face_cascade;
    face_cascade.load(face_cascade_classifier);
    std::vector<Rect> faces;
    face_cascade.detectMultiScale(img, faces, 1.1, 3, 0, Size(100, 100) );
    int n = faces.size();
    if (n < 1)
    {
        return false;
    }
    else
    {
        int x1, x2, y1, y2;
        if(faces[0].x-k1*faces[0].x > 0)
            x1 = faces[0].x-k1*faces[0].x;
        else
            x1 = 1;
        if(faces[0].y-k1*faces[0].y > 0)
            y1 = faces[0].y-k1*faces[0].y;
        else
            y1 = 1;
        if(faces[0].x + faces[0].width*k2 < img.cols)
            x2 = faces[0].x + faces[0].width*k2;
        else
            x2 = img.rows-1;
        if(faces[0].y + faces[0].height*k2 < img.rows)
            y2 = faces[0].y + faces[0].height*k2;
        else
            y2 = img.cols-1;
        Point pt1(x1, y1); // Display detected faces on main window - live stream from camera
        Point pt2(x2, y2);
        rectangle(img, pt1, pt2, Scalar(0, 255, 0), 2, 8, 0);
        int p1,p2;
        if(pt2.x-pt1.x > img.cols)
           p1 = img.cols-1;
        else p1 = pt2.x-pt1.x;
        if (pt2.y - pt1.y > img.rows)
            p2 = img.rows-1;
        else
            p2 = pt2.y - pt1.y;
        if(pt1.x + p1 > img.cols)
            pt1.x = 0;
        if(pt1.y + p2 > img.rows)
            pt1.y = 0;
        Rect myROI(pt1.x, pt1.y, p1, p2);
        Mat img1 = img(myROI);
        resizeimg(img1);
        imwrite(pathout, img1);

        return true;
    }
}

void FaceRecognition::resizeimg(Mat &img)
{
    Mat img1(512,512,0);
    resize(img, img1, Size(512,512),0,0);
    img = img1;
}

int FaceRecognition::FaceDetection(string path)
{
    // These vectors hold the images and corresponding labels.
    vector<Mat> images;
    vector<int> labels;
    // Read in the data. This can fail if no valid
    // input filename is given.
    try {
        read_csv(fn_csv, images, labels);
    } catch (cv::Exception& e) {
        cerr << "Error opening file \"" << fn_csv << "\". Reason: " << e.msg << endl;
        // nothing more we can do
        exit(1);
    }
    // Quit if there are not enough images for this demo.
    if(images.size() <= 1) {
        string error_message = "This demo needs at least 2 images to work. Please add more images to your data set!";
        CV_Error(CV_StsError, error_message);
    }
    Mat testSample = imread(path, 0);
    images.pop_back();
    labels.pop_back();
    Ptr<FaceRecognizer> model = createLBPHFaceRecognizer(1,8,8,8,123.0);
    model->train(images, labels);
    int predicted_label = -1;
    double predicted_confidence = 0.0;
    model->predict(testSample, predicted_label, predicted_confidence);
    string result_message = format("Predicted class = %d", predicted_label+1);
    string fileresult(pathtoimg+ to_string(predicted_label+1) + "//1.pgm");
    cout << result_message << endl;
    if(predicted_label<0)
        return 0;
    else
        return predicted_label+1;
}

void FaceRecognition::turnimg(Mat &src)
{
    double angle = 90;
    // get rotation matrix for rotating the image around its center
        cv::Point2f center(src.cols/2.0, src.rows/2.0);
        cv::Mat rot = cv::getRotationMatrix2D(center, angle, 1.0);
        // determine bounding rectangle
        cv::Rect bbox = cv::RotatedRect(center,src.size(), angle).boundingRect();
        // adjust transformation matrix
        rot.at<double>(0,2) += bbox.width/2.0 - center.x;
        rot.at<double>(1,2) += bbox.height/2.0 - center.y;
        cv::warpAffine(src, src, rot, bbox.size());
}
