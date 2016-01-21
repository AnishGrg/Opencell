package org.meveo.api.rest.impl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.MeveoApiErrorCode;
import org.meveo.api.ScriptInstanceApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.api.dto.response.GetScriptInstanceResponseDto;
import org.meveo.api.dto.response.ScriptInstanceReponseDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.logging.LoggingInterceptor;
import org.meveo.api.rest.ScriptInstanceRs;

/**
 * @author Edward P. Legaspi
 **/
@RequestScoped
@Interceptors({ LoggingInterceptor.class })
@Api(value = "/scriptInstance", tags = "scriptInstance")
public class ScriptInstanceRsImpl extends BaseRs implements ScriptInstanceRs {

    @Inject
    private ScriptInstanceApi scriptInstanceApi;

    @Override
    @ApiOperation(value = "")
    public ScriptInstanceReponseDto create(ScriptInstanceDto postData) {
        ScriptInstanceReponseDto result = new ScriptInstanceReponseDto();
        try {
            result.setCompilationErrors(scriptInstanceApi.create(postData, getCurrentUser()));
            result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        } catch (MeveoApiException e) {
            result.getActionStatus().setErrorCode(e.getErrorCode());
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());

        } catch (Exception e) {
            result.getActionStatus().setErrorCode(MeveoApiErrorCode.GENERIC_API_EXCEPTION);
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());
        }
        log.debug("RESPONSE={}", result);
        return result;
    }

    @Override
    @ApiOperation(value = "")
    public ScriptInstanceReponseDto update(ScriptInstanceDto postData) {
        ScriptInstanceReponseDto result = new ScriptInstanceReponseDto();
        try {
            result.setCompilationErrors(scriptInstanceApi.update(postData, getCurrentUser()));
            result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        } catch (MeveoApiException e) {
            result.getActionStatus().setErrorCode(e.getErrorCode());
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());
        } catch (Exception e) {
            result.getActionStatus().setErrorCode(MeveoApiErrorCode.GENERIC_API_EXCEPTION);
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());
        }
        log.debug("RESPONSE={}", result);
        return result;
    }

    @Override
    @ApiOperation(value = "")
    public ActionStatus remove(String scriptInstanceCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            scriptInstanceApi.removeScriptInstance(scriptInstanceCode, getCurrentUser());
        } catch (MeveoApiException e) {
            result.setErrorCode(e.getErrorCode());
            result.setStatus(ActionStatusEnum.FAIL);
            result.setMessage(e.getMessage());

        } catch (Exception e) {
            result.setErrorCode(MeveoApiErrorCode.GENERIC_API_EXCEPTION);
            result.setStatus(ActionStatusEnum.FAIL);
            result.setMessage(e.getMessage());
        }
        log.debug("RESPONSE={}", result);
        return result;
    }

    @Override
    @ApiOperation(value = "")
    public GetScriptInstanceResponseDto find(String scriptInstanceCode) {
        GetScriptInstanceResponseDto result = new GetScriptInstanceResponseDto();
        try {
            result.setScriptInstance(scriptInstanceApi.findScriptInstance(scriptInstanceCode, getCurrentUser()));
        } catch (MeveoApiException e) {
            result.getActionStatus().setErrorCode(e.getErrorCode());
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());
        } catch (Exception e) {
            result.getActionStatus().setErrorCode(MeveoApiErrorCode.GENERIC_API_EXCEPTION);
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());
        }

        log.debug("RESPONSE={}", result);
        return result;
    }

    @Override
    @ApiOperation(value = "")
    public ScriptInstanceReponseDto createOrUpdate(ScriptInstanceDto postData) {
        ScriptInstanceReponseDto result = new ScriptInstanceReponseDto();
        try {
            result.setCompilationErrors(scriptInstanceApi.createOrUpdate(postData, getCurrentUser()));
            result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);
        } catch (MeveoApiException e) {
            result.getActionStatus().setErrorCode(e.getErrorCode());
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());
        } catch (Exception e) {
            result.getActionStatus().setErrorCode(MeveoApiErrorCode.GENERIC_API_EXCEPTION);
            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
            result.getActionStatus().setMessage(e.getMessage());
        }
        log.debug("RESPONSE={}", result);
        return result;
    }

}
