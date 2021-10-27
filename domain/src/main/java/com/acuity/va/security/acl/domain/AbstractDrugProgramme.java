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

package com.acuity.va.security.acl.domain;

import org.springframework.util.ClassUtils;

/**
 * Simple Object representing an object entity identity in the spring security acl tables.
 *
 * This represents the MAP_PROJECT_RULE acuity table.
 *
 * @author Glen
 */
public abstract class AbstractDrugProgramme extends AcuityObjectIdentityImpl {

    public AbstractDrugProgramme() {
        Class<?> typeClass = ClassUtils.getUserClass(this.getClass());
        type = typeClass.getName();
        supertype = typeClass.getName();
    }

    public AbstractDrugProgramme(Long identifier) {
        this();
        this.id = identifier;
    }

    public AbstractDrugProgramme(String name) {
        this();
        this.id = generateId(name);
        this.name = name;
    }

    public AbstractDrugProgramme(Long identifier, String name) {
        this(identifier);
        this.name = name;
    }

    public AbstractDrugProgramme(Long identifier, String name, Integer mask) {
        this(identifier, name);
        this.permissionMask = mask;
    }

    public AbstractDrugProgramme(String name, Integer mask) {
        this(name);
        this.id = generateId(name);
        this.permissionMask = mask;
    }

    @Override
    public boolean isLockdown() {
        return false;
    }
}
