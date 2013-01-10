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
package org.meveo.model.admin;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Information about validated invoices Grieg received and processed.
 * 
 * @author Ignas Lelys
 * @created Apr 11, 2011
 * 
 */
@Entity
@DiscriminatorValue("GRIEG_VALIDATED")
public class GriegValidatedInvoiceInputHistory extends InputHistory {

    private static final long serialVersionUID = 1L;

    public GriegValidatedInvoiceInputHistory() {
    }

    /**
     * Convert from {@link InputHistory} to {@link GriegValidatedInvoiceInputHistory}.
     * 
     * @param inputInfo
     *            {@link InputHistory} superclass.
     */
    public GriegValidatedInvoiceInputHistory(InputHistory inputInfo) {
        // TODO this conversion is possible source of the bug (in case of
        // InputInfo changes)
        // so it should be either tested or generalized somehow.
        this.setId(inputInfo.getId());
        this.setVersion(inputInfo.getVersion());
        this.setAnalysisEndDate(inputInfo.getAnalysisEndDate());
        this.setAnalysisStartDate(inputInfo.getAnalysisStartDate());
        this.setName(inputInfo.getName());
        this.setParsedTickets(inputInfo.getParsedTickets());
        this.setSucceededTickets(inputInfo.getSucceededTickets());
        this.setRejectedTickets(inputInfo.getRejectedTickets());
        this.setIgnoredTickets(inputInfo.getIgnoredTickets());
        this.setProvider(inputInfo.getProvider());
    }

}
