package org.meveo.api;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.api.dto.OfferDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.admin.User;
import org.meveo.model.billing.InstanceStatusEnum;
import org.meveo.model.billing.ServiceInstance;
import org.meveo.model.billing.Subscription;
import org.meveo.model.catalog.OfferTemplate;
import org.meveo.model.catalog.ServiceTemplate;
import org.meveo.model.crm.Provider;
import org.meveo.service.billing.impl.ServiceInstanceService;
import org.meveo.service.billing.impl.SubscriptionService;
import org.meveo.service.catalog.impl.OfferTemplateService;
import org.meveo.service.catalog.impl.ServiceTemplateService;
import org.meveo.util.MeveoParamBean;

/**
 * @author Edward P. Legaspi
 * @since Oct 11, 2013
 **/
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class OfferTemplateServiceApi extends BaseApi {

	@Inject
	@MeveoParamBean
	private ParamBean paramBean;

	@Inject
	private OfferTemplateService offerTemplateService;

	@Inject
	private ServiceTemplateService serviceTemplateService;

	@Inject
	private ServiceInstanceService serviceInstanceService;

	@Inject
	private SubscriptionService subscriptionService;

	public void create(OfferDto offerDto) throws MeveoApiException {
		if (!StringUtils.isBlank(offerDto.getOfferId())
				&& offerDto.getDescriptions() != null
				&& offerDto.getDescriptions().size() > 0
				&& offerDto.getServices() != null
				&& offerDto.getServices().size() > 0) {

			Provider provider = providerService.findById(offerDto
					.getProviderId());
			User currentUser = userService
					.findById(offerDto.getCurrentUserId());

			String offerTemplateCode = paramBean.getProperty(
					"asg.api.offer.offer.prefix", "_OF_")
					+ offerDto.getOfferId();

			if (offerTemplateService.findByCode(offerTemplateCode, provider) != null) {
				throw new MeveoApiException("Offer template code="
						+ offerTemplateCode + " already exists.");
			}

			OfferTemplate offerTemplate = new OfferTemplate();
			offerTemplate.setActive(true);
			offerTemplate.setCode(offerTemplateCode);

			if (offerDto.getDescriptions() != null
					&& offerDto.getDescriptions().size() > 0) {
				offerTemplate.setDescription(offerDto.getDescriptions().get(0)
						.getDescription());
			}

			String chargedServiceTemplateCode = paramBean.getProperty(
					"asg.api.offer.notcharged.prefix", "_NC_OF_")
					+ offerDto.getOfferId();
			if (serviceTemplateService
					.findByCode(chargedServiceTemplateCode, provider) != null) {
				throw new MeveoApiException("Service template code="
						+ chargedServiceTemplateCode + " already exists.");
			}
			ServiceTemplate serviceTemplate = new ServiceTemplate();
			serviceTemplate.setActive(true);
			serviceTemplate.setCode(chargedServiceTemplateCode);
			serviceTemplateService.create(em, serviceTemplate, currentUser,
					provider);

			List<ServiceTemplate> serviceTemplates = new ArrayList<ServiceTemplate>();
			serviceTemplates.add(serviceTemplate);

			if (offerDto.getServices() != null
					&& offerDto.getServices().size() > 0) {
				for (String serviceId : offerDto.getServices()) {
					// not charged
					String serviceCode = paramBean.getProperty(
							"asg.api.service.notcharged.prefix", "_NC_SE_")
							+ serviceId;
					serviceTemplate = serviceTemplateService.findByCode(em,
							serviceCode, provider);
					if (serviceTemplate != null) {
						serviceTemplates.add(serviceTemplate);
					}

					// charged
					serviceCode = paramBean.getProperty(
							"asg.api.service.charged.prefix", "_CH_SE_")
							+ serviceId;
					serviceTemplate = serviceTemplateService.findByCode(em,
							serviceCode, provider);
					if (serviceTemplate != null) {
						serviceTemplates.add(serviceTemplate);
					}
				}
				offerTemplate.setServiceTemplates(serviceTemplates);
			}

			offerTemplateService.create(em, offerTemplate, currentUser,
					provider);

		} else {
			StringBuilder sb = new StringBuilder(
					"The following parameters are required ");
			List<String> missingFields = new ArrayList<String>();

			if (StringUtils.isBlank(offerDto.getOfferId())) {
				missingFields.add("Service Id");
			}
			if (offerDto.getDescriptions() == null
					|| offerDto.getDescriptions().size() == 0) {
				missingFields.add("Description");
			}
			if (offerDto.getServices() == null
					|| offerDto.getServices().size() == 0) {
				missingFields.add("Service");
			}

			if (missingFields.size() > 1) {
				sb.append(org.apache.commons.lang.StringUtils.join(
						missingFields.toArray(), ", "));
			} else {
				sb.append(missingFields.get(0));
			}
			sb.append(".");

			throw new MeveoApiException(sb.toString());
		}
	}

	public void update(OfferDto offerDto) throws MeveoApiException {
		Provider provider = providerService.findById(offerDto.getProviderId());
		User currentUser = userService.findById(offerDto.getCurrentUserId());

		String offerTemplateCode = paramBean.getProperty(
				"asg.api.offer.offer.prefix", "_OF_") + offerDto.getOfferId();

		OfferTemplate offerTemplate = offerTemplateService.findByCode(em,
				offerTemplateCode, provider);
		if (offerTemplate == null) {
			throw new MeveoApiException("Offer template code="
					+ offerTemplateCode + " does not exists.");
		}

		String offerDescription = "";
		if (offerDto.getDescriptions() != null
				&& offerDto.getDescriptions().size() > 0) {
			offerDescription = offerDto.getDescriptions().get(0)
					.getDescription();
		}

		offerTemplate.setDescription(offerDescription);

		if (offerDto.getServices() != null && offerDto.getServices().size() > 0) {
			List<ServiceTemplate> oldServiceTemplates = offerTemplate
					.getServiceTemplates();
			List<ServiceTemplate> newServiceTemplates = new ArrayList<ServiceTemplate>();

			if (oldServiceTemplates == null) {
				oldServiceTemplates = new ArrayList<ServiceTemplate>();
			}

			for (String serviceId : offerDto.getServices()) {
				String unchargedServiceTemplateCode = paramBean.getProperty(
						"asg.api.service.notcharged.prefix", "_NC_SE_")
						+ serviceId;

				boolean serviceExists = false;

				ServiceTemplate unchargedServiceTemplate = serviceTemplateService
						.findByCode(em, unchargedServiceTemplateCode, provider);
				if (unchargedServiceTemplate != null) {
					if (oldServiceTemplates.contains(unchargedServiceTemplate)) {
						// check if instantiated
						List<ServiceInstance> serviceInstances = serviceInstanceService
								.findByServiceTemplate(em,
										unchargedServiceTemplate, provider);
						if (serviceInstances != null) {
							// instantiated
							newServiceTemplates.add(unchargedServiceTemplate);
						}
					} else {
						// associate
						newServiceTemplates.add(unchargedServiceTemplate);
					}

					serviceExists = true;
				}

				String chargedServiceTemplateCode = paramBean.getProperty(
						"asg.api.service.charged.prefix", "_CH_SE_")
						+ serviceId;

				ServiceTemplate chargedServiceTemplate = serviceTemplateService
						.findByCode(em, chargedServiceTemplateCode, provider);
				if (chargedServiceTemplate != null) {
					if (oldServiceTemplates.contains(chargedServiceTemplate)) {
						// check if instantiated
						List<ServiceInstance> serviceInstances = serviceInstanceService
								.findByServiceTemplate(em,
										unchargedServiceTemplate, provider);
						if (serviceInstances != null) {
							// instantiated
							newServiceTemplates.add(chargedServiceTemplate);
						}
					} else {
						// associate
						newServiceTemplates.add(chargedServiceTemplate);
					}

					serviceExists = true;
				}

				if (!serviceExists) {
					throw new MeveoApiException("Service template code="
							+ unchargedServiceTemplateCode
							+ " does not exists.");
				}

			}

			offerTemplate.setServiceTemplates(newServiceTemplates);
		}

		offerTemplateService.update(em, offerTemplate, currentUser);

		String serviceTemplateCode = paramBean.getProperty(
				"asg.api.offer.notcharged.prefix", "_NC_OF_")
				+ offerDto.getOfferId();
		ServiceTemplate notChargedServiceTemplate = serviceTemplateService
				.findByCode(em, serviceTemplateCode, provider);
		if (notChargedServiceTemplate != null) {
			notChargedServiceTemplate.setDescription(offerDescription);
			serviceTemplateService.update(em, notChargedServiceTemplate,
					currentUser);
		}

		serviceTemplateCode = paramBean.getProperty(
				"asg.api.offer.notcharged.prefix", "_CH_OF_")
				+ offerDto.getOfferId();
		ServiceTemplate chargedServiceTemplate = serviceTemplateService
				.findByCode(em, serviceTemplateCode, provider);
		if (chargedServiceTemplate != null) {
			chargedServiceTemplate.setDescription(offerDescription);
			serviceTemplateService.update(em, chargedServiceTemplate,
					currentUser);
		}
	}

	public void remove(Long providerId, Long currentUserId, String offerId)
			throws MeveoApiException {
		if (!StringUtils.isBlank(offerId)) {
			Provider provider = providerService.findById(providerId);
			User currentUser = userService.findById(currentUserId);

			String offerTemplateCode = paramBean.getProperty(
					"asg.api.offer.offer.prefix", "_OF_") + offerId;

			OfferTemplate offerTemplate = offerTemplateService.findByCode(em,
					offerTemplateCode, provider);

			if (offerTemplate != null) {
				// uncharged
				String serviceTemplateCode = paramBean.getProperty(
						"asg.api.offer.notcharged.prefix", "_NC_OF_") + offerId;
				ServiceTemplate serviceTemplate = serviceTemplateService
						.findByCode(em, serviceTemplateCode, provider);

				List<ServiceTemplate> toBeDeletedServices = new ArrayList<ServiceTemplate>();

				if (serviceTemplate != null) {
					List<ServiceInstance> serviceInstances = serviceInstanceService
							.findByServiceTemplate(em, serviceTemplate,
									provider, InstanceStatusEnum.ACTIVE);
					if (serviceInstances == null
							|| serviceInstances.size() == 0) {
						toBeDeletedServices.add(serviceTemplate);
					}
				}

				// charged
				serviceTemplateCode = paramBean.getProperty(
						"asg.api.offer.notcharged.prefix", "_CH_OF_") + offerId;
				serviceTemplate = serviceTemplateService.findByCode(em,
						serviceTemplateCode, provider);

				if (serviceTemplate != null) {
					List<ServiceInstance> serviceInstances = serviceInstanceService
							.findByServiceTemplate(em, serviceTemplate,
									provider, InstanceStatusEnum.ACTIVE);
					if (serviceInstances == null
							|| serviceInstances.size() == 0) {
						toBeDeletedServices.add(serviceTemplate);
					}
				}

				// delete other meveo serviceTemplates
				if (offerTemplate.getServiceTemplates() != null
						&& offerTemplate.getServiceTemplates().size() > 0) {
					for (ServiceTemplate otherServiceTemplate : offerTemplate
							.getServiceTemplates()) {
						String chargedPrefix = paramBean.getProperty(
								"asg.api.service.charged.prefix", "_CH_SE_");
						String unchargedPrefix = paramBean.getProperty(
								"asg.api.service.notcharged.prefix", "_NC_SE_");
						if (otherServiceTemplate.getCode().startsWith(
								chargedPrefix)
								|| otherServiceTemplate.getCode().startsWith(
										unchargedPrefix)) {
							toBeDeletedServices.add(serviceTemplate);
						}
					}
				}

				// check if offer is subscribe
				List<Subscription> offerSubscriptions = subscriptionService
						.findByOfferTemplate(em, offerTemplate, provider);
				if (offerSubscriptions != null && offerSubscriptions.size() > 0) {
					offerTemplateService.remove(em, offerTemplate);
				} else {
					// to be remove
					for (ServiceTemplate st : toBeDeletedServices) {
						offerTemplate.getServiceTemplates().remove(st);
					}
					offerTemplateService.update(em, offerTemplate, currentUser);
				}

			} else {
				throw new MeveoApiException("Offer template code="
						+ offerTemplateCode + " does not exists.");
			}

		} else {
			StringBuilder sb = new StringBuilder(
					"The following parameters are required ");
			List<String> missingFields = new ArrayList<String>();

			missingFields.add("Offer Id");

			if (missingFields.size() > 1) {
				sb.append(org.apache.commons.lang.StringUtils.join(
						missingFields.toArray(), ", "));
			} else {
				sb.append(missingFields.get(0));
			}
			sb.append(".");

			throw new MeveoApiException(sb.toString());
		}
	}
}
