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

import static com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl.Origin.DETECT;
import static com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl.Origin.NONE;
import static com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl.Origin.ACUITY;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.hash.Hashing;
import java.io.Serializable;
import org.springframework.util.StringUtils;

/**
 * Base class object representing an object entity identity in the spring security acl tables, this just defines the id that is needed to look the row in the
 * table up (with the toString of the class)
 * <p>
 * <code>
 * Ie ID     NAME
 * 4      DrugProgramme
 * 14     ClinicalStudy
 * 21     ClinicalStudy
 * </code>
 *
 * @author Glen
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"identifier", "drugProgrammeType", "clinicalStudyType", "datasetType", "acuityType", "detectType"})
public abstract class AcuityObjectIdentityImpl
        implements AcuityObjectIdentity, AcuityObjectIdentityWithPermission, AcuityObjectIdentityWithPermissionAndLockDown,
        AcuityObjectIdentityWithParent, AcuityObjectIdentityWithInitialLockDown, Cloneable {

    /**
     * Type of ObjectIdentity, the classname.
     * <p>
     * <code>
     * com.acuity.va.security.acl.domain.DrugProgramme
     * com.acuity.va.security.acl.domain.DrugProgramme
     * com.acuity.va.security.acl.domain.AcuityDataset
     * com.acuity.va.security.acl.domain.DetectDataset
     * com.acuity.va.security.acl.domain.AcuityClinicalStudy
     * com.acuity.va.security.acl.domain.DetectClinicalStudy
     * </code>
     */
    protected String type;
    /**
     * SuperType of ObjectIdentity, the classname.
     * <p>
     * <code>
     * com.acuity.va.security.acl.domain.DrugProgramme
     * com.acuity.va.security.acl.domain.Dataset
     * com.acuity.va.security.acl.domain.ClinicalStudy
     * </code>
     */
    protected String supertype;
    /**
     * Id represented by the internal id in the acl db.
     * <p>
     * Ie this represents the primary key of the Drug Proramme, Study, Acuity Instance
     */
    protected Long id;
    /**
     * Display name represented by the name in the acuity db.
     */
    protected String name;
    /**
     * Permission mask of the role.
     * <p>
     * Optional.
     */
    protected Integer permissionMask;
    /**
     * Permission mask of the views.
     * <p>
     * Optional.
     */
    protected Integer viewPermissionMask;

    /**
     * Can the user view the dataset
     */
    protected boolean canView;
    /**
     * Is this in lockdown
     */
    protected boolean lockdown;
    /**
     * Does it have inherited permissions
     */
    protected boolean inherited;
    /**
     * Parent AcuityObjectIdentity
     * <p>
     * Optional.
     */
    protected AcuityObjectIdentity parent;
    /**
     * Hack to put back in where they came from AFTER i have removed it with ClinicalStudy and Drug Programme!!!
     */
    protected Origin origin = NONE;

    public enum Origin {
        DETECT, ACUITY, NONE
    }

    public AcuityObjectIdentityImpl() {
    }

    @Override
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    // for json
    //@Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    // for json
    protected void setType(String type) {
        this.type = type;
    }

    // for json
