package com.acuity.va.security.acl.service;

import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithInitialLockDown;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.vasecurity.ClinicalStudyInfo;
import com.acuity.va.security.acl.domain.vasecurity.DatasetInfo;
import com.acuity.va.security.acl.domain.vasecurity.DrugProgrammeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Interacts with the remote acuity and detect web services for information about their data
 *
 * @author Glen
 */
@Data
public class VASecurityResourceClient {

    private static final Logger LOG = LoggerFactory.getLogger(VASecurityResourceClient.class);

    private static final String PATH = "/resources/security";
    protected WebTarget target = null;
    protected String username;
    protected String password;
    protected String url;

    public VASecurityResourceClient(String username, String password, String baseUrl) {
        this.username = username;
        this.password = password;
        this.url = baseUrl + PATH;

        addBasicAuth();
    }

    public final void addBasicAuth() {
        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basicBuilder()
            .nonPreemptive()
            .credentials(username, password)
            .build();
        Client client = ClientBuilder.newClient();
        client.register(authFeature);

        target = client.target(url);
    }

    /**
     * Lists all the ROIs in the remote db
     *
     * @return List<AcuityObjectIdentity> all the rois
     * @throws java.lang.IllegalStateException if basic auth username or password incorrect
     */
    public List<AcuityObjectIdentityWithInitialLockDown> loadRois() {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Looking up rois in VAHub");

        WebTarget newTarget = target.path("/info/rois");

        LOG.debug("URL " + newTarget.getUri());
        Response response = newTarget.request(MediaType.APPLICATION_JSON).header("Content-Type", APPLICATION_JSON).accept(APPLICATION_JSON).get(Response.class);
        if (response.getStatus() == 401) { // invalid username or password
            throw new IllegalStateException("Username and/or password incorrect for remote server basic auth");
        }

        // System.out.println(response.getStatus());
        //  Hack to read in data from old class package com.acuity.visualisations.security.acl.domain data from acuity
        //  Also to change old DetectDrugProgramme and AcuityDrugProgramme to DrugProgramme
        //  and to change old DetectClinicalStudy and AcuityClinicalStudy to ClinicalStudy
        String responseUnchanged = response.readEntity(String.class);
        String responseChanged = responseUnchanged.replaceAll("com.acuity.visualisations.security.acl.domain.",
                "com.acuity.va.security.acl.domain.");
        responseChanged = responseChanged.replaceAll("com.acuity.va.security.acl.domain.DetectDrugProgramme",
                "com.acuity.va.security.acl.domain.DrugProgramme");
        responseChanged = responseChanged.replaceAll("com.acuity.va.security.acl.domain.AcuityDrugProgramme",
                "com.acuity.va.security.acl.domain.DrugProgramme");
        responseChanged = responseChanged.replaceAll("com.acuity.va.security.acl.domain.DetectClinicalStudy",
                "com.acuity.va.security.acl.domain.ClinicalStudy");
        responseChanged = responseChanged.replaceAll("com.acuity.va.security.acl.domain.AcuityClinicalStudy",
                "com.acuity.va.security.acl.domain.ClinicalStudy");
        ObjectMapper mapper = new ObjectMapper();

        try {
            return Arrays.asList(mapper.readValue(responseChanged, AcuityObjectIdentityWithInitialLockDown[].class));
        } catch (IOException ex) {
            LOG.error("Error turning response string {} into AcuityObjectIdentityWithInitialLockDownAndUsers[]", responseUnchanged, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the DatasetInfo from the remote web service
     *
     * @return DatasetInfo dataset info
     * @throws java.lang.IllegalStateException if basic auth username or password incorrect
     */
    public DatasetInfo getDatasetInfo(Dataset dataset) {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Looking up DatasetInfo for " + dataset);

        WebTarget newTarget = target.path("/info/Dataset/" + dataset.getClass().getSimpleName() + "/" + dataset.getId());

        LOG.debug("URL " + newTarget.getUri());
        Response response = newTarget.request(APPLICATION_JSON).header("Content-Type", APPLICATION_JSON).accept(APPLICATION_JSON).get(Response.class);
        if (response.getStatus() == 401) { // invalid username or password
            throw new IllegalStateException("Username and/or password incorrect for remote server basic auth");
        }
        return response.readEntity(DatasetInfo.class);
    }

    /**
     * Gets the ClinicalStudyInfo from the remote web service
     *
     * @return ClinicalStudyInfo clinicalStudy info
     * @throws java.lang.IllegalStateException if basic auth username or password incorrect
     */
    public ClinicalStudyInfo getClinicalStudyInfo(ClinicalStudy clinicalStudy) {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Looking up ClinicalStudyInfo for " + clinicalStudy);

        WebTarget newTarget = target.path("/info/ClinicalStudy/" + clinicalStudy.getClass().getSimpleName() + "/" + clinicalStudy.getId());

        LOG.debug("URL " + newTarget.getUri());
        Response response = newTarget.request(APPLICATION_JSON).header("Content-Type", APPLICATION_JSON).accept(APPLICATION_JSON).get(Response.class);
        if (response.getStatus() == 401) { // invalid username or password
            throw new IllegalStateException("Username and/or password incorrect for remote server basic auth");
        }

        return response.readEntity(ClinicalStudyInfo.class);
    }

    /**
     * Gets the DrugProgrammeInfo from the remote web service
     *
     * @return DrugProgrammeInfo DrugProgramme info
     * @throws java.lang.IllegalStateException if basic auth username or password incorrect
     */
    public DrugProgrammeInfo getDrugProgrammeInfo(DrugProgramme drugProgramme) {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Looking up DrugProgrammeInfo for " + drugProgramme);

        WebTarget newTarget = target.path("/info/DrugProgramme/" + drugProgramme.getClass().getSimpleName() + "/" + drugProgramme.getId());

        LOG.debug("URL " + newTarget.getUri());
        Response response = newTarget.request(APPLICATION_JSON).header("Content-Type", APPLICATION_JSON).accept(APPLICATION_JSON).get(Response.class);
        if (response.getStatus() == 401) { // invalid username or password
            throw new IllegalStateException("Username and/or password incorrect for remote server basic auth");
        }

        return response.readEntity(DrugProgrammeInfo.class);
    }
}
