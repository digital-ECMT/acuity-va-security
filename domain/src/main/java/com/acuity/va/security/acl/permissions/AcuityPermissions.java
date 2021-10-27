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

package com.acuity.va.security.acl.permissions;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.security.acls.domain.AclFormattingUtils;
import org.springframework.security.acls.model.Permission;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Add custom permissions for acuity. Use bitwise values 32 - 512 (5 - 9) as 0 - 4 already used by spring
 *
 * @author glen
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AcuityPermissions implements Permission {
    VIEW_VISUALISATIONS("VIEW_VISUALISATIONS", 1 << 0, 'V'),
    VIEW_BASE_PACKAGE("VIEW_BASE_PACKAGE", 1 << 1, 'B'),
    VIEW_ONCOLOGY_PACKAGE("VIEW_ONCOLOGY_PACKAGE", 1 << 2, 'O'),
    VIEW_PROACT_PACKAGE("VIEW_PROACT_PACKAGE", 1 << 3, 'P'),
    VIEW_MACHINE_INSIGHTS_PACKAGE("VIEW_MACHINE_INSIGHTS_PACKAGE", 1 << 4, 'M'),

    EDIT_TRAINED_USERS("EDIT_TRAINED_USERS", 1 << 11),
    EDIT_DATA_OWNERS("EDIT_DATA_OWNERS", 1 << 12),
    EDIT_AUTHORISERS("EDIT_AUTHORISERS", 1 << 13),
    EDIT_ADMINISTRATORS("EDIT_ADMINISTRATORS", 1 << 14),
    EDIT_AUTHORISED_USERS("EDIT_AUTHORISED_USERS", 1 << 15),
    EDIT_DRUG_PROGRAMMES("EDIT_DRUG_PROGRAMMES", 1 << 16),
    EDIT_CLINICAL_STUDIES("EDIT_CLINICAL_STUDIES", 1 << 17),
    EDIT_VISUALISATIONS("EDIT_VISUALISATIONS", 1 << 18);
    // Default user information
    // User used to add to acls in cron job
    public static final String ACUITY_SERVER_USER = "ACUITY_SERVER_USER";
    // Development Team Role
    public static final String DEVELOPMENT_TEAM_ROLE = "DEVELOPMENT_TEAM";

    // ie VIEW_VISUALISATIONS
    private final String name;
    private final char code;
    private final int mask;

    private static int allPermissionsMask;

    static {
        allPermissionsMask = Arrays.stream(values()).mapToInt(AcuityPermissions::getMask).sum();
    }

    AcuityPermissions(String name, int mask) {
        this.name = name;
        this.mask = mask;
        this.code = '*';
    }

    AcuityPermissions(String name, int mask, char code) {
        this.name = name;
        this.mask = mask;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public static List<AcuityPermissions> getAllEditPermissions() {
        return newArrayList(EDIT_TRAINED_USERS, EDIT_DATA_OWNERS, EDIT_AUTHORISERS, EDIT_ADMINISTRATORS,
                EDIT_AUTHORISED_USERS, EDIT_DRUG_PROGRAMMES, EDIT_CLINICAL_STUDIES, EDIT_VISUALISATIONS
        );
    }

    public static List<AcuityPermissions> getAllWithoutVisualisations() {
        return Arrays.stream(values())
                .filter(rp -> !VIEW_VISUALISATIONS.equals(rp))
                .collect(Collectors.toList());
    }

    public static String getStringMask(Integer permissionMask) {
        return getAllWithoutVisualisations().stream().filter(pm -> pm.getMask() == permissionMask)
                .findFirst().map(AcuityPermissions::getName).orElse(null);
    }

    public static int getAllPermissionsMask() {
        return allPermissionsMask;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public String getPattern() {
        return AclFormattingUtils.printBinary(mask, code);
    }
}
