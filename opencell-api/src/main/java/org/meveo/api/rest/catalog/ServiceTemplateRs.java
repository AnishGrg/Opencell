package org.meveo.api.rest.catalog;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.catalog.ServiceTemplateDto;
import org.meveo.api.dto.response.catalog.GetServiceTemplateResponseDto;
import org.meveo.api.rest.IBaseRs;

/**
 * Web service for managing {@link org.meveo.model.catalog.ServiceTemplate}.
 * 
 * @author Edward P. Legaspi
 **/
@Path("/catalog/serviceTemplate")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface ServiceTemplateRs extends IBaseRs {

    /**
     * Create a new service template.
     * 
     * @param postData The service template's data
     * @return Request processing status
     */
    @Path("/")
    @POST
    ActionStatus create(ServiceTemplateDto postData);

    /**
     * Update an existing service template.
     * 
     * @param postData The service template's data
     * @return Request processing status
     */
    @Path("/")
    @PUT
    ActionStatus update(ServiceTemplateDto postData);

    /**
     * Find a service template with a given code.
     * 
     * @param serviceTemplateCode The service template's code
     * @return Return serviceTemplate
     */
    @Path("/")
    @GET
    GetServiceTemplateResponseDto find(@QueryParam("serviceTemplateCode") String serviceTemplateCode);

    /**
     * Remove service template with a given code.
     * 
     * @param serviceTemplateCode The service template's code
     * @return Request processing status
     */
    @Path("/{serviceTemplateCode}")
    @DELETE
    ActionStatus remove(@PathParam("serviceTemplateCode") String serviceTemplateCode);

    /**
     * Create new or update an existing service template
     * 
     * @param postData The service template's data
     * @return Request processing status
     */
    @Path("/createOrUpdate")
    @POST
    ActionStatus createOrUpdate(ServiceTemplateDto postData);

}
