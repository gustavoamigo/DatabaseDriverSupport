#!/bin/bash
sleep 30

mysql -u root -proot -h mysql -e "ALTER USER 'root'@'%' IDENTIFIED BY ''"
mysql -u root -h mysql quill_test < mysql-schema.sql
mysql -u root -h mysql -e "CREATE USER 'finagle'@'%' IDENTIFIED BY 'finagle';"
mysql -u root -h mysql -e "GRANT ALL PRIVILEGES ON * . * TO 'finagle'@'%';"
mysql -u root -h mysql -e "FLUSH PRIVILEGES;"

createdb -h postgres quill_test -U postgres
psql -h postgres -U postgres -d quill_test -a -f postgres-schema.sql