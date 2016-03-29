#include "database.h"
#include <QtSql/QSqlDatabase>
#include <QtSql/QSqlError>
#include <QtSql/QSqlQuery>
#include <QtSql/QSqlRecord>
#include <QtDebug>
#include <string>


database::database(QString dbname)
{
    name = dbname;
    driver = "QSQLITE";
    sdb = QSqlDatabase::addDatabase(driver);
}

int database::connect()
{
    sdb.setDatabaseName(name);
    if (!sdb.open())
    {
        qDebug() << sdb.lastError().text();
        return -1;
    }
    qDebug() << "connected\n";
    return 0;
}

int database::add(QString qname, QString qemail, QString qpath)
{
    QSqlQuery query;
    query.prepare("INSERT INTO person (username, email, path) " "VALUES (:qname, :qemail, :qpath)");
    query.bindValue(0, qname);
    query.bindValue(1, qemail);
    query.bindValue(2, qpath);
    bool execute = query.exec();
    if (!execute)
    {
        qDebug() << query.lastError().text();
        return -1;
    }
    return 0;
}

int database::remove(QString qemail)
{
    QSqlQuery query;
    query.prepare
            ("delete from person where email = (:qemail)");
    query.bindValue(":qemail", qemail);
    bool execute = query.exec();
    if (!execute)
    {
        qDebug() << query.lastError().text();
        return -1;
    }
    return 0;
}

QString database::getpath(QString qemail)
{
    QSqlQuery query;
    query.prepare
            ("select path from person where email = :qemail;");
    query.bindValue(":qemail", qemail);
    bool execute = query.exec();
    if (!execute)
    {
        qDebug() << query.lastError().text();
        return query.lastError().text();
    }
    QSqlRecord record = query.record();
    if (query.next())
    {
        return query.value(record.indexOf("path")).toString();
    }
    return "not found";
}

QString database::getemail(QString qpath)
{
    QSqlQuery query;
    query.prepare
            ("select * from person");
    query.bindValue(":qpath", qpath);
    bool execute = query.exec();
    if (!execute)
    {
        qDebug() << query.lastError().text();
        return query.lastError().text();
    }
    QSqlRecord record = query.record();
    while (query.next())
    {
        if(query.value(record.indexOf("path")).toString() == qpath)
            return query.value(record.indexOf("email")).toString();
    }
    return "not found";
}

int database::createTable()
{
    QSqlQuery query;

     bool execute  = query.exec("create table person "
              "(username varchar(20) primary key, "
              "email varchar(20), "
              "path varchar(30))");

    if (!execute)
    {
        qDebug() << query.lastError().text();
        return -1;
    }
    return 0;
}

int database::numofrows()
{
    QSqlQuery query;
    bool execute  = query.exec("SELECT * FROM person");
    if (!execute)
    {
        qDebug() << query.lastError().text();
        return -1;
    }
    //QSqlRecord record = query.record();
    int i = 0;
    while (query.next())
    {
        i++;
    }
    return i;
}


bool database::findemail(QString qemail)
{
    QSqlQuery query;
    query.prepare
            ("select * from person");
    query.bindValue(":qemail", qemail);
    bool execute = query.exec();
    if (!execute)
    {
        qDebug() << query.lastError().text();
        return !execute;
    }
    QSqlRecord record = query.record();
    while (query.next())
    {
        if(query.value(record.indexOf("email")).toString() == qemail)
            return false;
    }
    return true;
}
