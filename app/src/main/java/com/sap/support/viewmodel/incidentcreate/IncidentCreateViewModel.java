package com.sap.support.viewmodel.incidentcreate;

import android.app.Application;
import android.os.Parcelable;

import com.sap.support.viewmodel.EntityViewModel;
import com.sap.cloud.android.odata.am_incident_srv_entities.IncidentCreate;
import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_EntitiesMetadata.EntitySets;

/*
 * Represents View model for IncidentCreate
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and
 * return the view model of that type. This is because the ViewModelStore of
 * ViewModelProvider cannot not be able to tell the difference between EntityViewModel<type1>
 * and EntityViewModel<type2>.
 */
public class IncidentCreateViewModel extends EntityViewModel<IncidentCreate> {

    /**
    * Default constructor for a specific view model.
    * @param application - parent application
    */
    public IncidentCreateViewModel(Application application) {
        super(application, EntitySets.incidentCreateSet, IncidentCreate.sysnr);
    }

    /**
    * Constructor for a specific view model with navigation data.
    * @param application - parent application
    * @param navigationPropertyName - name of the navigation property
    * @param entityData - parent entity (starting point of the navigation)
    */
	 public IncidentCreateViewModel(Application application, String navigationPropertyName, Parcelable entityData) {
        super(application, EntitySets.incidentCreateSet, IncidentCreate.sysnr, navigationPropertyName, entityData);
    }
}
