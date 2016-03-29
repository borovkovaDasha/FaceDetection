#ifndef DATABASE_H
#define DATABASE_H

#include <QSqlDatabase>
#include <QSqlQuery>

class database
{
private:
public:
    QString driver;
    QString name;
    QSqlDatabase sdb;
    QString getstr(int num);
    database(QString dbname);
    int connect();
    int createTable();
    QString getemail(QString qpath);
    int add(QString qname, QString qemail, QString qpath);
    int remove(QString qemail);
    QString getpath(QString qemail);
    int numofrows();
    bool findemail(QString qemail);
};

#endif // DATABASE_H
