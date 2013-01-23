/**
 * Copyright (c) 2012, NuoDB, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of NuoDB, Inc. nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUODB, INC. BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nuodb.migration.jdbc.connection;

import com.nuodb.migration.jdbc.url.JdbcUrl;
import com.nuodb.migration.jdbc.url.JdbcUrlParserUtils;
import com.nuodb.migration.spec.DriverConnectionSpec;
import com.nuodb.migration.utils.ReflectionUtils;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static java.lang.String.format;

public class DriverConnectionProvider extends DataSourceConnectionProvider {

    private DriverConnectionSpec driverConnectionSpec;
    private JdbcUrl jdbcUrl;
    private Boolean autoCommit;
    private Integer transactionIsolation;

    public DriverConnectionProvider(DriverConnectionSpec driverConnectionSpec) {
        this(driverConnectionSpec, false);
    }

    public DriverConnectionProvider(DriverConnectionSpec driverConnectionSpec, Boolean autoCommit) {
        this(driverConnectionSpec, autoCommit, null);
    }

    public DriverConnectionProvider(DriverConnectionSpec driverConnectionSpec, Boolean autoCommit,
                                    Integer transactionIsolation) {
        this.driverConnectionSpec = driverConnectionSpec;
        this.jdbcUrl = JdbcUrlParserUtils.getInstance().parse(
                driverConnectionSpec.getUrl(), driverConnectionSpec.getProperties());
        this.autoCommit = autoCommit;
        this.transactionIsolation = transactionIsolation;
    }

    @Override
    public String getCatalog() {
        String catalog = jdbcUrl.getCatalog();
        if (jdbcUrl.getCatalog() != null) {
            catalog = jdbcUrl.getCatalog();
        }
        return catalog;
    }

    @Override
    public String getSchema() {
        String schema = jdbcUrl.getSchema();
        if (jdbcUrl.getSchema() != null) {
            schema = jdbcUrl.getSchema();
        }
        return schema;
    }

    protected DataSource createDataSource() throws SQLException {
        registerDriver();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(driverConnectionSpec.getUrl());
        dataSource.setUsername(driverConnectionSpec.getUsername());
        dataSource.setPassword(driverConnectionSpec.getPassword());
        Map<String, Object> properties = driverConnectionSpec.getProperties();
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                dataSource.addConnectionProperty(entry.getKey(), (String) entry.getValue());
            }
        }
        return dataSource;
    }

    protected void registerDriver() throws SQLException {
        Driver driver = driverConnectionSpec.getDriver();
        if (driver == null) {
            String driverClassName = driverConnectionSpec.getDriverClassName();
            if (logger.isDebugEnabled()) {
                logger.debug(format("Loading driver %s", driverClassName));
            }
            driver = ReflectionUtils.newInstance(driverClassName);
        }
        DriverManager.registerDriver(driver);
    }

    protected DriverConnectionSpec getDriverConnectionSpec() {
        return driverConnectionSpec;
    }

    protected void initConnection(Connection connection) throws SQLException {
        if (autoCommit != null) {
            connection.setAutoCommit(autoCommit);
        }
        if (transactionIsolation != null) {
            connection.setTransactionIsolation(transactionIsolation);
        }
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Integer getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(Integer transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Closing connection");
            }
            connection.close();
        }
    }

    @Override
    public String toString() {
        return driverConnectionSpec.getUrl();
    }
}