//    @Override
//    public void setPermissionMask(Integer permissionMask) {
//        this.permissionMask = permissionMask;
//    }
//
//    @Override
//    public Integer getPermissionMask() {
//        return permissionMask;
//    }
    @Override
    public void setRolePermissionMask(Integer rolePermissionMask) {
        this.permissionMask = rolePermissionMask;
    }

    @Override
    public Integer getRolePermissionMask() {
        return permissionMask;
    }

    @Override
    public void setViewPermissionMask(Integer viewPermissionMask) {
        this.viewPermissionMask = viewPermissionMask;
    }

    @Override
    public Integer getViewPermissionMask() {
        return viewPermissionMask;
    }

    /*
     * has any permission
     */
    @Override
    public boolean hasRolePermission() {
        return permissionMask != null && permissionMask != 0;
    }

    //@Override
    //public boolean hasRolePermission() {
    //    return permissionMask != null && permissionMask != 0;
    //}

    /*
     * has view visualiation permission
     */
    @JsonIgnore
    @Override
    public boolean getCanView() {
        return canView;
    }

    @JsonIgnore
    @Override
    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    @Override
    public AcuityObjectIdentity getParent() {
        return parent;
    }

    // for json
    @Override
    public void setParent(AcuityObjectIdentity parent) {
        this.parent = parent;
    }

    @Override
    public boolean isLockdown() {
        return lockdown;
    }

    public void setLockdown(boolean lockdown) {
        this.lockdown = lockdown;
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    @Override
    public boolean thisDrugProgrammeType() {
        return this instanceof DrugProgramme;
    }

    @Override
    public boolean thisClinicalStudyType() {
        return this instanceof ClinicalStudy;
    }

    @Override
    public boolean thisDatasetType() {
        return this instanceof Dataset;
    }

    @JsonIgnore
    @Override
    public boolean thisAcuityType() {
        if (origin == NONE) {
            return this.getClass().getSimpleName().toLowerCase().contains("acuity");
        } else {
            return this.origin == ACUITY;
        }
    }

    @JsonIgnore
    @Override
    public boolean thisDetectType() {
        if (origin == NONE) {
            return this.getClass().getSimpleName().toLowerCase().contains("detect");
        } else {
            return this.origin == DETECT;
        }
    }

    @JsonIgnore
    @Override
    public String getShortNameByType() {
        if (thisDetectType()) {
            return "detect";
        } else if (thisAcuityType()) {
            return "acuity";
        } else {
            return "none";
        }
    }

    /*
     * Important so caching operates properly.
     *
     * @return the hash
     */
    @Override
    public int hashCode() {
        int code = 31;
        code ^= this.type.hashCode();
        code ^= this.id.hashCode();

        return code;
    }

    /**
     * Important so caching operates properly.
     * <p>
     * Considers an object of the same class equal if it has the same <code>classname</code> and <code>id</code> properties.
     * <p>
     * Numeric identities (Integer and Long values) are considered equal if they are numerically equal. Other serializable types are evaluated using a simple
     * equality.
     *
     * @param arg0 object to compare
     * @return <code>true</code> if the presented object matches this object
     */
    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null || !(arg0 instanceof AcuityObjectIdentityImpl)) {
            return false;
        }

        AcuityObjectIdentityImpl other = (AcuityObjectIdentityImpl) arg0;

        // Longs with same value should be considered equal
        if (id.longValue() != other.id.longValue()) {
            return false;
        }

        return type.equals(other.type);
    }

    /*
     * Factory class to generate a AcuityAclObjectIdentity from a classname
     */
    public static AcuityObjectIdentity create(String className, Long id) {
        return create(className, id, null);
    }

    public static DrugProgramme createDrugProgramme(String className, Long id, String name) {
        if (DrugProgramme.class.getSimpleName().equals(className)) {
            return new DrugProgramme(id, name);
        } else {
            throw new IllegalArgumentException("Unknown drug programme className " + className);
        }
    }

    public static DrugProgramme createDrugProgramme(String className, Long id) {
        return createDrugProgramme(className, id, null);
    }

    public static ClinicalStudy createClinicalStudy(String className, Long id, String name) {
        if (ClinicalStudy.class.getSimpleName().equals(className)) {
            return new ClinicalStudy(id, name);
        } else {
            throw new IllegalArgumentException("Unknown clinical study className " + className);
        }
    }

    public static ClinicalStudy createClinicalStudy(String className, Long id) {
        return createClinicalStudy(className, id, null);
    }

    public static Dataset createDataset(String className, Long id, String name) {
        if (AcuityDataset.class.getSimpleName().equals(className)) {
            return new AcuityDataset(id, name);
        } else if (DetectDataset.class.getSimpleName().equals(className)) {
            return new DetectDataset(id, name);
        } else {
            throw new IllegalArgumentException("Unknown dataset className " + className);
        }
    }

    public static Dataset createDataset(String className, Long id) {
        return createDataset(className, id, null);
    }

    /*
     * Factory class to generate a AcuityAclObjectIdentity from a classname
     */
    public static AcuityObjectIdentity create(String className, Long id, String name) {
        if (DrugProgramme.class.getSimpleName().equals(className)) {
            return createDrugProgramme(className, id, name);
        }

        if (ClinicalStudy.class.getSimpleName().equals(className)) {
            return createClinicalStudy(className, id, name);
        }

        if (AcuityDataset.class.getSimpleName().equals(className) || DetectDataset.class.getSimpleName().equals(className)) {
            return createDataset(className, id, name);
        }

        throw new IllegalArgumentException("Unknown className " + className);
    }

    @JsonIgnore
    @Override
    public Serializable getIdentifier() {
        return id;
    }

    @Override
    public AcuityObjectIdentity cloneAndReset() {
        try {
            AcuityObjectIdentityWithPermission clone = (AcuityObjectIdentityWithPermission) clone();
            clone.setRolePermissionMask(0);
            clone.setViewPermissionMask(0);

            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Unable to clone", ex);
        }
    }

    /**
     * Detect DG dont have any ids, so we auto generate them
     *
     * @param name of the drug programme
     * @return
     */
    protected final Long generateId(String name) {
        name = MoreObjects.firstNonNull(name, "");
        return (long) Hashing.sha256().newHasher().putString(name, Charsets.UTF_8).hash().asInt();
    }

    @Override
    public boolean isAutoGeneratedId() {
        if (StringUtils.isEmpty(name)) {
            return false;
        } else {
            return generateId(name).equals(id);
        }
    }

    // for json
    public void setAutoGeneratedId(boolean autogenerated) {

    }

    @Override
    public String getSupertype() {
        return supertype;
    }

    // for json
    public void setSupertype(String supertype) {
    }
}
