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

package com.acuity.va.security.config.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;

/**
 * Overrides EhCacheCacheManager so that any caches in the application that arent configured in the ehcache.xml are then
 * created on the fly.
 *
 * @author Glen
 */
public class CustomEhCacheCacheManager extends EhCacheCacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(CustomEhCacheCacheManager.class);

    @Override
    public Cache getCache(String name) {
        boolean exists = getCacheManager().cacheExists(name); // used defaultcache implementation
        if (!exists) {
            getCacheManager().addCacheIfAbsent(name);
            LOG.debug("Created cache " + name + ", " + getCacheManager().getActiveConfigurationText(name));
        }
        return super.getCache(name);
    }
}