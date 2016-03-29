#ifndef NOTEPADCONTROLLER_H
#define NOTEPADCONTROLLER_H
#include "httpserver/httprequesthandler.h"

class notepadController: public HttpRequestHandler {
    Q_OBJECT
public:
    notepadController(QObject* parent=0);
    void service(HttpRequest& request, HttpResponse& response);
    QByteArray data;
    QByteArray action;
    QByteArray email;
};

#endif // NOTEPADCONTROLLER_H
