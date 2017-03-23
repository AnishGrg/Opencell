/**
 * 
 */
package org.meveo.admin.async;

import java.util.Date;
import java.util.List;

import org.meveo.model.jobs.JobExecutionResultImpl;

/**
 * @author anasseh
 *
 */
public interface AsyncRunningJobs {
	
	public void setWaiting(long waitingMillis);
	public void setNbRuns(int runsNb);
	public void launchAndForget(List<?> list,JobExecutionResultImpl result,Date maxDate);
	public void launchAndForget(JobExecutionResultImpl result);

}
