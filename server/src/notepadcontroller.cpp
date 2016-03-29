#include "notepadcontroller.h"
#include "database.h"

notepadController::notepadController(QObject* parent)
    : HttpRequestHandler(parent) {
     email = "";
     data = "";
     action = "";

}

void notepadController::service(HttpRequest& request, HttpResponse& response){
    email = request.getParameter("email");
    action = request.getParameter("action");
    database db("facedetection.sqlite");
    db.connect();
    if (action == "get") {

        //open file or create new with email
        //read from file to data
        QString txt = db.getpath(email) + "/file.txt";
        QFile file(txt); // создаем объект класса QFile
        QByteArray data; // Создаем объект класса QByteArray, куда мы будем считывать данные
        if (!file.open(QIODevice::ReadOnly)) // Проверяем, возможно ли открыть наш файл для чтения
             return; // если это сделать невозможно, то завершаем функцию
        data = file.readAll(); //считываем все данные с файла в объект data
        qDebug() << "we will send to the user: "<<QString(data); // Выводим данные в консоль, предварительно создав строку из полученных данных
        response.write(data, true);
    }
    else {
       data = request.getParameter("action");
       if (data != 0){
           QString txt = db.getpath(email) + "/file.txt";
           QFile file(txt);
           if (!file.open(QIODevice::WriteOnly | QIODevice::Text)) // Проверяем, возможно ли открыть наш файл для чтения
                return; // если это сделать невозможно, то завершаем функцию
           QTextStream writeStream(&file); // Создаем объект класса QTextStream
           // и передаем ему адрес объекта fileOut
           writeStream << data; // Посылаем строку в поток для записи
           qDebug() << "user sent us: "<<QString(data);
           file.close(); // Закрываем файл
           }
           response.write("true", true);
       }
    }
