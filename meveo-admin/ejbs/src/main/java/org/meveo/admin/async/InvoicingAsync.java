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
import org.meveo.service.billing.impl.BillingAccountService;
import org.slf4j.Logger;

/**
 * @author anasseh
 *
 */

@Stateless
public class InvoicingAsync {
	
	@Inject
	private BillingAccountService billingAccountService;
	
	@Inject
	protected Logger log;

	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public Future<Integer> updateBillingAccountTotalAmountsAsync(List<BillingAccount> billingAccounts,BillingRun billingRun) {
		int count=0;
		for (BillingAccount billingAccount : billingAccounts) {
			if (billingAccountService.updateBillingAccountTotalAmounts(billingAccount,billingRun)) {
				count++;
			}
		}
		log.info("WorkSet billableBA:"+count);
		return new AsyncResult<Integer>(new Integer(count));
	}
}
