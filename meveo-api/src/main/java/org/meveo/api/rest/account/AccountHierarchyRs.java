package org.meveo.api.rest.account;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.account.AccountHierarchyDto;
import org.meveo.api.dto.account.CRMAccountHierarchyDto;
import org.meveo.api.dto.account.CustomerHierarchyDto;
import org.meveo.api.dto.account.FindAccountHierachyRequestDto;
import org.meveo.api.dto.response.CustomerListResponse;
import org.meveo.api.dto.response.account.GetAccountHierarchyResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.rest.security.RSSecured;

/**
 * Web service for managing account hierarchy. Account hierarchy is {@link org.meveo.model.crm.Customer}->{!link org.meveo.model.payments.CustomerAccount}->
 * {@link org.meveo.model.billing.BillingAccount}-> {@link org.meveo.model.billing.UserAccount}.
 */
@Path("/account/accountHierarchy")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@RSSecured
public interface AccountHierarchyRs extends IBaseRs {

    /**
     * Search for a list of customer accounts given a set of filter.
     * 
     * @param customerDto
     * @return
     */
    @POST
    @Path("/find")
    CustomerListResponse find(AccountHierarchyDto customerDto);

    /**
     * Create account hierarchy.
     * 
     * @param accountHierarchyDto
     * @return
     */
    @POST
    @Path("/")
    ActionStatus create(AccountHierarchyDto accountHierarchyDto);

    /**
     * Update account hierarchy.
     * 
     * @param accountHierarchyDto
     * @return
     */
    @PUT
    @Path("/")
    ActionStatus update(AccountHierarchyDto accountHierarchyDto);

    /**
     * This service allows to create / update (if exist already) and close / terminate (if termination date is set) a list of customer, customer accounts, billing accounts, user
     * accounts, subscriptions, services, and access in one transaction. It can activate and terminate subscription and service instance. Close customer account. Terminate billing
     * and user account.
     * 
     * @param postData
     * @return
     */
    @POST
    @Path("/customerHierarchyUpdate")
    ActionStatus customerHierarchyUpdate(CustomerHierarchyDto postData);

    /**
     * Is an update of findAccountHierarchy wherein the user can search on 1 or multiple levels of the hierarchy in 1 search. These are the modes that can be combined by using
     * bitwise - or |. Example: If we search on level=BA for lastName=legaspi and found a match, the search will return the hierarchy from BA to CUST. If we search on level=UA for
     * address1=my_address and found a match, the search will return the hierarchy from UA to CUST.", notes = "CUST = 1, CA = 2, BA = 4, UA = 8.
     */
    @POST
    @Path("/findAccountHierarchy")
    GetAccountHierarchyResponseDto findAccountHierarchy2(FindAccountHierachyRequestDto postData);

    @POST
    @Path("/createCRMAccountHierarchy")
    ActionStatus createCRMAccountHierarchy(CRMAccountHierarchyDto postData);

    @POST
    @Path("/updateCRMAccountHierarchy")
    ActionStatus updateCRMAccountHierarchy(CRMAccountHierarchyDto postData);

    @POST
    @Path("/createOrUpdateCRMAccountHierarchy")
    ActionStatus createOrUpdateCRMAccountHierarchy(CRMAccountHierarchyDto postData);

    /**
     * Create or update Account Hierarchy based on code.
     * 
     * @param accountHierarchyDto
     * @return
     */
    @POST
    @Path("/createOrUpdate")
    ActionStatus createOrUpdate(AccountHierarchyDto accountHierarchyDto);

}
