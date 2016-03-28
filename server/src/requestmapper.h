#ifndef REQUESTMAPPER_H
#define REQUESTMAPPER_H

#include "httpserver/httprequesthandler.h"
#include "httpserver/httpsessionstore.h"
#include "httpserver/staticfilecontroller.h"
#include "fileuploadcontroller.h"
#include "registercontroller.h"
#include "database.h"

class RequestMapper : public HttpRequestHandler {
    Q_OBJECT
public:
    RequestMapper(QObject* parent=0);
    void service(HttpRequest& request, HttpResponse& response);
    static HttpSessionStore* sessionStore;
    static StaticFileController* staticFileController;
private:
    FileUploadController fileUpload;
    RegisterController registerController;


};

#endif // REQUESTMAPPER_H
