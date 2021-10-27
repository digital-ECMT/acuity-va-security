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

package com.acuity.va.security.acl.util;

import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Class to generate and populate an external H2 database with the test data as in the unit tests
 *
 * @author Glen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class LoadingStaticH2DBWithDBUnitData {

    @Autowired
    DataSource dataSource;

    @Test
    @Rollback(false)
    public void createDB() throws Exception {

        List<String> sqls = Lists.newArrayList(
                "/sql/createAclSchemaH2.sql",
                "/sql/createGroupSchemaH2.sql"
        );

        sqls.forEach(sql -> {
            try {
                ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource(sql));
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("Executed " + sql);
        });
    }

    @Test
    @DatabaseSetup("/dbunit/dbunit-all-security.xml")
    @Rollback(false)
    public void loadSecurityDBUnitData() {
        System.out.println("Loading security h2 db");
    }
}
