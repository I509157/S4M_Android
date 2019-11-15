package com.sap.support.mdui.configurationdataset;

import androidx.lifecycle.ViewModelProviders;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.sap.support.R;
import com.sap.support.app.ErrorHandler;
import com.sap.support.app.ErrorMessage;
import com.sap.support.app.SAPWizardApplication;
import com.sap.support.databinding.FragmentConfigurationdatasetCreateBinding;
import com.sap.support.mdui.BundleKeys;
import com.sap.support.mdui.InterfacedFragment;
import com.sap.support.mdui.UIConstants;
import com.sap.support.repository.OperationResult;
import com.sap.support.viewmodel.configurationdata.ConfigurationDataViewModel;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;
import com.sap.cloud.android.odata.am_incident_srv_entities.ConfigurationData;
import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_EntitiesMetadata.EntityTypes;
import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_EntitiesMetadata.EntitySets;
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell;
import com.sap.cloud.mobile.odata.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fragment that presents a screen to either create or update an existing ConfigurationData entity.
 * This fragment is contained in the {@link ConfigurationDataSetActivity}.
 */
public class ConfigurationDataSetCreateFragment extends InterfacedFragment<ConfigurationData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDataSetCreateFragment.class);
    //The key for the saved instance of the working entity for device configuration change
    private static final String KEY_WORKING_COPY = "WORKING_COPY";

    /** ConfigurationData object and it's copy: the modifications are done on the copied object. */
    private ConfigurationData configurationDataEntity;
    private ConfigurationData configurationDataEntityCopy;

    /** DataBinding generated class */
    private FragmentConfigurationdatasetCreateBinding binding;

    /** Indicate what operation to be performed */
    private String operation;

    /** ConfigurationData ViewModel */
    private ConfigurationDataViewModel viewModel;

    /** Application error handler to report error */
    private ErrorHandler errorHandler;

    /** The update menu item */
    private MenuItem updateMenuItem;

    /**
     * This fragment is used for both update and create for ConfigurationDataSet to enter values for the properties.
     * When used for update, an instance of the entity is required. In the case of create, a new instance
     * of the entity with defaults will be created. The default values may not be acceptable for the
     * OData service.
     * Arguments: Operation: [OP_CREATE | OP_UPDATE]
     *            ConfigurationData if Operation is update
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menu = R.menu.itemlist_edit_options;
        setHasOptionsMenu(true);
        errorHandler = ((SAPWizardApplication)currentActivity.getApplication()).getErrorHandler();

        Bundle bundle = getArguments();
        operation = bundle.getString(BundleKeys.OPERATION);
        if (UIConstants.OP_CREATE.equals(operation)) {
            activityTitle = currentActivity.getResources().getString(R.string.title_create_fragment, EntityTypes.configurationData.getLocalName());
        } else {
            activityTitle = currentActivity.getResources().getString(R.string.title_update_fragment) + " " + EntityTypes.configurationData.getLocalName();
        }

        ((ConfigurationDataSetActivity)currentActivity).isNavigationDisabled = true;
        if(secondaryToolbar != null) {
            secondaryToolbar.setTitle(activityTitle);
        } else {
            currentActivity.setTitle(activityTitle);
        }

        viewModel = ViewModelProviders.of(currentActivity).get(ConfigurationDataViewModel.class);
        viewModel.getCreateResult().observe(this, result -> onComplete(result));
        viewModel.getUpdateResult().observe(this, result -> onComplete(result));

        if(UIConstants.OP_CREATE.equals(operation)) {
            configurationDataEntity = createConfigurationData();
        } else {
            configurationDataEntity = viewModel.getSelectedEntity().getValue();
        }

        ConfigurationData workingCopy = null;
        if( savedInstanceState != null ) {
            workingCopy =  (ConfigurationData)savedInstanceState.getParcelable(KEY_WORKING_COPY);
        }
        if( workingCopy == null ) {
            configurationDataEntityCopy = (ConfigurationData) configurationDataEntity.copy();
            configurationDataEntityCopy.setEntityTag(configurationDataEntity.getEntityTag());
            configurationDataEntityCopy.setOldEntity(configurationDataEntity);
            configurationDataEntityCopy.setEditLink((configurationDataEntity.getEditLink()));
        } else {
            //in this case, the old entity and entity tag should already been set.
            configurationDataEntityCopy = workingCopy;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ObjectHeader objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if( objectHeader != null ) objectHeader.setVisibility(View.GONE);
        return setupDataBinding(inflater, container);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(KEY_WORKING_COPY, configurationDataEntityCopy);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                updateMenuItem = item;
                enableUpdateMenuItem(false);
                return onSaveItem();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** 
     * Enables or disables the update menu item base on the given 'enable'
     * @param enable true to enable the menu item, false otherwise
     */
    private void enableUpdateMenuItem(boolean enable) {
        updateMenuItem.setEnabled(enable);
        updateMenuItem.getIcon().setAlpha( enable ? 255 : 130);
    }

    /**
     * Saves the entity
     */
    private boolean onSaveItem() {
        if (!isConfigurationDataValid()) {
            return false;
        }
        //set 'isNavigationDisabled' false here to make sure the logic in list is ok, and set it to true if update fails.
        ((ConfigurationDataSetActivity)currentActivity).isNavigationDisabled = false;
        if( progressBar != null ) progressBar.setVisibility(View.VISIBLE);
        if (operation.equals(UIConstants.OP_CREATE)) {
            viewModel.create(configurationDataEntityCopy);
        } else {
            viewModel.update(configurationDataEntityCopy);
        }
        return true;
    }

    /**
     * Create a new ConfigurationData instance and initialize properties to its default values
     * Nullable property will remain null
     * @return new ConfigurationData instance
     */
    private ConfigurationData createConfigurationData() {
        ConfigurationData configurationDataEntity = new ConfigurationData(true);
        return configurationDataEntity;
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private void onComplete(@NonNull OperationResult<ConfigurationData> result) {
        if( progressBar != null ) progressBar.setVisibility(View.INVISIBLE);
        enableUpdateMenuItem(true);
        if (result.getError() != null) {
            ((ConfigurationDataSetActivity)currentActivity).isNavigationDisabled = true;
            handleError(result);
        } else {
            boolean isMasterDetail = currentActivity.getResources().getBoolean(R.bool.two_pane);
            if( UIConstants.OP_UPDATE.equals(operation) && !isMasterDetail) {
                viewModel.setSelectedEntity(configurationDataEntityCopy);
            }
            currentActivity.onBackPressed();
        }
    }

    /** Simple validation: checks the presence of mandatory fields. */
    private boolean isValidProperty(@NonNull Property property, @NonNull String value) {
        boolean isValid = true;
        if (!property.isNullable() && value.isEmpty()) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Set up data binding for this view
     * @param inflater - layout inflater from onCreateView
     * @param container - view group from onCreateView
     * @return view - rootView from generated data binding code
     */
    private View setupDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        binding = FragmentConfigurationdatasetCreateBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.setConfigurationData(configurationDataEntityCopy);
        return rootView;
    }

    /** Validate the edited inputs */
    private boolean isConfigurationDataValid() {
        LinearLayout linearLayout = getView().findViewById(R.id.create_update_configurationdata);
        boolean isValid = true;
        // validate properties i.e. check non-nullable properties are truly non-null
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View viewItem = linearLayout.getChildAt(i);
            SimplePropertyFormCell simplePropertyFormCell = (SimplePropertyFormCell)viewItem;
            String propertyName = (String) simplePropertyFormCell.getTag();
            Property property = EntityTypes.configurationData.getProperty(propertyName);
            String value = simplePropertyFormCell.getValue().toString();
            if (!isValidProperty(property, value)) {
                simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, true);
                String errorMessage = getResources().getString(R.string.mandatory_warning);
                simplePropertyFormCell.setErrorEnabled(true);
                simplePropertyFormCell.setError(errorMessage);
                isValid = false;
            }
            else {
                if (simplePropertyFormCell.isErrorEnabled()){
                    boolean hasMandatoryError = (Boolean)simplePropertyFormCell.getTag(R.id.TAG_HAS_MANDATORY_ERROR);
                    if (!hasMandatoryError) {
                        isValid = false;
                    } else {
                        simplePropertyFormCell.setErrorEnabled(false);
                    }
                }
                simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, false);
            }
        }
        return isValid;
    }

    /**
     * Notify user of error encountered while execution the operation
     * @param result - operation result with error
     */
    private void handleError(@NonNull OperationResult<ConfigurationData> result) {
        ErrorMessage errorMessage;
        switch (result.getOperation()) {
            case UPDATE:
                errorMessage = new ErrorMessage(getResources().getString(R.string.update_failed),
                        getResources().getString(R.string.update_failed_detail), result.getError(), false);
                break;
            case CREATE:
                errorMessage = new ErrorMessage(getResources().getString(R.string.create_failed),
                        getResources().getString(R.string.create_failed_detail), result.getError(), false);
                break;
            default:
                throw new AssertionError();
        }
        errorHandler.sendErrorMessage(errorMessage);
    }
}
