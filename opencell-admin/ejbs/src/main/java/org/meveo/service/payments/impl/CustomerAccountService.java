/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.payments.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.audit.logging.annotations.MeveoAudit;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.InstanceStatusEnum;
import org.meveo.model.billing.ServiceInstance;
import org.meveo.model.crm.Customer;
import org.meveo.model.payments.AccountOperation;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.CustomerAccountStatusEnum;
import org.meveo.model.payments.DunningLevelEnum;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.OperationCategoryEnum;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.model.shared.Address;
import org.meveo.model.shared.ContactInformation;
import org.meveo.service.base.AccountService;
import org.meveo.service.catalog.impl.TitleService;
import org.meveo.service.crm.impl.CustomerService;

/**
 * Customer Account service implementation.
 */
@Stateless
public class CustomerAccountService extends AccountService<CustomerAccount> {
	
	@Inject
	private CreditCategoryService creditCategoryService;

	@Inject
	private CustomerService customerService;

	@Inject
	private OtherCreditAndChargeService otherCreditAndChargeService;

	@Inject
	private TitleService titleService;

	@Inject
	private ResourceBundle recourceMessages;

	private ParamBean paramBean = ParamBean.getInstance();

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#isCustomerAccountWithIdExists(java.lang.Long)
	 */
	public boolean isCustomerAccountWithIdExists(Long id) {
		Query query = getEntityManager().createQuery("select count(*) from CustomerAccount a where a.id = :id");
		query.setParameter("id", id);
		Long count = (Long) query.getSingleResult();
		if(count == null){
			return false;
		}
		return count.longValue() > 0;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllBillingKeywords() {
		Query query = getEntityManager().createQuery("select distinct(billingKeyword) from CustomerAccount");
		return query.getResultList();
	}

	public List<CustomerAccount> importCustomerAccounts(List<CustomerAccount> customerAccountsToImport) {
		List<CustomerAccount> failedImports = new ArrayList<CustomerAccount>();
		return failedImports;
	}

	private BigDecimal computeOccAmount(CustomerAccount customerAccount, OperationCategoryEnum operationCategoryEnum,
			boolean isDue, Date to, boolean activeDunningExclusion, MatchingStatusEnum... status) throws Exception {

		BigDecimal balance = null;
		QueryBuilder queryBuilder = new QueryBuilder("select sum(unMatchingAmount) from AccountOperation");
		queryBuilder.addCriterionEnum("transactionCategory", operationCategoryEnum);
		if (isDue) {
			queryBuilder.addCriterion("dueDate", "<=", to, false);
		} else {
			queryBuilder.addCriterion("transactionDate", "<=", to, false);
		}
		if(activeDunningExclusion){
			queryBuilder.addBooleanCriterion("excludedFromDunning", false);
		}
		queryBuilder.addCriterionEntity("customerAccount", customerAccount);
		if (status.length == 1) {
			queryBuilder.addCriterionEnum("matchingStatus", status[0]);
		} else {
			queryBuilder.startOrClause();
			for (MatchingStatusEnum st : status) {
				queryBuilder.addCriterionEnum("matchingStatus", st);
			}
			queryBuilder.endOrClause();
		}
		log.debug("query={}",queryBuilder.getSqlString());
		Query query = queryBuilder.getQuery(getEntityManager());
		balance = (BigDecimal) query.getSingleResult();
		return balance;
	}

	private BigDecimal computeBalance(CustomerAccount customerAccount, Date to, boolean isDue,boolean dunningExclusion,
			MatchingStatusEnum... status) throws BusinessException {
		log.trace("start computeBalance customerAccount:{}, toDate:{}, isDue:{}, dunningExclusion:{}",(customerAccount == null ? "null" : customerAccount.getCode())
				,to,isDue,dunningExclusion);
		if (customerAccount == null) {
			log.warn("Error when customerAccount is null!");
			throw new BusinessException("customerAccount is null");
		}
		if (to == null) {
			log.warn("Error when toDate is null!");
			throw new BusinessException("toDate is null");
		}
		BigDecimal balance = null, balanceDebit = null, balanceCredit = null;
		try {
			balanceDebit = computeOccAmount(customerAccount, OperationCategoryEnum.DEBIT, isDue, to,dunningExclusion, status);
			balanceCredit = computeOccAmount(customerAccount, OperationCategoryEnum.CREDIT, isDue, to,dunningExclusion, status);
			if (balanceDebit == null) {
				balanceDebit = BigDecimal.ZERO;
			}
			if (balanceCredit == null) {
				balanceCredit = BigDecimal.ZERO;
			}
			balance = balanceDebit.subtract(balanceCredit);
			ParamBean param = ParamBean.getInstance();
			int balanceFlag = Integer.parseInt(param.getProperty("balance.multiplier", "1"));
			balance = balance.multiply(new BigDecimal(balanceFlag));
            log.debug("end computeBalance customerAccount code:{} , balance:{}", customerAccount.getCode(), balance);
		} catch (Exception e) {
			throw new BusinessException("Internal error");
		}
		return balance;

	}

	public BigDecimal customerAccountBalanceExigible(CustomerAccount customerAccount, Date to) throws BusinessException {
		log.info("customerAccountBalanceExigible  customerAccount:"
				+ (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		return computeBalance(customerAccount, to, true,false, MatchingStatusEnum.O, MatchingStatusEnum.P,
				MatchingStatusEnum.I);

	}

	public BigDecimal customerAccountBalanceExigibleWithoutLitigation(Long customerAccountId,
			String customerAccountCode, Date to) throws BusinessException {
		log.info("customerAccountBalanceExigibleWithoutLitigation with id:{},code:{},toDate:{}", customerAccountId,
				customerAccountCode, to);
		return customerAccountBalanceExigibleWithoutLitigation(
				findCustomerAccount(customerAccountId, customerAccountCode), to);
	}

	public BigDecimal customerAccountBalanceExigibleWithoutLitigation(CustomerAccount customerAccount, Date to)
			throws BusinessException {
		log.info("customerAccountBalanceExigibleWithoutLitigation  customerAccount:"
				+ (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		return computeBalance(customerAccount, to, true,true, MatchingStatusEnum.O, MatchingStatusEnum.P);
	}

	public BigDecimal customerAccountBalanceDue(CustomerAccount customerAccount, Date to) throws BusinessException {
		log.info("customerAccountBalanceDue  customerAccount:"
				+ (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		return computeBalance(customerAccount, to, false,false, MatchingStatusEnum.O, MatchingStatusEnum.P,
				MatchingStatusEnum.I);
	}

	public BigDecimal customerAccountBalanceDueWithoutLitigation(Long customerAccountId, String customerAccountCode,
			Date to) throws BusinessException {
		log.info("customerAccountBalanceDueWithoutLitigation with id" + customerAccountId + ",code:"
				+ customerAccountCode + ",toDate:" + to);
		return customerAccountBalanceDueWithoutLitigation(findCustomerAccount(customerAccountId, customerAccountCode),
				to);
	}

	public BigDecimal customerAccountBalanceDueWithoutLitigation(CustomerAccount customerAccount, Date to)
			throws BusinessException {
		log.info("customerAccountBalanceDueWithoutLitigation  customerAccount:"
				+ (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		return computeBalance(customerAccount, to, false,false, MatchingStatusEnum.O, MatchingStatusEnum.P);
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#customerAccountBalanceExigible(java.lang.Long,
	 *      java.lang.String, java.util.Date)
	 */
	public BigDecimal customerAccountBalanceExigible(Long customerAccountId, String customerAccountCode, Date to)
			throws BusinessException {
		log.info("customerAccountBalanceExligible with id:" + customerAccountId + ",code:" + customerAccountCode
				+ ",toDate:" + to);
		return customerAccountBalanceExigible(findCustomerAccount(customerAccountId, customerAccountCode), to);
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#customerAccountBalanceDue(java.lang.Long,
	 *      java.lang.String, java.util.Date)
	 */
	public BigDecimal customerAccountBalanceDue(Long customerAccountId, String customerAccountCode, Date to)
			throws BusinessException {

		log.info("start customerAccountBalanceDue with id:" + customerAccountId + ",code:" + customerAccountCode
				+ ",toDate:" + to);

		return customerAccountBalanceDue(findCustomerAccount(customerAccountId, customerAccountCode), to);
	}

	public void createCustomerAccount(String code, String title, String firstName, String lastName, String address1,
			String address2, String zipCode, String city, String state, String email, Long customerId,
			String creditCategory, PaymentMethodEnum paymentMethod) throws BusinessException {
		log.info("start createCustomerAccount with code:" + code + ",customerId:" + customerId);
		if (code == null || code.trim().equals("") || customerId == null ) {
			log.warn("Error: requried value(s) is null with code:{},customerId:{}", code, customerId);
			throw new BusinessException("Error when required value(s) is required");
		}
		log.info("create customer account with code:" + code);
		CustomerAccount customerAccount = null;
		try {
			customerAccount = findCustomerAccount(null, code);
		} catch (Exception e) {
		}

		if (customerAccount != null) {
			log.warn("Error when one customer account existed with code:" + code);
			throw new BusinessException("Error: one customer account existed with code:" + code
					+ " when create new customer account!");
		}
		Customer customer = getCustomerById(customerId);

		customerAccount = new CustomerAccount();
		customerAccount.setCustomer(customer);
		customerAccount.setCode(code);
		customerAccount.setName(new org.meveo.model.shared.Name());
		customerAccount.getName().setTitle(titleService.findByCode(title));
		customerAccount.getName().setFirstName(firstName);
		customerAccount.getName().setLastName(lastName);
		customerAccount.setAddress(new Address());
		customerAccount.getAddress().setAddress1(address1);
		customerAccount.getAddress().setAddress2(address2);
		customerAccount.getAddress().setZipCode(zipCode);
		customerAccount.getAddress().setCity(city);
		customerAccount.getAddress().setState(state);
		customerAccount.setContactInformation(new ContactInformation());
		customerAccount.getContactInformation().setEmail(email);
		customerAccount.setStatus(CustomerAccountStatusEnum.ACTIVE);
		customerAccount.setDunningLevel(DunningLevelEnum.R0);
		customerAccount.setDateStatus(new Date());
		customerAccount.setDateDunningLevel(new Date());
		customerAccount.setPaymentMethod(paymentMethod);
		if (!StringUtils.isBlank(creditCategory)) {
			customerAccount.setCreditCategory(creditCategoryService.findByCode(creditCategory));
		}

		try {
			this.create(customerAccount);
		} catch (Exception e) {
			log.warn("Error when create one customer account with code {}, customerId {}", code, customerId);
			throw new BusinessException("Error:" + e.getMessage() + " when create a new customer account with code:"
					+ code + ",customerId:" + customerId);
		}
		log.info("successfully create one customer account with code:" + code + ",customerId:" + customerId);
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#updateCustomerAccount(java.lang.Long,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, org.meveo.model.payments.CreditCategoryEnum,
	 *      org.meveo.model.payments.PaymentMethodEnum,
	 *      org.meveo.model.admin.User)
	 */
	@MeveoAudit
	public void updateCustomerAccount(Long id, String code, String title, String firstName, String lastName,
			String address1, String address2, String zipCode, String city, String state, String email,
			String creditCategory, PaymentMethodEnum paymentMethod) throws BusinessException {
		log.info("start updateCustomerAccount with code:{},id:{}", code, id);
		if ((code == null || code.trim().equals(""))) {
			log.warn("Error when require value(s) is null!");
			throw new BusinessException("Error when required values(s) is null!");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);

		if (customerAccount.getName() == null)
			customerAccount.setName(new org.meveo.model.shared.Name());
		customerAccount.getName().setTitle(titleService.findByCode(code));
		customerAccount.getName().setFirstName(firstName);
		customerAccount.getName().setLastName(lastName);
		if (customerAccount.getAddress() == null)
			customerAccount.setAddress(new Address());
		customerAccount.getAddress().setAddress1(address1);
		customerAccount.getAddress().setAddress2(address2);
		customerAccount.getAddress().setZipCode(zipCode);
		customerAccount.getAddress().setCity(city);
		customerAccount.getAddress().setState(state);
		customerAccount.setPaymentMethod(paymentMethod);
		if (!StringUtils.isBlank(creditCategory)) {
			customerAccount.setCreditCategory(creditCategoryService.findByCode(creditCategory));
		}
		
		if (customerAccount.getContactInformation() == null) {
			customerAccount.setContactInformation(new ContactInformation());
		}
		customerAccount.getContactInformation().setEmail(email);

		try {
			this.update(customerAccount);
		} catch (Exception e) {
			log.warn("Error: " + e.getMessage() + " whne update one customer acount with code:" + code);
			throw new BusinessException("Error: " + e.getMessage() + " when update one customer account with code:"
					+ code);
		}
		log.info("successfully update customer account with code:" + code);
	}

	@MeveoAudit
	public void closeCustomerAccount(CustomerAccount customerAccount) throws BusinessException {
		log.info("closeCustomerAccount customerAccount {}", (customerAccount == null ? "null" : customerAccount.getCode()));

		if (customerAccount == null) {
			log.warn("closeCustomerAccount customerAccount is null");
			throw new BusinessException("customerAccount is null");
		}
		if (customerAccount.getStatus() == CustomerAccountStatusEnum.CLOSE) {
			log.warn("closeCustomerAccount customerAccount already closed");
			throw new BusinessException("customerAccount already closed");
		}
		try {
			log.debug("closeCustomerAccount  update customerAccount ok");
			ParamBean param = ParamBean.getInstance("meveo-admin.properties");
			String codeOCCTemplate = param.getProperty("occ.codeOccCloseAccount", "CLOSE_ACC");
			BigDecimal balanceDue = customerAccountBalanceDue(customerAccount, new Date());
			if (balanceDue == null) {
				log.warn("closeCustomerAccount balanceDue is null");
				throw new BusinessException("balanceDue is null");
			}
			log.debug("closeCustomerAccount  balanceDue:" + balanceDue);
			if (balanceDue.compareTo(BigDecimal.ZERO) < 0) {
				throw new BusinessException(recourceMessages.getString("closeCustomerAccount.balanceDueNegatif"));
			}
			if (balanceDue.compareTo(BigDecimal.ZERO) > 0) {
				otherCreditAndChargeService
						.addOCC(codeOCCTemplate, null, customerAccount, balanceDue, new Date());
				log.debug("closeCustomerAccount  add occ ok");
			}
			customerAccount.setStatus(CustomerAccountStatusEnum.CLOSE);
			customerAccount.setDateStatus(new Date());
			update(customerAccount);
			log.info("closeCustomerAccount customerAccountCode:" + customerAccount.getCode() + " closed successfully");
		} catch (BusinessException be) {
			throw be;
		}
	}

	public void closeCustomerAccount(Long customerAccountId, String customerAccountCode)
			throws BusinessException, Exception {
		log.info("closeCustomerAccount customerAccountCode {}, customerAccountID {}", customerAccountCode, customerAccountId);
		closeCustomerAccount(findCustomerAccount(customerAccountId, customerAccountCode));
	}

	@MeveoAudit
	public void transferAccount(CustomerAccount fromCustomerAccount, CustomerAccount toCustomerAccount,
			BigDecimal amount) throws BusinessException, Exception {
		log.info("transfertAccount fromCustomerAccount {} toCustomerAccount {} amount {}", (fromCustomerAccount == null ? "null" : fromCustomerAccount.getCode()), (toCustomerAccount == null ? "null" : toCustomerAccount.getCode()),  amount);

		if (fromCustomerAccount == null) {
			log.warn("transfertAccount fromCustomerAccount is null");
			throw new BusinessException("fromCustomerAccount is null");
		}
		if (toCustomerAccount == null) {
			log.warn("transfertAccount toCustomerAccount is null");
			throw new BusinessException("toCustomerAccount is null");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
			log.warn("Error in transfertAccount amount is null");
			throw new BusinessException("amount is null");
		}
		try {
			ParamBean param = ParamBean.getInstance("meveo-admin.properties");
			String occTransferAccountCredit = param.getProperty("occ.templateTransferAccountCredit", "TRANS_CRED");
			String occTransferAccountDebit = param.getProperty("occ.templateTransferAccountDebit", "TRANS_DEB");
			String descTransfertFrom = paramBean.getProperty("occ.descTransfertFrom", "transfer from");
			String descTransfertTo = paramBean.getProperty("occ.descTransfertFrom", "transfer from");

			otherCreditAndChargeService.addOCC(occTransferAccountDebit,
					descTransfertFrom + " " + toCustomerAccount.getCode(), fromCustomerAccount, amount, new Date());
			otherCreditAndChargeService.addOCC(occTransferAccountCredit,
					descTransfertTo + " " + fromCustomerAccount.getCode(), toCustomerAccount, amount, new Date());
			log.info("Successful transfertAccount fromCustomerAccountCode:" + fromCustomerAccount.getCode()
					+ " toCustomerAccountCode:" + toCustomerAccount.getCode());

		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#transferAccount(java.lang.Long,
	 *      java.lang.String, java.lang.Long, java.lang.String,
	 *      java.math.BigDecimal, org.meveo.model.admin.User)
	 */
	public void transferAccount(Long fromCustomerAccountId, String fromCustomerAccountCode, Long toCustomerAccountId,
			String toCustomerAccountCode, BigDecimal amount) throws BusinessException, Exception {
        log.info("transfertAccount fromCustomerAccountCode {} fromCustomerAccountId {} toCustomerAccountCode {} toCustomerAccountId {}, amount {}", fromCustomerAccountCode,
            fromCustomerAccountId, toCustomerAccountCode, +toCustomerAccountId, amount);
		transferAccount(findCustomerAccount(fromCustomerAccountId, fromCustomerAccountCode),
				findCustomerAccount(toCustomerAccountId, toCustomerAccountCode), amount);
	}

	public CustomerAccount consultCustomerAccount(Long id, String code) throws BusinessException {
		return findCustomerAccount(id, code);
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#updateCreditCategory(java.lang.Long,
	 *      java.lang.String, org.meveo.model.payments.CreditCategoryEnum,
	 *      org.meveo.model.admin.User)
	 */
	public void updateCreditCategory(Long id, String code, String creditCategory)
			throws BusinessException {
		log.info("start updateCreditCategory with id:" + id + ",code:" + code);
		if (creditCategory == null) {
			log.warn("Error when required creditCategory is null!");
			throw new BusinessException("Error when required creditCategory is null");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		if (!StringUtils.isBlank(creditCategory)) {
			customerAccount.setCreditCategory(creditCategoryService.findByCode(creditCategory));
		}
		
		update(customerAccount);
		log.info("successfully end updateCreditCategory!");
	}

	/**
	 * update dunningLevel for one existed customer account by id or code
	 */
	@MeveoAudit
	public void updateDunningLevel(Long id, String code, DunningLevelEnum dunningLevel)
			throws BusinessException {
		log.info("start updateDunningLevel with id:" + id + ",code:" + code);
		if (dunningLevel == null) {
			log.warn("Error when required dunningLevel is null!");
			throw new BusinessException("Error when required dunningLevel is null");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		customerAccount.setDunningLevel(dunningLevel);
		customerAccount.setDateDunningLevel(new Date());
		update(customerAccount);
		log.info("successfully end updateDunningLevel!");
	}

	/**
	 * update paymentMethod for one existed customer account by id or code
	 */
	@MeveoAudit
	public void updatePaymentMethod(Long id, String code, PaymentMethodEnum paymentMethod)
			throws BusinessException {
		log.info("start updatePaymentMethod with id:" + id + ",code:" + code);
		if (paymentMethod == null) {
			log.warn("Error when required paymentMethod is null!");
			throw new BusinessException("Error when required paymentMethod is null");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		customerAccount.setPaymentMethod(paymentMethod);
		update(customerAccount);
		log.info("successfully end updatePaymentMethod!");

	}

	/**
	 * get operations from one existed customerAccount by id or code
	 */
	public List<AccountOperation> consultOperations(Long id, String code, Date from, Date to) throws BusinessException {
		log.info("start consultOperations with id:" + id + ",code:" + code + "from:" + from + ",to:" + to);
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		List<AccountOperation> operations = customerAccount.getAccountOperations();
		log.info("found accountOperation size:" + (operations != null ? operations.size() : 0)
				+ " from customerAccount code:" + code + ",id:" + id);
		if (to == null) {
			to = new Date();
		}
		if (operations != null) {
			Iterator<AccountOperation> it = operations.iterator();
			while (it.hasNext()) {
				Date transactionDate = it.next().getTransactionDate();
				if (transactionDate == null)
					continue;
				if (from == null) {
					if (transactionDate.after(to)) {
						it.remove();
					}
				} else if (transactionDate.before(from) || transactionDate.after(to)) {
					it.remove();
				}
			}
		}
		log.info("found effective operations size:" + (operations != null ? operations.size() : 0)
				+ " from customerAccount code:" + code + ",id:" + id);
		log.info("successfully end consultOperations");
		return operations;
	}

	private Customer getCustomerById(Long id) throws BusinessException {
		log.info("start to find one customer with id:" + id);
		Customer result = customerService.findById(id);
		if (result == null) {
			log.warn("retrieve a null customer with id:" + id);
			throw new BusinessException("Error when find null customer with id:" + id);
		}
		log.info("successfully end getCustomerById with id:" + id);
		return result;
	}

	public CustomerAccount findCustomerAccount(Long id, String code) throws BusinessException {
		
		log.info("findCustomerAccount with code:" + code + ",id:" + id);

		if ((code == null || code.equals("")) && (id == null || id == 0)) {
			log.warn("Error: require code and id are null!");
			throw new BusinessException("Error: required code and ID are null!");
		}

		CustomerAccount customerAccount = null;
		try {
			customerAccount = (CustomerAccount) getEntityManager().createQuery("from CustomerAccount where id=:id or code=:code")
					.setParameter("id", id).setParameter("code", code).getSingleResult();
		} catch (Exception e) {
			log.warn("failed to fin customer account ",e);
		}

		if (customerAccount == null) {
			log.warn("Error when find nonexisted customer account ");
			throw new BusinessException("Error when find nonexisted customer account code:" + code + " , id:" + id);
		}

		return customerAccount;
	}

	public boolean isAllServiceInstancesTerminated(CustomerAccount customerAccount) {
		// FIXME : just count inside the query
		Query billingQuery = getEntityManager()
				.createQuery(
						"select si from ServiceInstance si join si.subscription s join s.userAccount ua join ua.billingAccount ba join ba.customerAccount ca where ca.id = :customerAccountId");
		billingQuery.setParameter("customerAccountId", customerAccount.getId());
		@SuppressWarnings("unchecked")
		List<ServiceInstance> services = (List<ServiceInstance>) billingQuery.getResultList();
		for (ServiceInstance service : services) {
			boolean serviceActive = service.getStatus() == InstanceStatusEnum.ACTIVE;
			if (serviceActive) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<CustomerAccount> getCustomerAccounts(String creditCategory,
			PaymentMethodEnum paymentMethod) {
		List<CustomerAccount> customerAccounts = getEntityManager()
				.createQuery(
						"from "
								+ CustomerAccount.class.getSimpleName()
								+ " where paymentMethod=:paymentMethod and creditCategory.code=:creditCategoryCode and status=:status ")
				.setParameter("paymentMethod", paymentMethod).setParameter("creditCategoryCode", creditCategory)
				.setParameter("status", CustomerAccountStatusEnum.ACTIVE)
				.getResultList();
		return customerAccounts;
	}

	@SuppressWarnings("unchecked")
	public List<CustomerAccount> listByCustomer(Customer customer) {
		QueryBuilder qb = new QueryBuilder(CustomerAccount.class, "c");
		qb.addCriterionEntity("customer", customer);

		try {
			return (List<CustomerAccount>) qb.getQuery(getEntityManager()).getResultList();
		} catch (NoResultException e) {
			log.warn("failed to get customerAccount list by customer",e);
			return null;
		}
	}

}