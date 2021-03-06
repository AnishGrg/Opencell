/**
 * 
 */
package org.meveo.admin.async;

import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.BillingRun;
import org.meveo.service.billing.impl.InvoiceService;
import org.slf4j.Logger;

/**
 * @author anasseh
 *
 */

@Stateless
public class RatedTxInvoicingAsync {
	
	@Inject
	private InvoiceService invoiceService;
	
	@Inject
	protected Logger log;

	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public Future<String> createAgregatesAndInvoiceAsync(List<BillingAccount> billingAccounts,BillingRun billingRun) {
		
		for (BillingAccount billingAccount : billingAccounts) {
			try {
				invoiceService.createAgregatesAndInvoice(billingAccount, billingRun,null,null,null,null);
			} catch (Exception e) {
				log.error("Error for BA=" + billingAccount.getCode() + " : " + e);
			}
		}
		return new AsyncResult<String>("OK");
	}
}
