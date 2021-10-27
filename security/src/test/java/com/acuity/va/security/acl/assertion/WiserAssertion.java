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

import org.assertj.core.api.AbstractAssert;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.WiserMessage;

/**
 * Assertion class for asserting the email messages from calc engine
 *
 * @author Glen
 */
public class WiserAssertion extends AbstractAssert<WiserAssertion, WiserMessage> {

    private static final Logger log = LoggerFactory.getLogger(WiserAssertion.class);

    /**
     * Code for start of the header for a study or experiment table
     */
    private final String START_HEADER = "<span style='font-size:14.0pt;color:#7030A0'>";

    protected WiserAssertion(WiserMessage actual) {
        super(actual, WiserAssertion.class);
    }

    public String getContent() {
        return _getContent();
    }

    public String getSubject() {
        return _getSubject();
    }


    /**
     * Asserts the content (text) of the email
     */
    public WiserAssertion hasContent(String content) {
        assertThat(_getContent()).isEqualTo(content);
        return this;
    }
    
    /**
     * Asserts the content (text) of the email
     */
    public WiserAssertion hasContentContains(String content) {
        assertThat(_getContent()).contains(content);
        return this;
    }

    /**
     * Asserts the subject of the email
     */
    public WiserAssertion hasSubject(String subject) {
        assertThat(_getSubject()).isEqualTo(subject);
        return this;
    }

    /**
     * Asserts the sender of the email
     */
    public WiserAssertion isSendFrom(String from) {
        assertThat(actual.getEnvelopeSender()).isEqualTo(from);
        return this;
    }

    /**
     * Asserts the sender (fuzzy match) of the email
     */
    public WiserAssertion isSendFromContains(String from) {
        assertThat(actual.getEnvelopeSender()).contains(from);
        return this;
    }

    /**
     * Asserts the recipient of the email
     */
    public WiserAssertion isSendTo(String to) {
        assertThat(actual.getEnvelopeReceiver()).isEqualTo(to);
        return this;
    }

    /**
     * Asserts the recipient (fuzzy match) of the email
     */
    public WiserAssertion isSendToContains(String to) {
        assertThat(actual.getEnvelopeReceiver()).contains(to);
        return this;
    }
   
    /**
     * Wraps checked exceptions into runtime for email content
     */
    private String _getContent() {
        try {
            return (String) actual.getMimeMessage().getContent();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Wraps checked exceptions into runtime for email subject
     */
    private String _getSubject() {
        try {
            return actual.getMimeMessage().getSubject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
