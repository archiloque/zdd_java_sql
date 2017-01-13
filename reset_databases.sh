#!/usr/bin/env bash

# Reset databases : drop the databases, recreate them, and recreate the schemas

set -x # echo on

cd v1
dropdb zdd_java_sql_v1
createdb --owner=zdd_java_sql zdd_java_sql_v1
mvn clean install -Dmaven.test.skip=true
java -jar target/v1-1.0-SNAPSHOT.jar db migrate config.yml
cd ..

cd v2
dropdb zdd_java_sql_v2
createdb --owner=zdd_java_sql zdd_java_sql_v2
mvn clean install -Dmaven.test.skip=true
java -jar target/v2-1.0-SNAPSHOT.jar db migrate config.yml
cd ..

cd v3
dropdb zdd_java_sql_v3
createdb --owner=zdd_java_sql zdd_java_sql_v3
mvn clean install -Dmaven.test.skip=true
java -jar target/v3-1.0-SNAPSHOT.jar db migrate config.yml
cd ..

cd v4
dropdb zdd_java_sql_v4
createdb --owner=zdd_java_sql zdd_java_sql_v4
mvn clean install -Dmaven.test.skip=true
java -jar target/v4-1.0-SNAPSHOT.jar db migrate config.yml
cd ..

cd v5
dropdb zdd_java_sql_v5
createdb --owner=zdd_java_sql zdd_java_sql_v5
mvn clean install -Dmaven.test.skip=true
java -jar target/v5-1.0-SNAPSHOT.jar db migrate config.yml
cd ..

cd v6
dropdb zdd_java_sql_v6
createdb --owner=zdd_java_sql zdd_java_sql_v6
mvn clean install -Dmaven.test.skip=true
java -jar target/v6-1.0-SNAPSHOT.jar db migrate config.yml
cd ..