#ifndef FILEUPLOADCONTROLLER_H
#define FILEUPLOADCONTROLLER_H

#include "httpserver/httprequest.h"
#include "httpserver/httpresponse.h"
#include "httpserver/httprequesthandler.h"
#include "src/facerecognition.h"
#include "src/database.h"
class FileUploadController : public HttpRequestHandler {
    Q_OBJECT
    Q_DISABLE_COPY(FileUploadController)
    FaceRecognition fr;
    const char* filepath = "C:/Users/Dasha/Documents/file.png";
    const char* filepathocv = "C://Users//Dasha//Documents//file.png";
    const char* filepathocvpgm = "C://Users//Dasha//Documents//file.pgm";
    const char* ppath = "C:/BD/s";
    const char* ppathx = "C://BD//s";
    const char* pathtocsv = "C:/BD/csv.txt";
public:

    /** Constructor */
    FileUploadController();
    //database db;
    /** Generates the response */
    void service(HttpRequest& request, HttpResponse& response);
};

#endif // FILEUPLOADCONTROLLER_H
