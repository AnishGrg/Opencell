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
package org.meveo.service.wf;

import java.util.List;

import javax.ejb.Stateless;

import org.meveo.model.crm.Provider;
import org.meveo.model.wf.Workflow;
import org.meveo.model.wf.WorkflowStatusEnum;
import org.meveo.service.base.BusinessService;

@Stateless
public class WorkflowService extends BusinessService<Workflow> {

	@SuppressWarnings("unchecked")
	public List<Workflow> getWorkflows(Provider provider) {
		return (List<Workflow>) getEntityManager()
				.createQuery(
						"from " + Workflow.class.getSimpleName()
								+ " where status=:status and provider=:provider")
				.setParameter("status", WorkflowStatusEnum.ACTIVE)
				.setParameter("provider", provider)
				.getResultList();
	}

    @SuppressWarnings("unchecked")
    public Workflow getWorkflowOrder(Provider provider) {
        Workflow workflow = null;
        try {
            return (Workflow) getEntityManager()
                .createQuery(
                        "from " + Workflow.class.getSimpleName()
                                + " wf left join fetch wf.transitions where wf.wfType=:wfType and wf.status=:status and wf.provider=:provider")
                .setParameter("wfType", "org.meveo.admin.wf.types.OrderWF")
                .setParameter("status", WorkflowStatusEnum.ACTIVE)
                .setParameter("provider", provider)
                .getSingleResult();
        } catch (Exception e) {
        }
        return workflow;
    }
}
