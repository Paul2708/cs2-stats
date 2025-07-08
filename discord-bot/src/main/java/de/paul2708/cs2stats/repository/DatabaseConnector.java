package de.paul2708.cs2stats.repository;

import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;

import javax.sql.DataSource;

public class DatabaseConnector {

    public void registerMapper(DataSource dataSource) {
        RowMapperRegistry registry = new RowMapperRegistry();
        registry.register(PostgresqlMapper.getDefaultMapper());

        QueryConfiguration.setDefault(QueryConfiguration.builder(dataSource)
                .setRowMapperRegistry(new RowMapperRegistry().register(PostgresqlMapper.getDefaultMapper()))
                .build());
    }
}
