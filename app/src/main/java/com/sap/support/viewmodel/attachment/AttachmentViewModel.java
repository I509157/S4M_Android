package com.sap.support.viewmodel.attachment;

import android.app.Application;
import android.os.Parcelable;

import com.sap.support.viewmodel.EntityViewModel;
import com.sap.cloud.android.odata.am_incident_srv_entities.Attachment;
import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_EntitiesMetadata.EntitySets;

/*
 * Represents View model for Attachment
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and
 * return the view model of that type. This is because the ViewModelStore of
 * ViewModelProvider cannot not be able to tell the difference between EntityViewModel<type1>
 * and EntityViewModel<type2>.
 */
public class AttachmentViewModel extends EntityViewModel<Attachment> {

    /**
    * Default constructor for a specific view model.
    * @param application - parent application
    */
    public AttachmentViewModel(Application application) {
        super(application, EntitySets.attachmentSet, Attachment.pointer);
    }

    /**
    * Constructor for a specific view model with navigation data.
    * @param application - parent application
    * @param navigationPropertyName - name of the navigation property
    * @param entityData - parent entity (starting point of the navigation)
    */
	 public AttachmentViewModel(Application application, String navigationPropertyName, Parcelable entityData) {
        super(application, EntitySets.attachmentSet, Attachment.pointer, navigationPropertyName, entityData);
    }
}
