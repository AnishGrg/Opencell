package org.meveo.api.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CountryIsoDto;
import org.meveo.api.dto.CurrencyIsoDto;
import org.meveo.api.dto.LanguageIsoDto;
import org.meveo.api.dto.ProviderDto;
import org.meveo.api.dto.response.GetCountryIsoResponse;
import org.meveo.api.dto.response.GetCurrencyIsoResponse;
import org.meveo.api.dto.response.GetLanguageIsoResponse;
import org.meveo.api.dto.response.GetProviderResponse;

/**
 * @author Edward P. Legaspi
 **/
@WebService(targetNamespace = "http://superAdmin.ws.api.meveo.org/")
public interface SuperAdminSettingsWs extends IBaseWs {

    // provider

    @WebMethod
    public ActionStatus createProvider(@WebParam(name = "provider") ProviderDto postData);

    @WebMethod
    public GetProviderResponse findProvider(@WebParam(name = "providerCode") String providerCode);

    @WebMethod
    public ActionStatus updateProvider(@WebParam(name = "provider") ProviderDto postData);

    @WebMethod
    public ActionStatus createOrUpdateProvider(@WebParam(name = "provider") ProviderDto postData);

   // language

    @WebMethod
    public ActionStatus createLanguage(@WebParam(name = "languageIso") LanguageIsoDto languageIsoDto);

    @WebMethod
    public GetLanguageIsoResponse findLanguage(@WebParam(name = "languageCode") String languageCode);

    @WebMethod
    public ActionStatus removeLanguage(@WebParam(name = "languageCode") String languageCode);

    @WebMethod
    public ActionStatus updateLanguage(@WebParam(name = "languageIso") LanguageIsoDto languageIsoDto);

    @WebMethod
    public ActionStatus createOrUpdateLanguage(@WebParam(name = "languageIso") LanguageIsoDto languageIsoDto);

    // country

    @WebMethod
    ActionStatus createCountry(@WebParam(name = "countryIso") CountryIsoDto countryIsoDto);

    @WebMethod
    GetCountryIsoResponse findCountry(@WebParam(name = "countryIsoCode") String countryCode);

    @WebMethod
    ActionStatus removeCountry(@WebParam(name = "countryCode") String countryCode);

    @WebMethod
    ActionStatus updateCountry(@WebParam(name = "countryIso") CountryIsoDto countryIsoDto);

    @WebMethod
    ActionStatus createOrUpdateCountry(@WebParam(name = "countryIso") CountryIsoDto countryisoDto);

    // currency

    @WebMethod
    ActionStatus createCurrency(@WebParam(name = "currencyIso") CurrencyIsoDto currencyIsoDto);

    @WebMethod
    GetCurrencyIsoResponse findCurrency(@WebParam(name = "currencyCode") String currencyCode);

    @WebMethod
    ActionStatus removeCurrency(@WebParam(name = "currencyCode") String currencyCode);

    @WebMethod
    ActionStatus updateCurrency(@WebParam(name = "currencyIso") CurrencyIsoDto currencyIsoDto);

    @WebMethod
    ActionStatus createOrUpdateCurrency(@WebParam(name = "currencyIso") CurrencyIsoDto currencyIsoDto);
}
