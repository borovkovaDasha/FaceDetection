QT  += core
QT  -= gui
QT  += network

TARGET = MyFirstWebApp

CONFIG   += console

TEMPLATE = app

SOURCES += src/main.cpp \
    src/requestmapper.cpp \
    src/fileuploadcontroller.cpp \
    src/registercontroller.cpp

HEADERS += \
    src/requestmapper.h \
    src/fileuploadcontroller.h \
    src/registercontroller.h

OTHER_FILES += etc/webapp1.ini \
     docroot/files/hello.html

include(httpserver/httpserver.pri)
