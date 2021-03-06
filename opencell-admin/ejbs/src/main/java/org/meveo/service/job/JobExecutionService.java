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
package org.meveo.service.job;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RunAs;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.jobs.JobExecutionResult;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.meveo.service.base.PersistenceService;

@Stateless
@RunAs("jobRunner")
public class JobExecutionService extends PersistenceService<JobExecutionResultImpl> {

    @Inject
    private JobInstanceService jobInstanceService;

    @Inject
    private CurrentUserProvider currentUserProvider;
    
    /**
     * Initiate job in a JAAS secured fashion - see @RunAs annotation. To be run from a job schedule expiration.
     * 
     * @param jobInstance Job instance to run
     * @param job Job implementation class
     * @throws BusinessException
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void executeInJaas(JobInstance jobInstance, Job job) throws BusinessException {
        // Force authentication to a current job's user
        currentUserProvider.forceAuthentication(jobInstance.getAuditable().getCreator());
        
//        log.error("AKK running {} as user {}", job.getClass(), currentUser);
        job.execute(jobInstance, null);
    }

    /**
     * Persist job execution results
     * 
     * @param job Job implementation
     * @param result Execution result
     * @param jobInstance Job instance
     * @return True if job is completely done. False if any data are left to process.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Boolean persistResult(Job job, JobExecutionResult result, JobInstance jobInstance) {
        try {
            JobExecutionResultImpl resultToPersist = JobExecutionResultImpl.createFromInterface(jobInstance, result);
            boolean isPersistResult = false;

            if ((resultToPersist.getNbItemsCorrectlyProcessed() + resultToPersist.getNbItemsProcessedWithError() + resultToPersist.getNbItemsProcessedWithWarning()) > 0) {
                log.info(job.getClass().getName() + resultToPersist.toString());
                isPersistResult = true;
            } else {
                log.info(job.getClass().getName() + ": nothing to do");
                isPersistResult = "true".equals(ParamBean.getInstance().getProperty("meveo.job.persistResult", "true"));
            }
            if (isPersistResult) {
                if (resultToPersist.isTransient()) {
                    create(resultToPersist);
                    result.setId(resultToPersist.getId());
                } else {
                    // search for job execution result
                    JobExecutionResultImpl updateEntity = findById(result.getId());
                    if (updateEntity != null) {
                        JobExecutionResultImpl.updateFromInterface(result, updateEntity);
                        update(updateEntity);
                    }
                }
            }
            return resultToPersist.isDone();

        } catch (Exception e) {// FIXME:BusinessException e) {
            log.error("Failed to persist job execution results", e);
        }

        return null;
    }

    /**
     * Execute next job or continue executing same job if more data is left to process (execution in batches). Executed asynchronously. Current user should be already available
     * from earlier context.
     * 
     * @param job Job implementation
     * @param jobInstance Job instance
     * @param continueSameJob Continue executing same job
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void executeNextJob(Job job, JobInstance jobInstance, boolean continueSameJob) {
        try {

            if (continueSameJob) {
                log.debug("Continue executing job {}", jobInstance);
                executeJobWithParameters(jobInstance, null);

            } else if (jobInstance.getFollowingJob() != null) {
                JobInstance nextJob = jobInstanceService.refreshOrRetrieve(jobInstance.getFollowingJob());
                log.debug("Executing next job {}", jobInstance.getFollowingJob().getCode());
                executeJobWithParameters(nextJob, null);
            }

        } catch (Exception e) {// FIXME:BusinessException e) {
            log.error("Failed to execute next job or continue same job", e);
        }
        log.info("JobExecutionService executeNextJob End");
    }

    /**
     * Execute a given job instance
     * 
     * @param jobInstance Job instance to execute
     * @param params Parameters to pass to job execution
     * @throws BusinessException
     * @throws NamingException Thrown when Job definition was not found
     */
    private void executeJobWithParameters(JobInstance jobInstance, Map<Object, Object> params) throws BusinessException {

        Job job = jobInstanceService.getJobByName(jobInstance.getJobTemplate());
        job.execute(jobInstance, null);
    }

