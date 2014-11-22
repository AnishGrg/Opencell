package org.meveo.api.dto;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.billing.InvoiceSubCategory;

/**
 * @author Edward P. Legaspi
 **/
@XmlRootElement(name = "InvoiceSubCategory")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceSubCategoryDto implements Serializable {

	private static final long serialVersionUID = 1832246068609179546L;

	@XmlAttribute(required = true)
	private String code;

	@XmlAttribute(required = true)
	private String description;

	@XmlAttribute(required = true)
	private String invoiceCategory;

	private List<LanguageDescriptionDto> languageDescriptions;

	public InvoiceSubCategoryDto() {

	}

	public InvoiceSubCategoryDto(InvoiceSubCategory invoiceSubCategory) {
		code = invoiceSubCategory.getCode();
		description = invoiceSubCategory.getDescription();
		invoiceCategory = invoiceSubCategory.getInvoiceCategory().getCode();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInvoiceCategory() {
		return invoiceCategory;
	}

	public void setInvoiceCategory(String invoiceCategory) {
		this.invoiceCategory = invoiceCategory;
	}

	public List<LanguageDescriptionDto> getLanguageDescriptions() {
		return languageDescriptions;
	}

	public void setLanguageDescriptions(
			List<LanguageDescriptionDto> languageDescriptions) {
		this.languageDescriptions = languageDescriptions;
	}

	@Override
	public String toString() {
		return "InvoiceSubCategoryDto [code=" + code + ", description="
				+ description + ", invoiceCategory=" + invoiceCategory
				+ ", languageDescriptions=" + languageDescriptions + "]";
	}

}
