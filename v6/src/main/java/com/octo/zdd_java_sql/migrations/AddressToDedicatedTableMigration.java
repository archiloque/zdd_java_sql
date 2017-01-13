package com.octo.zdd_java_sql.migrations;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AddressToDedicatedTableMigration implements CustomSqlChange {

    private static final int MIGRATION_STEP_SIZE = 100;

    @Override
    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
        Logger log = LogFactory.getInstance().getLog();
        JdbcConnection connection = (JdbcConnection) database.getConnection();

        try {
            /*
            Start by creating a few lines
            for (int i = 0; i < 998; i++) {
                String insertSql = "insert into person(name, address) values('name_" + i + "', 'address_" + i + "')";
                Statement statement = connection.createStatement();
                statement.executeUpdate(insertSql);
                statement.close();

            }
            */

            ResultSet personResult = connection.createStatement().executeQuery("select max(id) max_id from person");
            personResult.next();
            long maxPersonId = personResult.getLong(1);
            personResult.close();
            log.info("Max person id is [" + maxPersonId + "]");
            long lastMigrationStep = (maxPersonId / MIGRATION_STEP_SIZE) * MIGRATION_STEP_SIZE;
            for (int i = 0; i < lastMigrationStep; i += MIGRATION_STEP_SIZE) {
                log.info("Starting migration step at [" + i + "]");
                runMigrationStep(database, connection, i, i + MIGRATION_STEP_SIZE);
                log.info("Ended migration step at [" + i + "]");
            }

            log.info("Starting migration step at [" + lastMigrationStep + "]");
            runMigrationStep(database, connection, lastMigrationStep, maxPersonId + 1);
            log.info("Ended migration step at [" + lastMigrationStep + "]");


        } catch (SQLException | DatabaseException e) {
            throw new CustomChangeException(e);
        }

        return new SqlStatement[0];
    }

    private void runMigrationStep(Database database, JdbcConnection connection, long from, long to) throws DatabaseException, SQLException {
        String whereClause = "where id >= " + from + " and id < " + to + " and address is not null";

        database.commit();
        String selectSql = "select from person " + whereClause + " for update";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSql);
        resultSet.close();

        String insertSql = "insert into address(person_id, address) (select id, address from person " + whereClause + ")";
        statement = connection.createStatement();
        statement.executeUpdate(insertSql);
        statement.close();

        String updateSql = "update person set address = null " + whereClause;
        statement = connection.createStatement();
        statement.executeUpdate(updateSql);
        statement.close();

        database.commit();
    }

    @Override
    public String getConfirmationMessage() {
        return "Address migration is done";
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
