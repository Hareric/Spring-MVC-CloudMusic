## 项目说明
该项目是基于原项目，重构后使用Spring框架搭建java后台。
原项目地址：https://github.com/Zenaro/CloudMusic-for-normal


## 配置
开发环境 Eclipse Java EE IDE for Web Developers 4.3.2

开发语言 Java 1.8

MVC框架 Spring 4.3.3

web服务器 Tomcat

开发系统 mac OSX

Mysql数据库

## 相关路径：
controller： CloudMusic/src/controller

model:  CloudMusic/src/model

view:  CloudMusic/WebContent/WEB-INF/view


## 运行方法
1、需要提前配置好mysql数据库，sql文件位于根目录 (DDL.sql DML.sql)

2、设置数据库参数 配置文件位于 apache-tomcat/webapps/CloudMusic/WEB-INF/classes/dbConfig.properties

3、终端启动服务器
```
cd apache-tomcat
./bin/catalina.sh run
```

运行成功后，浏览器打开 http://localhost:8080/CloudMusic 可访问

## 其他说明

1、若运行.sh文件 报permisison denied， 则在终端运行 chmod +x catalina.sh 修改权限


