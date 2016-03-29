#include "registercontroller.h"
#include <QDir>
#include <QDebug>
#include <fstream>

RegisterController::RegisterController(QObject* parent)
    : HttpRequestHandler(parent) {
    username="";
    email = "";
    // empty
}

void RegisterController::service(HttpRequest &request, HttpResponse &response) {
    //registration comes in 2 stages: login and email and 3 photos.
    database db("facedetection.sqlite");
    db.connect();
    if (request.getHeader("Content-Type").contains("multipart")){
        //if we recieving multipart data - this is our pictures.
        //we need to make folder in a style of "username+email" and place all 3 pics there
        QTemporaryFile* image;
        int num = db.numofrows();
        std::string dirpath (pathtohuman + std::to_string(num+1));
        qDebug() << "dirpath " << QString::fromStdString(dirpath);
        QDir().mkdir(QString::fromStdString(dirpath));
        db.add(username, email, QString::fromStdString(dirpath));
        int fails = 0;
        //here we need attach dir to db
        for (int i=0; i<3; i++){

            if ( image = request.getUploadedFile("regphoto" + QByteArray::number(i))){
                std::string imgpath (pathtohuman + std::to_string(num+1) +"/"+ std::to_string(i+1) + ".png");
            image->copy(QString::fromStdString(imgpath));
            image->remove();
            std::string pathin(ppathtohuman + std::to_string(num+1) +"//"+ std::to_string(i+1) + ".png");
            std::string pathout(ppathtohuman + std::to_string(num+1) +"//"+ std::to_string(i+1) + ".pgm");
            Mat img = imread(pathin, 0);
            fr.turnimg(img);
            if (!fr.cutFace(img,pathin,pathout))
            {
                //response.write("false", false);
                fails = fails + 1;
                qDebug() << "fails" << fails;
            }
            else
            {
                QFile file(QString::fromStdString(fr.fn_csv));
                QTextStream out(&file);
                if(file.open(QIODevice::Append))
                {
                    std::string str(pathout + ";" + std::to_string(num) );
                    QString strtofile = QString::fromStdString(str);
                      out<<"\r\n"<<strtofile;
                }

                file.close();
            }
            if(fails == 3)
            {
                qDebug() << "where is your faces????";
                response.write("false", false);
            }
            if (i ==2 ){
                QString txt = db.getpath(email) + "/file.txt";
                std::ofstream outfile (txt.toStdString());
                outfile << "Hi new user :)" << std::endl;
                outfile.close();
                response.write("ok",true);
             }
            }
        }
    }
    else {
      //if this is not multipart data - we in a stage 1 -
      //need to recieve name and email, check it in db, and insert in db in case everything is ok
      bool isNewUser = true; //make false when db is added
      username = request.getParameter("username");
      email = request.getParameter("email");
     qDebug() << "username is:" << username;
     qDebug() << "email is:" <<email;
      isNewUser = db.findemail(email);
     //check username and email in db
     //if everything ok: isNewUser = true;
    if (isNewUser == true){
        qDebug() << "new email is:" <<email;
        response.write("true", true);
    }
    else {
        qDebug() << "there is such email:" <<email;
        //isNewUser is false, -> user with the same email already exists in db. Sending false to client
        response.write("false", true);
    }
    }
}

