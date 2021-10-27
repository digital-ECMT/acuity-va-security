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

package com.acuity.va.security.rest.resources;

//import com.acuity.va.model.myanalytics.DrugProgrammeRepository;

import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.dao.AcuityObjectService;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.vasecurity.ClinicalStudyInfo;
import com.acuity.va.security.acl.domain.vasecurity.DatasetInfo;
import com.acuity.va.security.acl.domain.vasecurity.DrugProgrammeInfo;
import com.acuity.va.security.acl.service.VASecurityResourceFactory;
import com.acuity.va.security.rest.resources.services.MyAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Rest resource for service operations for both the Acl and acuity dbs
 *
 * @author Glen
 */
@Component
@Path("/myanalytics")
@Produces({"application/json"})
@PreAuthorize("hasRole('TRAINED_USER')")
public class MyAnalyticsResource extends BaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(MyAnalyticsResource.class);

    @Autowired
    private MyAnalyticsService myAnalyticsService;
    // @Autowired
    //private DrugProgrammeRepository drugProgrammeRepository;
    @Autowired
    private VASecurityResourceFactory vASecurityResourceFactory;
    @Autowired
    private AcuityObjectService acuityObjectService;
    @Autowired
    private AclRepository aclRepository;

    /*
     * Methods:
     * GET:
     *      /myanalytics/DrugProgramme/DrugProgramme/1                gets all the information for DrugProgramme 1
     *      /myanalytics/DrugProgramme/DrugProgramme/1               gets all the information for DrugProgramme 1
     *      /myanalytics/ClinicalStudy/ClinicalStudy/2                gets all the information for ClinicalStudy 2
     *      /myanalytics/ClinicalStudy/ClinicalStudy/2               gets all the information for ClinicalStudy 2
     *      /myanalytics/Dataset/AcuityDataset/121              gets all the information for Dataset 121
     *      /myanalytics/Dataset/DetectDataset/121             gets all the information for Dataset 121
     *      /myanalytics/DrugProgrammes                                    gets all the drug programmes with studies
     */
    /**
     * Gets the Drug Programme Info
     *
     * <code>
     * /myanalytics/DrugProgramme/DrugProgramme/1 gets all the information regarding acuitys DrugProgram 1
     * /myanalytics/DrugProgramme/DrugProgramme/1 gets all the information regarding detects DrugProgram 1
     * </code>
     *
     * @return Status 200 if has permission otherwise 401
     */
    @GET
    @Path("/DrugProgramme/{drugProgramme}/{drugProgrammeId}")
    @PreAuthorize("@pe.hasDPPermission(authentication, #drugProgramme, #drugProgrammeId) or hasRole('DEVELOPMENT_TEAM')")
    public DrugProgrammeInfo getDrugProgrammeInfo(@PathParam("drugProgramme") String drugProgramme, @PathParam("drugProgrammeId") Long drugProgrammeId) {
        LOG.debug("GET: info for DrugProgramme " + drugProgrammeId);

        try {

            DrugProgramme drugProgrammeObj = AcuityObjectIdentityImpl.createDrugProgramme(drugProgramme, drugProgrammeId);
            String drugProgrammeName = aclRepository.getAclName(drugProgrammeObj.getClass().getName(), drugProgrammeId);
            drugProgrammeObj.setName(drugProgrammeName);

            DrugProgrammeInfo drugProgrammeInfo = vASecurityResourceFactory.get().getDrugProgrammeInfo(drugProgrammeObj);

            if (drugProgrammeInfo != null) {

                List<ClinicalStudy> listClinicalStudiesForDrugProgramme = acuityObjectService.listClinicalStudiesForDrugProgramme(drugProgrammeObj);
                drugProgrammeInfo.setStudyModules(listClinicalStudiesForDrugProgramme);

                drugProgrammeInfo.setUsersAmount(securityAclService.getGrantedUsersAmountForDrugProgramme(drugProgrammeObj));

                return drugProgrammeInfo;
            } else {
                throw new org.springframework.security.acls.model.NotFoundException(drugProgrammeObj + " not found");
            }
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("DrugProgramme " + drugProgrammeId + " not found", nfe);
            throw new WebApplicationException("DrugProgramme " + drugProgrammeId + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Gets the Clinical Study Info
     *
     * <code>
     * /myanalytics/ClinicalStudy/AcuityClinicalStudy/1 gets all the information regarding acuitys ClinicalStudy 1
     * /myanalytics/ClinicalStudy/DetectClinicalStudy/1 gets all the information regarding detects ClinicalStudy 1
     * </code?
     *
     * @return Status 200 if has permission otherwise 401
     */
    @GET
    @Path("/ClinicalStudy/{clinicalStudy}/{clinicalStudyId}")
    @PreAuthorize("@pe.hasStudyPermission(authentication, #clinicalStudy, #clinicalStudyId) or hasRole('DEVELOPMENT_TEAM')")
    public ClinicalStudyInfo getClinicalStudyInfo(@PathParam("clinicalStudy") String clinicalStudy, @PathParam("clinicalStudyId") Long clinicalStudyId) {
        LOG.debug("GET: info for ClinicalStudy " + clinicalStudyId);

        try {
            
            ClinicalStudy clinicalStudyObj = AcuityObjectIdentityImpl.createClinicalStudy(clinicalStudy, clinicalStudyId);
            String clinicalStudyName = aclRepository.getAclName(clinicalStudyObj.getClass().getName(), clinicalStudyId);
            clinicalStudyObj.setName(clinicalStudyName);

            ClinicalStudyInfo clinicalStudyInfo = vASecurityResourceFactory.get().getClinicalStudyInfo(clinicalStudyObj);

            if (clinicalStudyInfo != null) {
                List<Dataset> listDatasetForClinicalStudy = acuityObjectService.listDatasetsForClinicalStudy(clinicalStudyObj);
                clinicalStudyInfo.setDatasets(listDatasetForClinicalStudy);

                return clinicalStudyInfo;
            } else {
                throw new org.springframework.security.acls.model.NotFoundException(clinicalStudyId + " not found");
            }
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("ClinicalStudy " + clinicalStudyId + " not found", nfe);
            throw new WebApplicationException("ClinicalStudy " + clinicalStudyId + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Gets the Dataset Info.
     *
     * <code>
     * /myanalytics/Dataset/AcuityDataset/1 gets all the information regarding acuitys Dataset 1
     * /myanalytics/Dataset/DetectDataset/1 gets all the information regarding detects Dataset 1
     * </code>
     *
     * @return Status 200 if has permission otherwise 401
     */
    @GET
    @Path("/Dataset/{dataset}/{datasetId}")
    @PreAuthorize("@pe.hasVisPermission(authentication, #dataset, #datasetId) "
            + "or hasRole('DEVELOPMENT_TEAM') "
            + "or @pe.isTrainingData(#datasetId)")
    public DatasetInfo getDatasetInfo(@PathParam("dataset") String dataset, @PathParam("datasetId") Long datasetId) {
        LOG.debug("GET: info for Dataset " + datasetId);

        try {
            Dataset datasetObj = AcuityObjectIdentityImpl.createDataset(dataset, datasetId);

            DatasetInfo datasetInfo = vASecurityResourceFactory.get().getDatasetInfo(datasetObj);

            if (datasetInfo != null) {
                datasetInfo.setUsersAmount(securityAclService.getGrantedUsersAmountForDataset(datasetObj));
                return datasetInfo;
            } else {
                throw new org.springframework.security.acls.model.NotFoundException(datasetId + " not found");
            }
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Dataset " + datasetId + " not found", nfe);
            throw new WebApplicationException("Dataset " + datasetId + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Gets all the Drug Programmes with their studies
     * <p/>
     * /myanalytics/DrugProgrammes
     *
     * @return Status 200 if has permission otherwise 401
     */
//    @GET
//    @Path("/DrugProgrammes")
//    @PreAuthorize("hasRole('TRAINED_USER')")
//    public Map<String, Collection<String>> getDrugProgrammes() {
//        LOG.debug("GET: all DrugProgrammes with studies");
//
//        return drugProgrammeRepository.listStudiesWithDrugProgrammes();
//    }
}
