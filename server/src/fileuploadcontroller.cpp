#include "fileuploadcontroller.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include "src/database.h"
#include <QDebug>

using namespace std;

FileUploadController::FileUploadController()
{
}

void FileUploadController::service(HttpRequest& request, HttpResponse& response)
{
    database db("facedetection.sqlite");
    db.connect();
    qDebug()<<"num " << db.numofrows();
    //we recieved post with picture to check. Lets get it from temp file
    // and copy it to app files folder in order to check it!
   if (QTemporaryFile* image = request.getUploadedFile("phototocheck") ) {
       if (QFile::exists(filepath))
       {
           QFile::remove(filepath);
       }
       bool rc = image->copy(filepath);
       qDebug() << "result is:" << rc ;
       if (rc ){
           //some magic face detection here
           Mat img = imread(filepathocv, 0);
           fr.turnimg(img);
           if (!fr.cutFace(img,filepathocv,filepathocvpgm))
           {
               response.write("false", false);
               qDebug() << "return false";
               return;
           }
           int ff = fr.FaceDetection(filepathocvpgm);
           if (ff > 0)
           {
               //response.write("true", true);
               QString path = ppath + QString::number(ff);
               QString x = "C://BD//s" + QString::number(ff);
               qDebug() << "path:" << path;
               QString email = db.getemail(path);
               //std::string email = db.getemail(path).toLocal8Bit().constData();
               qDebug() << "it is email " << email ;
               //cout << "email:" << email ;
               ifstream F;
               std::string a;
               F.open("C:/BD/csv.txt", ios::in);
               int numofphoto = 0;
               int i = 0;
               int lastnum = 0;
               while (!F.eof())
               {
                    i++;
                    //чтение очередного значения из потока F в переменную a
                    F>>a;
                    QString str = QString::fromStdString(a);
                    if (str.contains(x))
                    {
                        numofphoto++;
                        lastnum = i;
                    }
               }
               F.close();
               ofstream file;
               file.open("C:/BD/csv.txt", ios::app);
               std::string newpath(x.toStdString() + "//" + std::to_string(numofphoto+1) + ".pgm;" + std::to_string(ff-1));
               file<<"\r\n"<<newpath;
               file.close();
               std::string newimg(x.toStdString() + "//" + std::to_string(numofphoto+1) + ".pgm");
               imwrite(newimg, img);
           }
           else
           {
               qDebug() << "you are not in the database ";
               response.write("false", false);
           }
           //response.write("true", true);
           //qDebug() << "face:" << ff ;
           //or false, true if face is not detected
       }
   }
   else {
       qDebug() << "doesnt exists";
   }
}

