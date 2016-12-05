package org.meveo.api.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.invoice.CreateInvoiceResponseDto;
import org.meveo.api.dto.invoice.GenerateInvoiceRequestDto;
import org.meveo.api.dto.invoice.GenerateInvoiceResponseDto;
import org.meveo.api.dto.invoice.GetInvoiceResponseDto;
import org.meveo.api.dto.invoice.GetPdfInvoiceResponseDto;
import org.meveo.api.dto.invoice.GetXmlInvoiceResponseDto;
import org.meveo.api.dto.invoice.InvoiceDto;
import org.meveo.api.dto.response.CustomerInvoicesResponse;

/**
 * @author Edward P. Legaspi
 **/
@WebService
public interface InvoiceWs extends IBaseWs {

	 /**
     * Create invoice. Invoice number depends on invoice type
     * 
     * @param invoiceDto invoice dto
     * @return
     */
	@WebMethod
	public CreateInvoiceResponseDto createInvoice(@WebParam(name = "invoice") InvoiceDto invoiceDto);

	 /**
     * Search for a list of invoices given a customer account code.
     * 
     * @param customerAccountCode Customer account code
     * @return
     */
	@WebMethod
	public CustomerInvoicesResponse findInvoice(@WebParam(name = "customerAccountCode") String customerAccountCode);
	
	/**
     * Launch all the invoicing process for a given billingAccount, that's mean
	 * : <lu> <li>Create rated transactions <li>Create an exceptional billingRun
	 * with given dates <li>Validate the pre-invoicing report <li>Validate the
	 * post-invoicing report <li>Validate the BillingRun </lu>
	 *  
     * @param generateInvoiceRequestDto Contains the code of the billing account, invoicing and last transaction date
     * @return
     */
	@WebMethod
	public GenerateInvoiceResponseDto generateInvoiceData(
			@WebParam(name = "generateInvoiceRequest") GenerateInvoiceRequestDto generateInvoiceRequestDto);

	/**
     * Finds an invoice based on its invoice number and return it as xml string
     * 
     * @param invoiceNumber Invoice number
     * @return
     */
	@WebMethod
	public GetXmlInvoiceResponseDto findXMLInvoice(@WebParam(name = "invoiceNumber") String invoiceNumber);
	
	/**
     * Finds an invoice based on invoice number and invoice type. It returns the result as xml string
     * 
     * @param invoiceNumber Invoice number
     * @param invoiceType Invoice type
     * @return
     */
	@WebMethod
	public GetXmlInvoiceResponseDto findXMLInvoiceWithType(@WebParam(name = "invoiceNumber") String invoiceNumber,
			@WebParam(name = "invoiceType") String invoiceType);

	/**
     * Finds an invoice based on invoice number and return it as pdf as byte []. 
     * Invoice is not recreated, instead invoice stored as pdf in database is returned.
     * 
     * @param invoiceNumber Invoice number
     * @return
     */
	@WebMethod
	public GetPdfInvoiceResponseDto findPdfInvoice(@WebParam(name = "invoiceNumber") String invoiceNumber);
	
	/**
     * Finds an invoice based on invoice number and invoice type and return it as pdf as byte []. 
     * Invoice is not recreated, instead invoice stored as pdf in database is returned.
     * 
     * @param invoiceNumber Invoice number
     * @param invoiceType Invoice type
     * @return
     */
	@WebMethod
	public GetPdfInvoiceResponseDto findPdfInvoiceWithType(@WebParam(name = "invoiceNumber") String invoiceNumber,
			@WebParam(name = "invoiceType") String invoiceType);
	
	/**
     * Cancel an invoice based on invoice id
     * @param invoiceId Invoice id
     * @return
     */
	@WebMethod
	public ActionStatus cancelInvoice(@WebParam(name = "invoiceId") Long invoiceId);
	
	/**
     * Validate an invoice based on the invoice id
     * @param invoiceId Invoice id
     * @return
     */
	@WebMethod
	public ActionStatus validateInvoice(@WebParam(name = "invoiceId") Long invoiceId);	
	
	/**
     * Search for an invoice given an invoice id or invoice number and invoice type.
     * @param id  invoice id
     * @param invoiceNumber invoice number
     * @param invoiceType invoice type
     * @return GetInvoiceResponseDto
     */
	@WebMethod
	public GetInvoiceResponseDto findInvoiceByIdOrType(@WebParam(name = "id") Long id, @WebParam(name = "invoiceNumber") String invoiceNumber,
			@WebParam(name = "invoiceType") String invoiceType);
	
	
	@WebMethod
	public CustomerInvoicesResponse listPresentInAR(@WebParam(name = "customerAccountCode") String customerAccountCode);

	@WebMethod
	public GenerateInvoiceResponseDto generateDraftInvoice(@WebParam(name = "generateInvoiceRequest") GenerateInvoiceRequestDto generateInvoiceRequestDto);
}
