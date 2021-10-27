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

import com.acuity.va.security.acl.domain.AclObject;
import com.acuity.va.security.acl.domain.AclRemoteLocation;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.DrugStudyDataset;
import com.acuity.va.security.acl.domain.GroupWithItsLockdownDatasets;
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.SidWithPermissionMaskAndGranted;
import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Repository for Acl queries.
 *
 * @author Glen
 */
@Repository
@Transactional(value = "security", readOnly = true)
public interface AclRepositoryImpl {

    /**
     * Lists all the projects, studies and datasets with their name and type.
     * <p/>
     * Returns the order by projects then studies then datasets in id order
     * <p/>
     * Will return for example:
     * <p/>
     * <code>
     * Ie ID   Name                          Type                                                      moduleType
     * 4    STUDY1234                      com.acuity.va.security.acl.domain.DrugProgramme
     * 14   STUDY0002                   com.acuity.va.security.acl.domain.AcuityClinicalStudy
     * 21   STUDY0003                   com.acuity.va.security.acl.domain.AcuityClinicalStudy
     * 5    ACUITY_Safety_STDY4547_Study_2  com.acuity.va.security.acl.domain.AcuityDataset          Safety
     * 5    ACUITY_Safety_STDY4547_Study_22 com.acuity.va.security.acl.domain.AcuityDataset          Safety
     * </code>
     */
    @Select("SELECT "
            + "  aoi.object_id_identity AS id, "
            + "  ac.class AS type, "
            + "  ao.name AS name, "
            + "  ao.code AS code, "
            + "  ao.parent_drug_programme AS drugProgramme, "
            + "  ao.parent_clinical_study AS clinicalStudyName, "
            + "  ao.parent_clinical_study_code AS clinicalStudyCode, "
            + "  ao.lockdown AS lockdown, "
            + "  aoi.entries_inheriting AS inherited "
            + "  FROM acl_object_identity aoi "
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id "
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " ORDER BY id")
    @TypeDiscriminator(column = "type",
            cases = {
                    @Case(value = "com.acuity.va.security.acl.domain.AcuityDataset", type = AcuityDataset.class),
                    @Case(value = "com.acuity.va.security.acl.domain.DetectDataset", type = DetectDataset.class),
                    @Case(value = "com.acuity.va.security.acl.domain.ClinicalStudy", type = ClinicalStudy.class),
                    @Case(value = "com.acuity.va.security.acl.domain.DrugProgramme", type = DrugProgramme.class)
            })
    List<AcuityObjectIdentityWithPermissionAndLockDown> listObjectIdentities();

