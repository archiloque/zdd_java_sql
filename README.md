# zdd-java-sql

Demonstrate how to to ZDD with SQL migrations.
This project expose a REST API and is written with Dropwizard.

Subdirectories contain the different versions of the project to show the steps.

Versions
---
- `v1` Initial version, store the address in the `address` column of the `Person` table
- `v2` Add new `Address` table, not used by the code
- `v3` Can store address in the `address` column of the `Person` table or in the `Address` table
- `v4` Migrate all addresses to the `Address` table
- `v5` Provide the `/v2` API, remove knowledge of the `address` column from the code
- `v6` Drop the `address` column

Prerequisites
---

- A local PostgreSQL database
- A `zdd_java_sql` user with a `zdd_java_sql` password
- Run `reset_databases.sh` to create the databases

How to start one of the applications
---

1. Go to the `vX` directory
1. Run `mvn clean install` to build the application
1. Create a database in your PostgreSQL schema
1. Update the database configuration in `config.yml`
1. Run `java -jar target/vX-1.0-SNAPSHOT.jar db migrate config.yml` to initialize the database tables
1. Start application with `java -jar target/vX-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

Command-line API examples :
---

V1 :

- Get all people : `curl -v "http://localhost:8080/v1/people"`
- Get one person `curl -v "http://localhost:8080/v1/people/1"`
- Create one person `curl -v -X POST -H "Content-Type: application/json" -d '{"name":"John Doe", "address": "34, avenue de l Opéra 75002 Paris"}' "http://localhost:8080/v1/people"`
- Delete one person `curl -v -X DELETE "http://localhost:8080/v1/people/1"`
- Update one person `curl -v -X PUT POST -H "Content-Type: application/json" -d '{"name":"John Doe", "address": "34, avenue de l Opéra 75002 Paris"}' "http://localhost:8080/v1/people/1"`
