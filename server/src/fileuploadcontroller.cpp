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
    //we recieved post with picture to check. Lets get it from temp file
    // and copy it to app files folder in order to check it!
   if (QTemporaryFile* image = request.getUploadedFile("phototocheck") ) {
       if (QFile::exists(filepath))
       {
           QFile::remove(filepath);
       }
       bool rc = image->copy(filepath);
       if (rc ){
           //some magic face detection here
           Mat img = imread(filepathocv, 0);
           fr.turnimg(img);
           if (!fr.cutFace(img,filepathocv,filepathocvpgm))
           {
               response.write("false", false);
               return;
           }
           int ff = fr.FaceDetection(filepathocvpgm);
           if (ff > 0)
           {
               //response.write("true", true);
               QString path = ppath + QString::number(ff);
               QString x = ppathx + QString::number(ff)+ "/";
               qDebug() << "path:" << path;
               QString email = db.getemail(path);
               //std::string email = db.getemail(path).toLocal8Bit().constData();
               qDebug() << "email of user: " << email ;
               ifstream F;
               std::string a;
               F.open(pathtocsv, ios::in);
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
               file.open(pathtocsv, ios::app);
               std::string newpath(x.toStdString() + "//" + std::to_string(numofphoto+1) + ".pgm;" + std::to_string(ff-1));
               file<<"\r\n"<<newpath;
               file.close();
               std::string newimg(x.toStdString() + "//" + std::to_string(numofphoto+1) + ".pgm");
               imwrite(newimg, img);
               QByteArray tmp = email.toUtf8().left(1000) ;
               response.write(tmp, true);
           }
           else
           {
               qDebug() << "you are not in the database ";
               response.write("false", false);
           }
       }
   }
   else {
       qDebug() << "doesnt exists";
   }
}

