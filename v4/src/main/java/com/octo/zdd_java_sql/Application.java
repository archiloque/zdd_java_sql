package com.octo.zdd_java_sql;

import com.octo.zdd_java_sql.db.AddressDAO;
import com.octo.zdd_java_sql.db.PersonDAO;
import com.octo.zdd_java_sql.resources.v1.PersonResource;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.ScanningHibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Application extends io.dropwizard.Application<Configuration> {

    public static void main(final String[] args) throws Exception {
        new Application().run(args);
    }

    @Override
    public String getName() {
        return "zdd-java-sql";
    }


    private final ScanningHibernateBundle<Configuration> hibernateBundle =
            new ScanningHibernateBundle<Configuration>(
                    "com.octo.zdd_java_sql.core") {

                @Override
                public DataSourceFactory getDataSourceFactory(Configuration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public void initialize(final Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new MigrationsBundle<Configuration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(Configuration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(final Configuration configuration,
                    final Environment environment) {
        final PersonDAO personDAO = new PersonDAO(hibernateBundle.getSessionFactory());
        final AddressDAO addressDAO = new AddressDAO(hibernateBundle.getSessionFactory());
        environment.jersey().register(new PersonResource(personDAO, addressDAO));
    }


}