    /**
     * Execute job from GUI. Execution is done asynchronously. Current user is already set by GUI.
     * 
     * @param jobInstance Job instance to execute
     * @throws BusinessException Any exception
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void manualExecute(JobInstance jobInstance) throws BusinessException {
        log.info("Manual execute a job {} of type {}", jobInstance.getCode(), jobInstance.getJobTemplate());
        try {
            executeJobWithParameters(jobInstance, null);

        } catch (Exception e) {
            log.error("Failed to manually execute a job {} of type {}", jobInstance.getCode(), jobInstance.getJobTemplate(), e);
            throw e;
        }
    }

    /**
     * Execute job and return job execution result ID to be able to query execution results later. Job execution result is persisted right away, while job is executed
     * asynchronously.
     * 
     * @param jobInstance Job instance to execute.
     * @param params Parameters (currently not used)
     * @return Job execution result ID
     * @throws BusinessException Any exception
     */
    public Long executeJobWithResultId(JobInstance jobInstance, Map<String, String> params) throws BusinessException {
        log.info("Execute a job {}  of type {} with parameters {} ", jobInstance, jobInstance.getJobTemplate(), params);

        try {
            JobExecutionResultImpl jobExecutionResult = new JobExecutionResultImpl();
            jobExecutionResult.setJobInstance(jobInstance);
            create(jobExecutionResult);

            Job job = jobInstanceService.getJobByName(jobInstance.getJobTemplate());
            job.executeInNewTrans(jobInstance, jobExecutionResult);

            log.debug("Job execution result ID for job {} of type {} is {}", jobInstance, jobInstance.getJobTemplate(), jobExecutionResult.getId());
            return jobExecutionResult.getId();

        } catch (Exception e) {
            log.error("Failed to execute a job {} of type {}", jobInstance.getCode(), jobInstance.getJobTemplate(), e);
            throw new BusinessException(e);
        }
    }

    /**
     * Execute job and return job execution result ID to be able to query execution results later. Job is executed asynchronously.
     * 
     * @param jobInstance Job instance to execute.
     * @param params Parameters (currently not used)
     * @throws BusinessException Any exception
     */
    public void executeJob(JobInstance jobInstance, Map<Object, Object> params) throws BusinessException {
        log.info("Execute a job {}  of type {} with parameters {} ", jobInstance, jobInstance.getJobTemplate(), params);

        executeJobWithParameters(jobInstance, params);
    }

    private QueryBuilder getFindQuery(String jobName, PaginationConfiguration configuration) {
        String sql = "select distinct t from JobExecutionResultImpl t";
        QueryBuilder qb = new QueryBuilder(sql);// FIXME:.cacheable();

        if (!StringUtils.isEmpty(jobName)) {
            qb.addCriterion("t.jobInstance.code", "=", jobName, false);
        }
        qb.addPaginationConfiguration(configuration);

        return qb;
    }

    /**
     * Count job execution history records which end date is older then a given date
     * 
     * @param date Date to check
     * @return A number of job execution history records which is older then a given date
     */
    public long countJobExecutionHistoryToDelete(String jobName, Date date) {
        long result = 0;
        if (date != null) {
            String sql = "select t from JobExecutionResultImpl t";
            QueryBuilder qb = new QueryBuilder(sql);// FIXME:.cacheable();
            if (!StringUtils.isEmpty(jobName)) {
                qb.addCriterion("t.jobInstance.code", "=", jobName, false);
            }
            qb.addCriterion("t.startDate", "<", date, false);
            result = qb.count(getEntityManager());
        }

        return result;
    }

    /**
     * Remove job execution history older then a given date
     * 
     * @param jobName Job name to match
     * @param date Date to check
     * @return A number of records that were removed
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int deleteJobExecutionHistory(String jobName, Date date) {
        log.trace("Removing {} job execution history older then a {} date", jobName, date);

        List<JobInstance> jobInstances = null;
        if (jobName != null) {
            QueryBuilder qb = new QueryBuilder("select ji from JobInstance ji");
            qb.addCriterion("ji.code", "=", jobName, false);
            jobInstances = qb.getQuery(getEntityManager()).getResultList();
            if (jobInstances.isEmpty()) {
                log.info("Removed 0 job execution history which start date is older then a {} date", date);
                return 0;
            }
        }

        String sql = "delete from JobExecutionResultImpl t";
        QueryBuilder qb = new QueryBuilder(sql);
        if (jobName != null) {
            qb.addSqlCriterion("t.jobInstance in :jis", "jis", jobInstances);
        }
        qb.addCriterionDateRangeToTruncatedToDay("t.startDate", date);
        int itemsDeleted = qb.getQuery(getEntityManager()).executeUpdate();

        log.info("Removed {} job execution history which start date is older then a {} date ", itemsDeleted, date);
        return itemsDeleted;
    }

    @SuppressWarnings("unchecked")
    public List<JobExecutionResultImpl> find(String jobName, PaginationConfiguration configuration) {
        return getFindQuery(jobName, configuration).find(getEntityManager());
    }

    public long count(String jobName, PaginationConfiguration configuration) {
        return getFindQuery(jobName, configuration).count(getEntityManager());
    }

    public JobInstanceService getJobInstanceService() {
        return jobInstanceService;
    }

    @Override
    public List<JobExecutionResultImpl> findByCodeLike(String code) {
        throw new UnsupportedOperationException();
    }
}