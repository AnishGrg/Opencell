/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.model.payments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.meveo.model.AuditableEntity;

/**
 * @author Tyshan(tyshan@manaty.net)
 * @created Nov 13, 2010 11:52:56 AM
 */
@Entity
@Table(name = "AR_DDREQUEST_LOT")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "AR_DDREQUEST_LOT_SEQ")
public class DDRequestLOT extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "SEND_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendDate;

    @Column(name = "INVOICE_NUMBER")
    private Integer invoicesNumber;

    @Column(name = "IS_PAYMENT_CREATED")
    private boolean paymentCreated;

    @Column(name = "INVOICE_AMOUNT", precision = 23, scale = 12)
    private BigDecimal invoicesAmount;

    @OneToMany(mappedBy = "ddRequestLOT", fetch = FetchType.LAZY)
    private List<RecordedInvoice> invoices = new ArrayList<RecordedInvoice>();

    @OneToMany(mappedBy = "ddRequestLOT", fetch = FetchType.LAZY,cascade=CascadeType.ALL)
    private List<DDRequestItem> ddrequestItems = new ArrayList<DDRequestItem>();
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public void setInvoices(List<RecordedInvoice> invoices) {
        this.invoices = invoices;
    }

    public List<RecordedInvoice> getInvoices() {
        return invoices;
    }

    public Integer getInvoicesNumber() {
        return invoicesNumber;
    }

    public void setInvoicesNumber(Integer invoicesNumber) {
        this.invoicesNumber = invoicesNumber;
    }

    public BigDecimal getInvoicesAmount() {
        return invoicesAmount;
    }

    public void setInvoicesAmount(BigDecimal invoicesAmount) {
        this.invoicesAmount = invoicesAmount;
    }

    /**
     * @param paymentCreated
     *            the paymentCreated to set
     */
    public void setPaymentCreated(boolean paymentCreated) {
        this.paymentCreated = paymentCreated;
    }

    /**
     * @return the paymentCreated
     */
    public boolean isPaymentCreated() {
        return paymentCreated;
    }

	public void setDdrequestItems(List<DDRequestItem> ddrequestItems) {
		this.ddrequestItems = ddrequestItems;
	}

	public List<DDRequestItem> getDdrequestItems() {
		return ddrequestItems;
	}

}
