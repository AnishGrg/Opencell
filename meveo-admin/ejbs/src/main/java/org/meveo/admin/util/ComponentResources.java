package org.meveo.admin.util;

import java.io.Serializable;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.meveo.commons.utils.ParamBean;
import org.meveo.util.MeveoParamBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentResources implements Serializable {

	private static final long serialVersionUID = 1L;

	private Locale locale = Locale.ENGLISH;

	@Produces
	public ResourceBundle getResourceBundle() {
		String bundleName="messages";
		if (FacesContext.getCurrentInstance() != null){
			try {
				locale = FacesContext.getCurrentInstance().getViewRoot()
						.getLocale();
			} catch (Exception e) {
			}
			try {
				bundleName=FacesContext.getCurrentInstance().getApplication().getMessageBundle();
			} catch (Exception e) {
			}
		}
		return new ResourceBundle(java.util.ResourceBundle.getBundle(bundleName, locale));
	}

	@Produces
	@ApplicationScoped
	@Named
	@MeveoParamBean
	public ParamBean getParamBean() {
		return ParamBean.getInstance();
	}

	@Produces
	public Logger createLogger(InjectionPoint injectionPoint) {
		return LoggerFactory.getLogger(injectionPoint.getMember()
				.getDeclaringClass().getName());
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}