package org.meveo.api.dto.response.communication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.communication.EmailTemplatesDto;
import org.meveo.api.dto.response.BaseResponse;

/**
 * 
 * @author Tyshan　Shi(tyshan@manaty.net)
 * @date Jun 3, 2016 5:04:24 AM
 *
 */
@XmlRootElement(name="EmailTemplatesResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTemplatesResponseDto extends BaseResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7682857831421991842L;
	private EmailTemplatesDto emailTemplates;

	public EmailTemplatesDto getEmailTemplates() {
		return emailTemplates;
	}

	public void setEmailTemplates(EmailTemplatesDto emailTemplates) {
		this.emailTemplates = emailTemplates;
	}
	

}

