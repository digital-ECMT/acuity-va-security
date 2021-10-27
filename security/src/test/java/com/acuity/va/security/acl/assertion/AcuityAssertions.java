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

package com.acuity.va.security.acl.assertion;

import org.springframework.security.acls.model.Acl;
import org.subethamail.wiser.WiserMessage;

/**
 * Entry point for assertion methods for Acls. Each method in this class is a static factory for the
 * type-specific assertion objects. The purpose of this class is to make test code more readable.
 *
 * @author glen
 */
public class AcuityAssertions {

    /**
     * Creates a new instance of
     * <code>{@link AclAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    public static AclAssert assertThat(Acl actual) {
        return new AclAssert(actual);
    }
   
    /**
     * Creates a new instance of
     * <code>{@link WiserAssertion}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    public static WiserAssertion assertThat(WiserMessage actual) {
        return new WiserAssertion(actual);
    }
}
