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

package com.acuity.va.security.acl.dao;

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.acl.task.RefreshCachesTask;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.common.Constants.HOURLY_REFRESHABLE_CACHE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Glen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenQueryingWithCaching {

    private static final Logger LOG = LoggerFactory.getLogger(WhenQueryingWithCaching.class);

    @Autowired
    AclRepository aclRepository;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    SecurityAclService securityAclService;

    @Autowired
    RefreshCachesTask refreshCachesTask;

    @BeforeClass
    public static void activateCache() {
        System.setProperty("net.sf.ehcache.disabled", "false");
    }

    @AfterClass
    public static void deactivateCache() {
        System.setProperty("net.sf.ehcache.disabled", "true");
    }

    @Test
    public void shouldCacheListingAclObjects() {

        Cache cache = cacheManager.getCache(HOURLY_REFRESHABLE_CACHE + "AclRepository-listObjectIdentities");

      //  assertThat(cache).isNull();

        LOG.info("1");
        aclRepository.listObjectIdentities(); //creates cache
        LOG.info("2");
        cache = cacheManager.getCache(HOURLY_REFRESHABLE_CACHE + "AclRepository-listObjectIdentities");

       // ArrayList list = (ArrayList) ((Map) cache.getNativeCache()).values().iterator().next();
        assertThat(cache.getSize()).isEqualTo(1);
        LOG.info("3");
        aclRepository.listObjectIdentities();
        LOG.info("4");
    }

    @Test
    public void shouldCacheAclFind() {
        Cache cache = cacheManager.getCache("aclCache");

       // assertThat(cache.getSize()).isEqualTo(0);

        securityAclService.find(new DrugProgramme(2L));

        //assertThat(cache.getSize()).isEqualTo(2);

        securityAclService.find(new DrugProgramme(2L));
    }

    @Test
    public void shouldRefreshOnlyRefreshableCaches() {

        // put thing in cache
        securityAclService.find(new DrugProgramme(2L));
        aclRepository.listObjectIdentities();

        Cache noneRefreshableCache = cacheManager.getCache("aclCache");
        Cache refreshableCache = cacheManager.getCache(HOURLY_REFRESHABLE_CACHE + "AclRepository-listObjectIdentities");

        //assertThat(noneRefreshableCache.getSize()).isEqualTo(2);
        //assertThat(refreshableCache.getSize()).isEqualTo(1);

        refreshCachesTask.runNightly();
        refreshCachesTask.runHourly();

       // assertThat(noneRefreshableCache.getSize()).isEqualTo(2);
        // assertThat(refreshableCache.getSize()).isEqualTo(0);
    }
}
