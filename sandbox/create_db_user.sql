CREATE DATABASE IF NOT EXISTS log;
CREATE DATABASE IF NOT EXISTS cpsiot;

CREATE USER 'arrowhead'@'localhost' IDENTIFIED BY '20peter19';
CREATE USER 'arrowhead'@'%' IDENTIFIED BY '20peter19';

USE log;
CREATE TABLE logs ( id int(10) unsigned NOT NULL AUTO_INCREMENT, date datetime NOT NULL, origin varchar(255) COLLATE utf8_unicode_ci NOT NULL, level varchar(10) COLLATE utf8_unicode_ci NOT NULL, message varchar(1000) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (id) ) ENGINE=InnoDB AUTO_INCREMENT=1557 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

GRANT ALL PRIVILEGES ON log.* TO 'arrowhead'@'%';
GRANT ALL PRIVILEGES ON log.* TO 'arrowhead'@'localhost';

GRANT ALL PRIVILEGES ON cpsiot.* TO 'arrowhead'@'%';
GRANT ALL PRIVILEGES ON cpsiot.* TO 'arrowhead'@'localhost';
