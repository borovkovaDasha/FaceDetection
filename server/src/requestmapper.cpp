#include "requestmapper.h"
#include "httpserver/httpsession.h"

HttpSessionStore* RequestMapper::sessionStore=0;
 StaticFileController* RequestMapper::staticFileController=0;

RequestMapper::RequestMapper(QObject* parent)
    : HttpRequestHandler(parent) {
    // empty
}

void RequestMapper::service(HttpRequest& request, HttpResponse& response) {
    QByteArray path=request.getPath();
    qDebug("RequestMapper: path=%s",path.data());

    if (path == "/check"){
        fileUpload.service(request, response);

    }
    else if (path == "/register"){
        registerController.service(request, response);

    }
    else if (path == "/sendtext"){
        notepadControl.service(request, response);

    }

    else {
            response.setStatus(404,"Not found");
            response.write("The URL is wrong, no such document.");
    }


    qDebug("RequestMapper: finished request");
}
