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

package com.acuity.va.security.acl.task;

import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.acuity.va.security.common.Constants.HOURLY_REFRESHABLE_CACHE;
import static com.acuity.va.security.common.Constants.REFRESHABLE_CACHE;

/**
 * Clears all the caches that have a name starting with Constants.CLEAR_CACHE_PREFIX = "refreshable-"
 *
 * @author Glen
 */
@Component
public class RefreshCachesTask {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshCachesTask.class);

    @Autowired
    private CacheManager cacheManager;

    public void runNightly() {
        LOG.info("Running NightlyRefreshCachesTask ...");

        cacheManager.clearAllStartingWith(REFRESHABLE_CACHE);

        LOG.info("Finished NightlyRefreshCachesTask");
    }

    public void runHourly() {
        LOG.info("Running HourlyRefreshCachesTask ...");

        cacheManager.clearAllStartingWith(HOURLY_REFRESHABLE_CACHE);

        LOG.info("Finished HourlyRefreshCachesTask");
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
