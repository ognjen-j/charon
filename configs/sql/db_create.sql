CREATE DATABASE IF NOT EXISTS `radius` /*!40100 DEFAULT CHARACTER SET utf8 */;
CREATE USER radius@'10.41.126.2' identified by '__MYSQL_RADIUS_PASSWORD__';
CREATE USER __MYSQL_WEBAPP_USERNAME__@'10.41.127.3' identified by '__MYSQL_WEBAPP_PASSWORD__';
grant all privileges on radius.* to radius@'10.41.126.2';
grant all privileges on radius.* to __MYSQL_WEBAPP_USERNAME__@'10.41.127.3';
flush privileges;