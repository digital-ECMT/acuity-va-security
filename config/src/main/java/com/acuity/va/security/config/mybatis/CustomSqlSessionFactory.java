/*
 * Copyright 2021 The University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acuity.va.security.config.mybatis;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows parallel queries
 * 
 * @author Glen
 */
public class CustomSqlSessionFactory extends DefaultSqlSessionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CustomSqlSessionFactory.class);

    public CustomSqlSessionFactory(Configuration configuration) {
        super(configuration);
    }

    @Override
    public SqlSession openSession() {
        SqlSession session = super.openSession();
        alterSession(session);
        return session;
    }

    protected void alterSession(SqlSession session) {
        try {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.addBatch("ALTER SESSION ENABLE PARALLEL DML");
                statement.addBatch("ALTER SESSION ENABLE PARALLEL QUERY");
                statement.executeBatch();
                LOG.debug("Altered newly created session parameters with parallel DML and QUERY.");
            }
        } catch (SQLException e) {
            LOG.warn("Alter session failed, using H2?!" + e.getMessage(), e);
        }
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        SqlSession session = super.openSession(autoCommit);
        alterSession(session);
        return session;
    }

    @Override
    public SqlSession openSession(Connection connection) {
        SqlSession session = super.openSession(connection);
        alterSession(session);
        return session;
    }

    @Override
    public SqlSession openSession(ExecutorType execType) {
        SqlSession session = super.openSession(execType);
        alterSession(session);
        return session;
    }

    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        SqlSession session = super.openSession(execType, autoCommit);
        alterSession(session);
        return session;
    }

    @Override
    public SqlSession openSession(ExecutorType execType, Connection connection) {
        SqlSession session = super.openSession(execType, connection);
        alterSession(session);
        return session;
    }

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        SqlSession session = super.openSession(execType, level);
        alterSession(session);
        return session;
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        SqlSession session = super.openSession(level);
        alterSession(session);
        return session;
    }
}
