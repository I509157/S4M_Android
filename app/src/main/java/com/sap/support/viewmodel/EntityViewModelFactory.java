package com.sap.support.viewmodel;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.os.Parcelable;

import com.sap.support.viewmodel.attachment.AttachmentViewModel;
import com.sap.support.viewmodel.configurationdata.ConfigurationDataViewModel;
import com.sap.support.viewmodel.contacts.ContactsViewModel;
import com.sap.support.viewmodel.custprodinst.CustProdInstViewModel;
import com.sap.support.viewmodel.expertarea.ExpertAreaViewModel;
import com.sap.support.viewmodel.incidentcreate.IncidentCreateViewModel;
import com.sap.support.viewmodel.incidentinfo.IncidentInfoViewModel;
import com.sap.support.viewmodel.loginuserdata.LoginUserDataViewModel;
import com.sap.support.viewmodel.messagecore.MessageCoreViewModel;
import com.sap.support.viewmodel.notesadvisor.NotesAdvisorViewModel;
import com.sap.support.viewmodel.systemsearch.SystemSearchViewModel;


/**
 * Custom factory class, which can create view models for entity subsets, which are
 * reached from a parent entity through a navigation property.
 */
public class EntityViewModelFactory implements ViewModelProvider.Factory {

	// application class
    private Application application;
	// name of the navigation property
    private String navigationPropertyName;
	// parent entity
    private Parcelable entityData;

	/**
	 * Creates a factory class for entity view models created following a navigation link.
	 *
	 * @param application parent application
	 * @param navigationPropertyName name of the navigation link
	 * @param entityData parent entity
	 */
    public EntityViewModelFactory(Application application, String navigationPropertyName, Parcelable entityData) {
        this.application = application;
        this.navigationPropertyName = navigationPropertyName;
        this.entityData = entityData;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        T retValue = null;
		switch(modelClass.getSimpleName()) {



			case "AttachmentViewModel":
				retValue = (T) new AttachmentViewModel(application, navigationPropertyName, entityData);
				break;
			case "ConfigurationDataViewModel":
				retValue = (T) new ConfigurationDataViewModel(application, navigationPropertyName, entityData);
				break;
			case "ContactsViewModel":
				retValue = (T) new ContactsViewModel(application, navigationPropertyName, entityData);
				break;
			case "CustProdInstViewModel":
				retValue = (T) new CustProdInstViewModel(application, navigationPropertyName, entityData);
				break;
			case "ExpertAreaViewModel":
				retValue = (T) new ExpertAreaViewModel(application, navigationPropertyName, entityData);
				break;
			case "IncidentCreateViewModel":
				retValue = (T) new IncidentCreateViewModel(application, navigationPropertyName, entityData);
				break;
			case "IncidentInfoViewModel":
				retValue = (T) new IncidentInfoViewModel(application, navigationPropertyName, entityData);
				break;
			case "LoginUserDataViewModel":
				retValue = (T) new LoginUserDataViewModel(application, navigationPropertyName, entityData);
				break;
			case "MessageCoreViewModel":
				retValue = (T) new MessageCoreViewModel(application, navigationPropertyName, entityData);
				break;
			case "NotesAdvisorViewModel":
				retValue = (T) new NotesAdvisorViewModel(application, navigationPropertyName, entityData);
				break;
			case "SystemSearchViewModel":
				retValue = (T) new SystemSearchViewModel(application, navigationPropertyName, entityData);
				break;
		}
		return retValue;
	}
}