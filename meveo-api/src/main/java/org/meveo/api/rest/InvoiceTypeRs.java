package org.meveo.api.rest;

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
import org.meveo.api.dto.billing.InvoiceTypeDto;
import org.meveo.api.dto.response.GetInvoiceTypeResponse;
import org.meveo.api.dto.response.GetInvoiceTypesResponse;
import org.meveo.api.rest.security.RSSecured;

/**
 * Web service for managing {@link org.meveo.model.billing.InvoiceType}
 * 
 * @author Edward P. Legaspi
 **/
@Path("/invoiceType")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@RSSecured
public interface InvoiceTypeRs extends IBaseRs {

    /**
     * Create invoiceType. Description per language can be defined
     * 
     * @param invoiceTypeDto
     * @return
     */
    @Path("/")
    @POST
    public ActionStatus create(InvoiceTypeDto invoiceTypeDto);

    /**
     * Update invoiceType. Description per language can be defined
     * 
     * @param invoiceTypeDto
     * @return
     */
    @Path("/")
    @PUT
    public ActionStatus update(InvoiceTypeDto invoiceTypeDto);

    /**
     * Search invoiceType with a given code.
     * 
     * @param invoiceTypeCode
     * @return
     */
    @Path("/")
    @GET
    public GetInvoiceTypeResponse find(@QueryParam("invoiceTypeCode") String invoiceTypeCode);

    /**
     * Remove invoiceType with a given code.
     * 
     * @param invoiceTypeCode
     * @return
     */
    @Path("/{invoiceTypeCode}")
    @DELETE
    public ActionStatus remove(@PathParam("invoiceTypeCode") String invoiceTypeCode);

    @Path("/createOrUpdate")
    @POST
    public ActionStatus createOrUpdate(InvoiceTypeDto invoiceTypeDto);

    @Path("/list")
    @GET
    public GetInvoiceTypesResponse list();
}
