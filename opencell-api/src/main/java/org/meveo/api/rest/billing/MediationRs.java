package org.meveo.api.rest.billing;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.billing.CdrListDto;
import org.meveo.api.dto.billing.PrepaidReservationDto;
import org.meveo.api.dto.response.billing.CdrReservationResponseDto;
import org.meveo.api.rest.IBaseRs;

@Path("/billing/mediation")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface MediationRs extends IBaseRs {

    /**
     * Accepts a list of CDR line. This CDR is parsed and created as EDR. CDR is same format use in mediation job
     * 
     * @param postData String of CDR
     * @return Request processing status 
     */
    @POST
    @Path("/registerCdrList")
    ActionStatus registerCdrList(CdrListDto postData);

    /**
     * Same as registerCdrList, but at the same process rate the EDR created
     * 
     * @param cdr String of CDR
     * @return Request processing status 
     */
    @POST
    @Path("/chargeCdr")
    ActionStatus chargeCdr(String cdr);

    /**
     * Allows the user to reserve a CDR, this will create a new reservation entity attached to a wallet operation. A reservation has expiration limit save in the provider entity
     * (PREPAID_RESRV_DELAY_MS)
     * 
     * @param cdr String of CDR
     * @return Available quantity and reservationID is returned
     */
    @POST
    @Path("/reserveCdr")
    CdrReservationResponseDto reserveCdr(String cdr);

    /**
     * Confirms the reservation
     * 
     * @param reservationDto Prepaid reservation's data
     * @return Request processing status 
     */
    @POST
    @Path("/confirmReservation")
    ActionStatus confirmReservation(PrepaidReservationDto reservationDto);

    /**
     * Cancels the reservation
     * 
     * @param reservationDto Prepaid reservation's data
     * @return Request processing status 
     */
    @POST
    @Path("/cancelReservation")
    ActionStatus cancelReservation(PrepaidReservationDto reservationDto);
}