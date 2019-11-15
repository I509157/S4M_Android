package com.sap.support.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sap.support.service.SAPServiceManager;

import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_Entities;
import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_EntitiesMetadata.EntitySets;

import com.sap.cloud.android.odata.am_incident_srv_entities.Attachment;
import com.sap.cloud.android.odata.am_incident_srv_entities.ConfigurationData;
import com.sap.cloud.android.odata.am_incident_srv_entities.Contacts;
import com.sap.cloud.android.odata.am_incident_srv_entities.CustProdInst;
import com.sap.cloud.android.odata.am_incident_srv_entities.ExpertArea;
import com.sap.cloud.android.odata.am_incident_srv_entities.IncidentCreate;
import com.sap.cloud.android.odata.am_incident_srv_entities.IncidentInfo;
import com.sap.cloud.android.odata.am_incident_srv_entities.LoginUserData;
import com.sap.cloud.android.odata.am_incident_srv_entities.MessageCore;
import com.sap.cloud.android.odata.am_incident_srv_entities.NotesAdvisor;
import com.sap.cloud.android.odata.am_incident_srv_entities.SystemSearch;

import com.sap.cloud.mobile.odata.EntitySet;
import com.sap.cloud.mobile.odata.Property;

import java.util.WeakHashMap;

/*
 * Repository factory to construct repository for an entity set
 */
public class RepositoryFactory {

    /*
     * Cache all repositories created to avoid reconstruction and keeping the entities of entity set
     * maintained by each repository in memory. Use a weak hash map to allow recovery in low memory
     * conditions
     */
    private WeakHashMap<String, Repository> repositories;

    /*
     * Service manager to interact with OData service
     */
    private SAPServiceManager sapServiceManager;

    /**
     * Construct a RepositoryFactory instance. There should only be one repository factory and used
     * throughout the life of the application to avoid caching entities multiple times.
     * @param sapServiceManager - Service manager for interaction with OData service
     */
    public RepositoryFactory(SAPServiceManager sapServiceManager) {
        repositories = new WeakHashMap<>();
        this.sapServiceManager = sapServiceManager;
    }

    /**
     * Construct or return an existing repository for the specified entity set
     * @param entitySet - entity set for which the repository is to be returned
     * @param orderByProperty - if specified, collection will be sorted ascending with this property
     * @return a repository for the entity set
     */
    public Repository getRepository(@NonNull EntitySet entitySet, @Nullable Property orderByProperty) {
        AM_INCIDENT_SRV_Entities aM_INCIDENT_SRV_Entities = sapServiceManager.getAM_INCIDENT_SRV_Entities();
        String key = entitySet.getLocalName();
        Repository repository = repositories.get(key);
        if (repository == null) {
            if (key.equals(EntitySets.attachmentSet.getLocalName())) {
                repository = new Repository<Attachment>(aM_INCIDENT_SRV_Entities, EntitySets.attachmentSet, orderByProperty);
            } else if (key.equals(EntitySets.configurationDataSet.getLocalName())) {
                repository = new Repository<ConfigurationData>(aM_INCIDENT_SRV_Entities, EntitySets.configurationDataSet, orderByProperty);
            } else if (key.equals(EntitySets.contactsSet.getLocalName())) {
                repository = new Repository<Contacts>(aM_INCIDENT_SRV_Entities, EntitySets.contactsSet, orderByProperty);
            } else if (key.equals(EntitySets.custProdInstSet.getLocalName())) {
                repository = new Repository<CustProdInst>(aM_INCIDENT_SRV_Entities, EntitySets.custProdInstSet, orderByProperty);
            } else if (key.equals(EntitySets.expertAreaSet.getLocalName())) {
                repository = new Repository<ExpertArea>(aM_INCIDENT_SRV_Entities, EntitySets.expertAreaSet, orderByProperty);
            } else if (key.equals(EntitySets.incidentCreateSet.getLocalName())) {
                repository = new Repository<IncidentCreate>(aM_INCIDENT_SRV_Entities, EntitySets.incidentCreateSet, orderByProperty);
            } else if (key.equals(EntitySets.incidentInfoSet.getLocalName())) {
                repository = new Repository<IncidentInfo>(aM_INCIDENT_SRV_Entities, EntitySets.incidentInfoSet, orderByProperty);
            } else if (key.equals(EntitySets.loginUserDataSet.getLocalName())) {
                repository = new Repository<LoginUserData>(aM_INCIDENT_SRV_Entities, EntitySets.loginUserDataSet, orderByProperty);
            } else if (key.equals(EntitySets.messageCoreSet.getLocalName())) {
                repository = new Repository<MessageCore>(aM_INCIDENT_SRV_Entities, EntitySets.messageCoreSet, orderByProperty);
            } else if (key.equals(EntitySets.notesAdvisorSet.getLocalName())) {
                repository = new Repository<NotesAdvisor>(aM_INCIDENT_SRV_Entities, EntitySets.notesAdvisorSet, orderByProperty);
            } else if (key.equals(EntitySets.systemSearchSet.getLocalName())) {
                repository = new Repository<SystemSearch>(aM_INCIDENT_SRV_Entities, EntitySets.systemSearchSet, orderByProperty);
            } else {
                throw new AssertionError("Fatal error, entity set[" + key + "] missing in generated code");
            }
            repositories.put(key, repository);
        }
        return repository;
    }

    /**
     * Get rid of all cached repositories
     */
    public void reset() {
        repositories.clear();
    }
 }