    /**
     * Gets the acuity_instance parent drug programme
     */
    @Select("SELECT "
            + "  aoi.object_id_identity AS id, "
            + "  ac.class AS type, "
            + "  ao.name AS name, "
            + "  ao.parent_drug_programme AS drugProgramme "
            + " FROM acl_object_identity aoi"
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id"
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE ac.class = #{dataset.drugProgrammeClassString} AND ao.name = "
            + "(SELECT ao.parent_drug_programme AS datasetParentDrugProgramme "
            + " FROM acl_object_identity aoi"
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id"
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE aoi.object_id_identity = #{dataset.id} AND ac.class = #{dataset.type})")
    @TypeDiscriminator(column = "type",
            cases = {
                @Case(value = "com.acuity.va.security.acl.domain.DrugProgramme", type = DrugProgramme.class)
            })
    AcuityObjectIdentity getDatasetParentDrugProgrammeFromDB(@Param("dataset") Dataset dataset);

    @Select("SELECT "
            + "  aoi.object_id_identity AS id, "
            + "  ac.class AS type, "
            + "  ao.name AS name, "
            + "  ao.parent_drug_programme AS drugProgramme "
            + " FROM acl_object_identity aoi"
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id"
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE ac.class = #{dataset.drugProgrammeClassString} AND ao.name = #{dataset.drugProgramme}")
    @TypeDiscriminator(column = "type",
            cases = {
                @Case(value = "com.acuity.va.security.acl.domain.DrugProgramme", type = DrugProgramme.class)
            })
    AcuityObjectIdentity getDatasetParentDrugProgrammeFromObj(@Param("dataset") Dataset dataset);

    @Select("SELECT "
            + "  aoi.object_id_identity AS id, "
            + "  ac.class AS type, "
            + "  ao.name AS name, "
            + "  ao.parent_drug_programme AS drugProgramme "
            + " FROM acl_object_identity aoi"
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id"
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE ac.class = #{clinicalStudy.drugProgrammeClassString} AND ao.name = #{clinicalStudy.drugProgramme}")
    @TypeDiscriminator(column = "type",
            cases = {
                @Case(value = "com.acuity.va.security.acl.domain.DrugProgramme", type = DrugProgramme.class)
            })
    AcuityObjectIdentity getClinicalStudyParentDrugProgrammeFromObj(@Param("clinicalStudy") ClinicalStudy clinicalStudy);

    /**
     * Removed all the user aces from the db using the users sid/group name
     */
    @Delete("DELETE FROM acl_entry WHERE acl_entry.sid IN (SELECT id FROM acl_sid WHERE sid = #{userId})")
    void removeAllAcesForSid(String userId);

    /**
     * Gets the clinical study parent drug programme
     */
    @Select("SELECT "
            + "  aoi.object_id_identity AS id, "
            + "  ac.class AS type, "
            + "  ao.name AS name, "
            + "  ao.parent_drug_programme AS drugProgramme "
            + " FROM acl_object_identity aoi"
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id"
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE ac.class = #{clinicalStudy.drugProgrammeClassString} AND ao.name = "
            + "(SELECT ao.parent_drug_programme AS visParentDrugProgramme "
            + " FROM acl_object_identity aoi"
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id"
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE aoi.object_id_identity = #{clinicalStudy.id} AND ac.class = #{clinicalStudy.type})")
    @TypeDiscriminator(column = "type",
            cases = {
                @Case(value = "com.acuity.va.security.acl.domain.DrugProgramme", type = DrugProgramme.class)
            })
    AcuityObjectIdentity getClinicalStudyParentDrugProgrammeFromDB(@Param("clinicalStudy") ClinicalStudy clinicalStudy);

    /**
     * Gets all the users for a particular permission and aclClassName.
     * <p/>
     * Ie find all the users for clinical studies with Permission DRUG_PROGRAM_DATA_OWNER, ie mask 480
     *
     * @param aclClassName either DrugProgramme, ClinicalStudy, Dataset acl_class
     * @param permissionsAsRoleMask permission role mask the user is assigned to, ie 480 for data owner
     */
    @Select("SELECT DISTINCT acl_sid.sid FROM acl_object_identity "
            + "JOIN acl_class ON acl_class.id = acl_object_identity.object_id_class "
            + "JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id "
            + "JOIN acl_sid ON acl_sid.id = acl_entry.sid "
            + "WHERE acl_class.class = #{aclClassName} AND acl_entry.mask = #{permissionsAsRoleMask} "
            + "  AND acl_entry.granting = true")
    List<String> listUsersByPermissionMask(
            @Param("aclClassName") String aclClassName,
            @Param("permissionsAsRoleMask") Integer permissionsAsRoleMask);

    /**
     * List all the acls in the database
     */
    @Select("SELECT * FROM acl_object_identity")
    List<Map<String, Object>> listAcls();

    /**
     * High-level information for the number of users/group, mask and granted for a drug p/clinical study/dataset.
     * <p/>
     * acl_class.class = com....DrugProgramme is DrugProgramme and acl_entry.mask in as list of all the possible AcuityCumulativePermissionsAsRoles masks
     * ie ( != 522240 (DEVELOPER).
     *
     * @param aclClassName either com....DrugProgramme Clinical Study from the acl_class
     * @param aclObjectIdentityId internal acl id of the drug programme/clinical study/dataset
     * @return list of SidWithPermissionMaskAndGranted
     */
    @Select("SELECT distinct "
            + " acl_sid.sid AS sid, acl_sid.principal as isuser, "
            + " acl_entry.mask AS permissionMask, "
            + " acl_entry.granting AS granted "
            + " FROM acl_object_identity "
            + " JOIN acl_class ON acl_class.id = acl_object_identity.object_id_class "
            + " JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id "
            + " JOIN acl_sid ON acl_sid.id = acl_entry.SID "
            + " WHERE acl_class.class = #{aclClassName} "
            + " AND acl_object_identity.object_id_identity = #{aclObjectIdentityId} "
            + " AND acl_entry.mask != 522240")
    List<SidWithPermissionMaskAndGranted> getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(
            @Param("aclClassName") String aclClassName,
            @Param("aclObjectIdentityId") Long aclObjectIdentityId);

    /**
     * Gets the acl name from the acl id and acl classname
     */
    @Select("SELECT ao.name  "
            + " FROM acl_object_identity aoi "
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id "
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE aoi.object_id_identity = #{aclObjectIdentity} AND ac.class = #{aclClassName}")
    String getAclName(@Param("aclClassName") String aclClassName, @Param("aclObjectIdentity") Long aclObjectIdentity);

    /**
     * List all the enabled remote acl locations, ie for acuity and detect
     */
    @Select("SELECT name, base_url AS baseUrl, enabled "
            + " FROM acl_remote "
            + " WHERE enabled = true")
    List<AclRemoteLocation> listAllRemoteAclsLocations();

    /**
     * List all the enabled remote acl locations, ie for acuity and detect
     */
    @Select("SELECT name, base_url AS baseUrl, enabled "
            + " FROM acl_remote "
            + " WHERE name = 'detect'")
    List<AclRemoteLocation> listDetectRemoteAclsLocations();

    /**
     * Insert a new aclObject
     */
    @Insert("INSERT INTO acl_object "
            + "   (name, code, acl_object_identity_id, lockdown, parent_drug_programme, parent_clinical_study, parent_clinical_study_code, acl_type)"
            + " VALUES (#{name,jdbcType=VARCHAR}, #{code,jdbcType=VARCHAR}, #{aclObjectIdentityPkId,jdbcType=NUMERIC}, #{lockdown}, "
            + " #{parentDrugProgramme,jdbcType=VARCHAR}, #{parentClinicalStudyName,jdbcType=VARCHAR}, #{parentClinicalStudyCode,jdbcType=VARCHAR},"
            + " #{aclType,jdbcType=VARCHAR})")
    int insertAclObject(AclObject aclObject);

    /**
     * Updates an aclObject.
     *
     * No updates on DP. DS updates both CS updates on name, parentClinicalStudyName will always be null
     */
    @Update("UPDATE acl_object "
            + "  SET name = #{name,jdbcType=VARCHAR}, parent_clinical_study = #{parentClinicalStudyName,jdbcType=VARCHAR} "
            + " WHERE id = #{id}")
    int updateAclObject(AclObject aclObject);

    /**
     * Sets lockdown for an aclObject
     */
    @Update("UPDATE acl_object "
            + "  SET lockdown = #{lockdownStatus}"
            + " WHERE id = #{id}")
    int setLockdown(@Param("id") Long id, @Param("lockdownStatus") boolean lockdownStatus);

    /**
     * Gets the lockdown for an aclObject
     */
    @Select("SELECT lockdown "
            + " FROM acl_object "
            + " WHERE id = #{id}")
    boolean isLockdown(@Param("id") Long id);

    /**
     * Checks if the aclObject is already in the db
     */
    @Select("SELECT ao.id  "
            + " FROM acl_object_identity aoi "
            + "   JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id "
            + "   JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + " WHERE aoi.object_id_identity = #{aclObjectIdentity} AND ac.class = #{aclClassName}")
    Long findAclObjectId(@Param("aclClassName") String aclClassName, @Param("aclObjectIdentity") Long aclObjectIdentity);

    // TODO need to add study information
    @Select(" SELECT aoi.object_id_identity AS id, ac.class AS type, ao.name AS name, "
            + "      ao.module_type AS moduleType, ao.parent_drug_programme AS drugProgramme, ao.parent_clinical_study AS study "
            + "   FROM acl_object_identity aoi"
            + "     JOIN acl_object ao ON ao.acl_object_identity_id = aoi.id "
            + "     JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + "    WHERE ac.class = 'com.acuity.va.security.acl.domain.AcuityDataset'"
            + "    AND or ac.class = 'com.acuity.va.security.acl.domain.DetectDataset'"
            + "   ORDER BY id")
    List<DrugStudyDataset> listDrugsStudiesInstances();

    @Select(" SELECT acl_sid.sid FROM acl_object_identity "
            + "JOIN acl_object on acl_object.acl_object_identity_id = acl_object_identity.id "
            + "JOIN acl_class on acl_object_identity.object_id_class = acl_class.id "
            + "JOIN acl_entry on acl_entry.acl_object_identity = acl_object_identity.id "
            + "JOIN acl_sid on acl_sid.id = acl_entry.sid "
            + "WHERE lockdown = true AND principal = false "
            + "AND class = 'com.acuity.va.security.acl.domain.DetectDataset' "
            + "AND acl_sid.sid != 'DEVELOPMENT_TEAM'")
    List<String> listGroupsInLockdown();

    @Select(" SELECT acl_sid.sid AS sid, name AS ds_name FROM acl_object_identity "
            + "JOIN acl_object on acl_object.acl_object_identity_id = acl_object_identity.id "
            + "JOIN acl_class on acl_object_identity.object_id_class = acl_class.id "
            + "JOIN acl_entry on acl_entry.acl_object_identity = acl_object_identity.id "
            + "JOIN acl_sid on acl_sid.id = acl_entry.sid "
            + "WHERE lockdown = true AND principal = false "
            + "AND class = 'com.acuity.va.security.acl.domain.DetectDataset' "
            + "AND acl_sid.sid != 'DEVELOPMENT_TEAM'")
    @ResultMap("com.acuity.va.security.acl.dao.AclRepositoryImpl.listGroupsAndTheirDatasetsInLockdown")
    List<GroupWithItsLockdownDatasets> listGroupsAndTheirDatasetsInLockdown();

    @Select(" SELECT object_id_identity FROM acl_object_identity "
            + "  JOIN acl_object ON acl_object.acl_object_identity_id = acl_object_identity.id "
            + "  JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id "
            + "  WHERE acl_type = 'DS' AND class = 'com.acuity.va.security.acl.domain.AcuityDataset' "
            + "  AND parent_clinical_study_code = #{studyId}")
    Long findVasecurityIdFromStudyId(@Param("studyId") String studyId);

    /**
     * Get acl object identity id
     */
    @Select("SELECT aoi.id  "
            + "FROM acl_object_identity aoi "
            + "JOIN acl_class ac ON ac.id = aoi.object_id_class "
            + "WHERE aoi.object_id_identity = #{aclObjectIdentity} AND ac.class = #{aclClassName}")
    Long findAclObjectIdentityId(@Param("aclClassName") String aclClassName,
                                 @Param("aclObjectIdentity") Long aclObjectIdentity);

    /**
     * Remove the entries from the db
     */
    @Delete({"<script>",
            "DELETE ",
            "FROM acl_entry ae ",
            "WHERE ae.acl_object_identity IN ",
                "<foreach item='object' index='index' collection='aclObjectIdentityIds'",
                    "open='(' separator=', ' close=')'>",
                    "#{object}",
                "</foreach>",
            "</script>"})
    void removeEntries(@Param("aclObjectIdentityIds") List<Long> aclObjectIdentityIds);

    /**
     * Remove the objects from the db
     */
    @Delete({"<script>",
            "DELETE ",
            "FROM acl_object ao ",
            "WHERE ao.acl_object_identity_id IN ",
                "<foreach item='object' index='index' collection='aclObjectIdentityIds'",
                    "open='(' separator=', ' close=')'>",
                    "#{object}",
                "</foreach>",
            "</script>"})
    void removeObjects(@Param("aclObjectIdentityIds") List<Long> aclObjectIdentityIds);

    /**
     * Remove the object identities from the db
     */
    @Delete({"<script>",
            "DELETE ",
            "FROM acl_object_identity aoi ",
            "WHERE aoi.id IN ",
                "<foreach item='object' index='index' collection='aclObjectIdentityIds'",
                    "open='(' separator=', ' close=')'>",
                    "#{object}",
                "</foreach>",
            "</script>"})
    void removeObjectIdentities(@Param("aclObjectIdentityIds") List<Long> aclObjectIdentityIds);
}
