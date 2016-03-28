QT += core
QT -= gui
QT  += network
QT += sql

CONFIG += c++11

TARGET = untitled3
CONFIG += console
CONFIG -= app_bundle
INCLUDEPATH+=C:\opencv_build\install
INCLUDEPATH+=C:\opencv_build\install\include
INCLUDEPATH+=C:\opencv_build\install\include\opencv2
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_core310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_features2d310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_flann310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_highgui310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_objdetect310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_video310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_calib3d310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_imgproc310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_ml310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_imgcodecs310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_shape310.dll.a
#LIBS+=C:\new_opencv\opencv\build\x64\vc10\lib\opencv_shape2411d.lib
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_stitching310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_superres310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_objdetect310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_face310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_aruco310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_bgsegm310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_bioinspired310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_ccalib310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_datasets310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_dnn310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_dpm310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_features2d310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_flann310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_fuzzy310.dll.a
#LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_hal310.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_line_descriptor310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_optflow310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_photo310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_plot310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_reg310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_rgbd310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_saliency310.dll.a
LIBS+=C:\opencv_build\install\x86\mingw\lib\libopencv_xobjdetect310.dll.a
#greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TEMPLATE = app

SOURCES += \
    src/facerecognition.cpp \
    src/fileuploadcontroller.cpp \
    src/main.cpp \
    src/registercontroller.cpp \
    src/requestmapper.cpp \
    httpserver/httpconnectionhandler.cpp \
    httpserver/httpconnectionhandlerpool.cpp \
    httpserver/httpcookie.cpp \
    httpserver/httpglobal.cpp \
    httpserver/httplistener.cpp \
    httpserver/httprequest.cpp \
    httpserver/httprequesthandler.cpp \
    httpserver/httpresponse.cpp \
    httpserver/httpsession.cpp \
    httpserver/httpsessionstore.cpp \
    httpserver/staticfilecontroller.cpp \
    src/database.cpp \
    src/notepadcontroller.cpp

HEADERS += \
    src/facerecognition.h \
    src/fileuploadcontroller.h \
    src/registercontroller.h \
    src/requestmapper.h \
    httpserver/httpconnectionhandler.h \
    httpserver/httpconnectionhandlerpool.h \
    httpserver/httpcookie.h \
    httpserver/httpglobal.h \
    httpserver/httplistener.h \
    httpserver/httprequest.h \
    httpserver/httprequesthandler.h \
    httpserver/httpresponse.h \
    httpserver/httpsession.h \
    httpserver/httpsessionstore.h \
    httpserver/staticfilecontroller.h \
    src/database.h \
    src/notepadcontroller.h

DISTFILES += \
    etc/webapp1.ini
