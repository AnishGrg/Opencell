/**
 * 
 */
package org.meveo.admin.async;

import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.job.importexport.ImportSubscriptionsJobBean;
import org.meveo.model.jobs.JobExecutionResultImpl;

/**
 * @author anasseh
 * 
 */

@Stateless
public class ImportSubscriptionsAsync {

    @Inject
    private ImportSubscriptionsJobBean importSubscriptionsJobBean;

    @Asynchronous
    public Future<String> launchAndForget(JobExecutionResultImpl result) {
        importSubscriptionsJobBean.execute(result);
        return new AsyncResult<String>("OK");
    }
}