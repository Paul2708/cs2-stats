package de.paul2708.cs2stats.repository;

import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import io.github.cdimascio.dotenv.Dotenv;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Driver;
import java.sql.SQLException;

public class DatabaseConnector {

    private DataSource dataSource;

    public void connect(Dotenv dotenv) {
        this.dataSource = createDatasource(dotenv);

        configure();
        setup();
    }

    private DataSource createDatasource(Dotenv dotenv) {
        return DataSourceCreator.create(PostgreSql.get())
                .configure(config -> config.host(dotenv.get("DB_HOST"))
                        .port(5432)
                        .user(dotenv.get("DB_USER"))
                        .password(dotenv.get("DB_PASSWORD"))
                        .database(dotenv.get("DB_DATABASE"))
                        .applicationName("CS2Stats")
                        .driverClass(Driver.class)
                )
                .create()
                .withMaximumPoolSize(3)
                .withMinimumIdle(1)
                .build();
    }

    private void configure() {
        RowMapperRegistry registry = new RowMapperRegistry();
        registry.register(PostgresqlMapper.getDefaultMapper());

        QueryConfiguration.setDefault(QueryConfiguration.builder(dataSource)
                .setRowMapperRegistry(new RowMapperRegistry().register(PostgresqlMapper.getDefaultMapper()))
                .build());
    }

    private void setup() {
        try {
            SqlUpdater.builder(dataSource, PostgreSql.get())
                    .execute();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
