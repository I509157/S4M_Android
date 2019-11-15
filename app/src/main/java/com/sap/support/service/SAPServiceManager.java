package com.sap.support.service;

import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_Entities;
import com.sap.support.app.ConfigurationData;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import com.sap.cloud.mobile.odata.OnlineODataProvider;
import com.sap.cloud.mobile.odata.core.Action0;
import com.sap.cloud.mobile.odata.http.OKHttpHandler;

public class SAPServiceManager {

    private final ConfigurationData configurationData;
    private OnlineODataProvider provider;
    private String serviceRoot;
    AM_INCIDENT_SRV_Entities aM_INCIDENT_SRV_Entities;
    public static final String CONNECTION_ID_AM_INCIDENT_SRV_ENTITIES = "com.sap.support.msggo.release";

    public SAPServiceManager(ConfigurationData configurationData) {
        this.configurationData = configurationData;
    }

    public void openODataStore(Action0 callback) {
        if (configurationData.loadData()) {
            String serviceUrl = configurationData.getServiceUrl();
            provider = new OnlineODataProvider("SAPService", serviceUrl + CONNECTION_ID_AM_INCIDENT_SRV_ENTITIES);
            provider.getNetworkOptions().setHttpHandler(new OKHttpHandler(ClientProvider.get()));
            provider.getServiceOptions().setCheckVersion(false);
            provider.getServiceOptions().setRequiresType(true);
            provider.getServiceOptions().setCacheMetadata(false);
            aM_INCIDENT_SRV_Entities = new AM_INCIDENT_SRV_Entities(provider);

        }
        callback.call();
    }

    public String getServiceRoot() {
        if (serviceRoot == null) {
            if (aM_INCIDENT_SRV_Entities == null) {
                throw new IllegalStateException("SAPServiceManager was not initialized");
            }
            provider = (OnlineODataProvider)aM_INCIDENT_SRV_Entities.getProvider();
            serviceRoot = provider.getServiceRoot();
        }
        return serviceRoot;
    }

    // This getter is used for the master-detail ui generation
    public AM_INCIDENT_SRV_Entities getAM_INCIDENT_SRV_Entities() {
        if (aM_INCIDENT_SRV_Entities == null) {
            throw new IllegalStateException("SAPServiceManager was not initialized");
        }
        return aM_INCIDENT_SRV_Entities;
    }

}