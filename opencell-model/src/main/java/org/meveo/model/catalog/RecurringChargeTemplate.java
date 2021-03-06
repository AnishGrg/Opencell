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
package org.meveo.model.catalog;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.meveo.model.MultilanguageEntity;

@Entity
@MultilanguageEntity(key = "menu.charges", group = "ChargeTemplate")
@Table(name = "CAT_RECURRING_CHARGE_TEMPL")
@NamedQueries({			
@NamedQuery(name = "recurringChargeTemplate.getNbrRecurringChrgWithNotPricePlan", 
	           query = "select count (*) from RecurringChargeTemplate r where r.code not in (select p.eventCode from  PricePlanMatrix p where p.eventCode is not null) "),
	           
@NamedQuery(name = "recurringChargeTemplate.getRecurringChrgWithNotPricePlan", 
	           query = "from RecurringChargeTemplate r where r.code not in (select p.eventCode from  PricePlanMatrix p where p.eventCode is not null) "),
	           
@NamedQuery(name = "recurringChargeTemplate.getNbrRecurringChrgNotAssociated", 
	           query = "select count(*) from RecurringChargeTemplate r where (r.id not in (select serv.chargeTemplate from ServiceChargeTemplateRecurring serv) "
	           		+ " OR r.code not in (select p.eventCode from  PricePlanMatrix p where p.eventCode is not null))   "),
	           		
@NamedQuery(name = "recurringChargeTemplate.getRecurringChrgNotAssociated", 
	 	           query = "from RecurringChargeTemplate r where (r.id not in (select serv.chargeTemplate from ServiceChargeTemplateRecurring serv) "
	 	           		+ " OR r.code not in (select p.eventCode from  PricePlanMatrix p where p.eventCode is not null))  ")	                
	       })
public class RecurringChargeTemplate extends ChargeTemplate {

	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	@Column(name = "RECURRENCE_TYPE")
	private RecurrenceTypeEnum recurrenceType = RecurrenceTypeEnum.CALENDAR;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CALENDAR_ID")
	private Calendar calendar;

	@Column(name = "DURATION_TERM_IN_MONTH")
	private Integer durationTermInMonth;

	@Type(type="numeric_boolean")
    @Column(name = "SUBSCRIPTION_PRORATA")
	private Boolean subscriptionProrata;

	@Type(type="numeric_boolean")
    @Column(name = "TERMINATION_PRORATA")
	private Boolean terminationProrata;

	@Type(type="numeric_boolean")
    @Column(name = "APPLY_IN_ADVANCE")
	private Boolean applyInAdvance;

	@Enumerated(EnumType.STRING)
	@Column(name= "SHARE_LEVEL",length=20)
	private LevelEnum shareLevel;
	
	@Column(name = "FILTER_EXPRESSION", length = 2000)
	@Size(max = 2000)
	private String filterExpression = null;
	
	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public RecurrenceTypeEnum getRecurrenceType() {
		return recurrenceType;
	}

	public void setRecurrenceType(RecurrenceTypeEnum recurrenceType) {
		this.recurrenceType = recurrenceType;
	}

	public Integer getDurationTermInMonth() {
		return durationTermInMonth;
	}

	public void setDurationTermInMonth(Integer durationTermInMonth) {
		this.durationTermInMonth = durationTermInMonth;
	}

	public Boolean getSubscriptionProrata() {
		return subscriptionProrata;
	}

	public void setSubscriptionProrata(Boolean subscriptionProrata) {
		this.subscriptionProrata = subscriptionProrata;
	}

	public Boolean getTerminationProrata() {
		return terminationProrata;
	}

	public void setTerminationProrata(Boolean terminationProrata) {
		this.terminationProrata = terminationProrata;
	}

	public Boolean getApplyInAdvance() {
		return applyInAdvance;
	}

	public void setApplyInAdvance(Boolean applyInAdvance) {
		this.applyInAdvance = applyInAdvance;
	}

	public LevelEnum getShareLevel() {
		return shareLevel;
	}

	public void setShareLevel(LevelEnum shareLevel) {
		this.shareLevel = shareLevel;
	}

	public String getFilterExpression() {
		return filterExpression;
	}

	public void setFilterExpression(String filterExpression) {
		this.filterExpression = filterExpression;
	}

}
