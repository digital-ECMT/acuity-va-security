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

import com.acuity.va.auditlogger.annotation.LogArg;
import com.acuity.va.auditlogger.annotation.LogOperation;
import com.acuity.va.auditlogger.domain.AcuityObjectIdentityForAudit;
import com.acuity.va.auditlogger.service.LockdownService;
import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Rest resource for logging actions of the current user, methods take no action, but trigger logging via the LogOperation annotation / aspect.
 *
 * @author Andrew
 */
@Component
@Path("/logging")
@Produces({"application/json"})
@PreAuthorize("hasRole('TRAINED_USER')")
public class LoggingResource {

    private static final Logger LOG = LoggerFactory.getLogger(MyResource.class);

    @Autowired
    private LockdownService lockdownService;
    @Autowired
    private AclRepository aclRepository;

    /**
     * Creates a new log entry for monitored site state change Methods: POST:
     * /vis_log/webApp/{isWebapp}/moduleType/{moduleType}/analysisName/{analysisName}?tab={tabName}&datasetId={datasetId}
     *
     */
    @LogOperation(name = "VIEWCHANGE", value = {
        @LogArg(arg = 0, name = "ISWEBAPP"),
        @LogArg(arg = 1, name = "MODULETYPE"),
        @LogArg(arg = 2, name = "ANALYSISNAME"),
        @LogArg(arg = 3, name = "TAB", persistNull = false),
        @LogArg(arg = 4, name = "DATASET_ID")
    })
    @POST
    @Path("/dataset_log/webApp/{isWebapp}/moduleType/{moduleType}/analysisName/{analysisName}")
    public Response viewChangeForDataset(@PathParam("isWebapp") Boolean isWebapp,
            @PathParam("moduleType") String moduleType,
            @PathParam("analysisName") String analysisName,
            @QueryParam("tab") String tabName,
            @QueryParam("datasetId") Long datasetId) {

        LOG.debug("viewChangeForDataset: moduleType: " + moduleType + ", analysisName: " + analysisName + ", tabName: "
                + tabName + ", datasetId: " + datasetId);
        return Response.ok().build();
    }

    /**
     * Creates a new log entry for monitored site state change Methods: POST:
     * /study_log/webApp/{isWebapp}/moduleType/{moduleType}/analysisName/{analysisName}?studyId={studyId}
     *
     */
    @LogOperation(name = "VIEWCHANGE", value = {
        @LogArg(arg = 0, name = "ISWEBAPP"),
        @LogArg(arg = 1, name = "MODULETYPE"),
        @LogArg(arg = 2, name = "ANALYSISNAME"),
        @LogArg(arg = 3, name = "STUDY_ID")
    })
    @POST
    @Path("/study_log/webApp/{isWebapp}/moduleType/{moduleType}/analysisName/{analysisName}")
    public Response viewChangeForStudy(@PathParam("isWebapp") Boolean isWebapp,
            @PathParam("moduleType") String moduleType,
            @PathParam("analysisName") String analysisName,
            @QueryParam("studyId") Long studyId) {

        LOG.debug("viewChangeForStudy: moduleType: " + moduleType + ", analysisName: " + analysisName + ", studyId: " + studyId);
        return Response.ok().build();
    }

    /**
     * Creates a new log entry for monitored site state change Methods: POST:
     * /drugprogramme_log/webApp/{isWebapp}/moduleType/{moduleType}/analysisName/{analysisName}?drugprogrammeId={drugprogrammeId}
     *
     */
    @LogOperation(name = "VIEWCHANGE", value = {
        @LogArg(arg = 0, name = "ISWEBAPP"),
        @LogArg(arg = 1, name = "MODULETYPE"),
        @LogArg(arg = 2, name = "ANALYSISNAME"),
        @LogArg(arg = 3, name = "DRUGPROGRAMME_ID")
    })
    @POST
    @Path("/drugprogramme_log/webApp/{isWebapp}/moduleType/{moduleType}/analysisName/{analysisName}")
    public Response viewChangeForDrugProgramme(@PathParam("isWebapp") Boolean isWebapp,
            @PathParam("moduleType") String moduleType,
            @PathParam("analysisName") String analysisName,
            @QueryParam("drugprogrammeId") Long drugprogrammeId) {

        LOG.debug("viewChangeForDrugProgramme: moduleType: " + moduleType + ", analysisName: " + analysisName + ", drugprogrammeId: " + drugprogrammeId);
        return Response.ok().build();
    }

    @GET
    @PreAuthorize("hasPermission(#objectIdentityId, #objectIdentityClassname, 'EDIT_AUTHORISED_USERS')")
    @Path("/{objectIdentityClassname}/{objectIdentityId}/lockdown")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLockdownHistory(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) throws IOException {

        Dataset vis = AcuityObjectIdentityImpl.createDataset(objectIdentityClassname, objectIdentityId);
        String datasetName = aclRepository.getAclName(vis.getClass().getName(), objectIdentityId);

        AcuityObjectIdentity datasetParentDrugProgrammeFromDB = aclRepository.getDatasetParentDrugProgrammeFromDB(vis);

        String output = lockdownService.getLatestLockdown(toROIForAudit(vis, datasetName), toROIForAudit(datasetParentDrugProgrammeFromDB, null));

        if (output != null) {
            return output;
        } else {
            throw new WebApplicationException("Lockdown not found", Response.Status.NOT_FOUND);
        }
    }

    private AcuityObjectIdentityForAudit toROIForAudit(AcuityObjectIdentity roi, String name) {
        return new AcuityObjectIdentityForAudit(roi.getClass().getSimpleName(), roi.getId(), name);
    }
}
