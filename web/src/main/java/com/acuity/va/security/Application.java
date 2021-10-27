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

package com.acuity.va.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @SuppressWarnings("checkstyle:linelength")
    @EventListener(ApplicationReadyEvent.class)
    public void onStartUp() {
        // The printed message that states "vasec has started" on SpringBoot startup. ASCII symbols are used to make it readable in both IDE console log and file log.
        LOG.info("\n888     888     d8888  .d8888b.  8888888888 .d8888b.       888    888        d8888  .d8888b.        .d8888b. 88888888888     d8888 8888888b. 88888888888 8888888888 8888888b.  \n"
                 + "888     888    d88888 d88P  Y88b 888       d88P  Y88b      888    888       d88888 d88P  Y88b      d88P  Y88b    888        d88888 888   Y88b    888     888        888  \"Y88b \n"
                 + "888     888   d88P888 Y88b.      888       888    888      888    888      d88P888 Y88b.           Y88b.         888       d88P888 888    888    888     888        888    888 \n"
                 + "Y88b   d88P  d88P 888  \"Y888b.   8888888   888             8888888888     d88P 888  \"Y888b.         \"Y888b.      888      d88P 888 888   d88P    888     8888888    888    888 \n"
                 + " Y88b d88P  d88P  888     \"Y88b. 888       888             888    888    d88P  888     \"Y88b.          \"Y88b.    888     d88P  888 8888888P\"     888     888        888    888 \n"
                 + "  Y88o88P  d88P   888       \"888 888       888    888      888    888   d88P   888       \"888            \"888    888    d88P   888 888 T88b      888     888        888    888 \n"
                 + "   Y888P  d8888888888 Y88b  d88P 888       Y88b  d88P      888    888  d8888888888 Y88b  d88P      Y88b  d88P    888   d8888888888 888  T88b     888     888        888  .d88P \n"
                 + "    Y8P  d88P     888  \"Y8888P\"  8888888888 \"Y8888P\"       888    888 d88P     888  \"Y8888P\"        \"Y8888P\"     888  d88P     888 888   T88b    888     8888888888 8888888P\"  ");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
