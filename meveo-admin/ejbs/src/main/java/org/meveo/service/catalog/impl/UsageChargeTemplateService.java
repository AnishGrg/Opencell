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
package org.meveo.service.catalog.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.RatingCacheContainerProvider;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.QueryBuilder.QueryLikeStyleEnum;
import org.meveo.model.admin.User;
import org.meveo.model.catalog.TriggeredEDRTemplate;
import org.meveo.model.catalog.UsageChargeTemplate;
import org.meveo.model.crm.Provider;
import org.meveo.service.base.MultilanguageEntityService;

/**
 * Charge Template service implementation.
 * 
 */
@Stateless
public class UsageChargeTemplateService extends MultilanguageEntityService<UsageChargeTemplate> {

	@Inject
	private RatingCacheContainerProvider ratingCacheContainerProvider;

	@Override
	public void create(UsageChargeTemplate e, User creator) throws BusinessException {
		super.create(e, creator);
		ratingCacheContainerProvider.updateUsageChargeTemplateInCache(e);
	}

	@Override
	public UsageChargeTemplate update(UsageChargeTemplate e, User updater) throws BusinessException {
		e = super.update(e, updater);
		ratingCacheContainerProvider.updateUsageChargeTemplateInCache(e);
		return e;
	}

	@SuppressWarnings("unchecked")
	public List<UsageChargeTemplate> findByPrefix(EntityManager em, String usageChargePrefix, Provider provider) {
		QueryBuilder qb = new QueryBuilder(UsageChargeTemplate.class, "a");
		qb.like("code", usageChargePrefix, QueryLikeStyleEnum.MATCH_BEGINNING, true);

		return (List<UsageChargeTemplate>) qb.getQuery(em).getResultList();
	}

	public List<UsageChargeTemplate> findAssociatedToEDRTemplate(TriggeredEDRTemplate triggeredEDRTemplate) {
		return getEntityManager().createNamedQuery("UsageChargeTemplate.getWithTemplateEDR", UsageChargeTemplate.class)
				.setParameter("edrTemplate", triggeredEDRTemplate).getResultList();
	}

	public int getNbrUsagesChrgWithNotPricePlan(Provider provider) {
		return ((Long) getEntityManager()
				.createNamedQuery("usageChargeTemplate.getNbrUsagesChrgWithNotPricePlan", Long.class)
				.setParameter("provider", provider).getSingleResult()).intValue();
	}

	public List<UsageChargeTemplate> getUsagesChrgWithNotPricePlan(Provider provider) {
		return (List<UsageChargeTemplate>) getEntityManager()
				.createNamedQuery("usageChargeTemplate.getUsagesChrgWithNotPricePlan", UsageChargeTemplate.class)
				.setParameter("provider", provider).getResultList();
	}

	public int getNbrUsagesChrgNotAssociated(Provider provider) {
		return ((Long) getEntityManager()
				.createNamedQuery("usageChargeTemplate.getNbrUsagesChrgNotAssociated", Long.class)
				.setParameter("provider", provider).getSingleResult()).intValue();
	}

	public List<UsageChargeTemplate> getUsagesChrgNotAssociated(Provider provider) {
		return (List<UsageChargeTemplate>) getEntityManager()
				.createNamedQuery("usageChargeTemplate.getUsagesChrgNotAssociated", UsageChargeTemplate.class)
				.setParameter("provider", provider).getResultList();
	}

}