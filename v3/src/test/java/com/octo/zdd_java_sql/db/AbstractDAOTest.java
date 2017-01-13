package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import io.dropwizard.testing.junit.DAOTestRule;
import org.hibernate.cfg.AvailableSettings;
import org.junit.Rule;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.sql.Driver;
import java.util.Map;

public abstract class AbstractDAOTest {

    @Rule
    public final DAOTestRule daoTestRule;


    AbstractDAOTest() throws FileNotFoundException, ClassNotFoundException {
        Yaml yaml = new Yaml();
        Map configuration = (Map) yaml.load(getClass().getClassLoader()
                .getResourceAsStream("test-config.yml"));
        Map databaseConfiguration = (Map) configuration.get("database");
        DAOTestRule.Builder builder = DAOTestRule.newBuilder().
                setDriver((Class<? extends Driver>) Class.forName((String) databaseConfiguration.get("driverClass"))).
                setUrl((String) databaseConfiguration.get("url")).
                setUsername((String) databaseConfiguration.get("user")).
                addEntityClass(PersonEntity.class).
                addEntityClass(AddressEntity.class);
        builder.setProperty(AvailableSettings.PASS, (String) databaseConfiguration.get("password"));
        ((Map) databaseConfiguration.get("properties")).forEach((o, o2) -> builder.setProperty((String) o, (String) o2));
        daoTestRule = builder.
                build();

    }


}
