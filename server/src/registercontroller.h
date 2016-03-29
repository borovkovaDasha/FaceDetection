#ifndef REGISTERCONTROLLER_H
#define REGISTERCONTROLLER_H
#include "httpserver/httprequesthandler.h"
#include <string.h>
#include <QDir>
#include "database.h"
#include "src/facerecognition.h"

class RegisterController : public HttpRequestHandler {
    Q_OBJECT
public:
    RegisterController(QObject* parent=0);
    void service(HttpRequest& request, HttpResponse& response);
private:
    FaceRecognition fr;
    QByteArray username;
    QByteArray email;
    const char* pathtohuman = "C:/BD/s";
    const char* ppathtohuman = "C://BD//s";
};

#endif // REGISTERCONTROLLER_H
