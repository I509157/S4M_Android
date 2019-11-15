package com.sap.support.mdui.systemsearchset;

import android.content.Intent;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.sap.support.service.SAPServiceManager;
import com.sap.support.R;
import com.sap.support.app.ErrorHandler;
import com.sap.support.app.ErrorMessage;
import com.sap.support.app.SAPWizardApplication;
import com.sap.support.databinding.FragmentSystemsearchsetDetailBinding;
import com.sap.support.mdui.BundleKeys;
import com.sap.support.mdui.InterfacedFragment;
import com.sap.support.mdui.UIConstants;
import com.sap.support.mdui.EntityKeyUtil;
import com.sap.support.repository.OperationResult;
import com.sap.support.viewmodel.systemsearch.SystemSearchViewModel;
import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_EntitiesMetadata.EntitySets;
import com.sap.cloud.android.odata.am_incident_srv_entities.SystemSearch;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.DataValueList;
import com.sap.cloud.mobile.odata.EntityKey;
import com.sap.support.mediaresource.EntityMediaResource;
/**
 * A fragment representing a single SystemSearch detail screen.
 * This fragment is contained in an SystemSearchSetActivity.
 */
public class SystemSearchSetDetailFragment extends InterfacedFragment<SystemSearch> {

    /** Generated data binding class based on layout file */
    private FragmentSystemsearchsetDetailBinding binding;

    /** SystemSearch entity to be displayed */
    private SystemSearch systemSearchEntity = null;

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private ObjectHeader objectHeader;

    /** View model of the entity type that the displayed entity belongs to */
    private SystemSearchViewModel viewModel;

    /** Error handler to display message should error occurs */
    private ErrorHandler errorHandler;

    /**
     * Service manager to provide root URL of OData Service for Glide to load images if there are media resources
     * associated with the entity type
     */
    private SAPServiceManager sapServiceManager;

    /** Arguments: SystemSearch for display */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menu = R.menu.itemlist_view_options;
        errorHandler = ((SAPWizardApplication) currentActivity.getApplication()).getErrorHandler();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return setupDataBinding(inflater, container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(currentActivity).get(SystemSearchViewModel.class);
        viewModel.getDeleteResult().observe(this, result -> {
            onDeleteComplete(result);
        });
        viewModel.getSelectedEntity().observe(this, entity -> {
            systemSearchEntity = entity;
            binding.setSystemSearch(entity);
            setupObjectHeader();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_item:
                listener.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, systemSearchEntity);
                return true;
            case R.id.delete_item:
                listener.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION,null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /** Completion callback for delete operation */
    private void onDeleteComplete(@NonNull OperationResult<SystemSearch> result) {
        if( progressBar != null ) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        viewModel.removeAllSelected(); //to make sure the 'action mode' not activated in the list
        Exception ex = result.getError();
        if (ex != null) {
            handleError(ex);
            return;
        }
        listener.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, systemSearchEntity);
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private void setDetailImage(@NonNull ObjectHeader objectHeader, @NonNull SystemSearch systemSearchEntity) {
        if (EntityMediaResource.hasMediaResources(EntitySets.systemSearchSet)) {
            // Glide offers caching in addition to fetching the images
            objectHeader.prepareDetailImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(currentActivity)
                    .load(EntityMediaResource.getMediaResourceUrl(systemSearchEntity, sapServiceManager.getServiceRoot()))
                    .apply(new RequestOptions().fitCenter())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(objectHeader.getDetailImageView());
        } else if (systemSearchEntity.getDataValue(SystemSearch.customerNbr) != null && !systemSearchEntity.getDataValue(SystemSearch.customerNbr).toString().isEmpty()) {
            objectHeader.setDetailImageCharacter(systemSearchEntity.getDataValue(SystemSearch.customerNbr).toString().substring(0, 1));
        } else {
            objectHeader.setDetailImageCharacter("?");
        }
    }

    /**
     * Setup ObjectHeader with an instance of SystemSearch
     */
    private void setupObjectHeader() {
        Toolbar secondToolbar = currentActivity.findViewById(R.id.secondaryToolbar);
        if (secondToolbar != null) {
            secondToolbar.setTitle(systemSearchEntity.getEntityType().getLocalName());
        } else {
            currentActivity.setTitle(systemSearchEntity.getEntityType().getLocalName());
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if (objectHeader != null) {
            // Use of getDataValue() avoids the knowledge of what data type the master property is.
            // This is a convenience for wizard generated code. Normally, developer will use the proxy class
            // get<Property>() method and add code to convert to string
            DataValue dataValue = systemSearchEntity.getDataValue(SystemSearch.customerNbr);
            if (dataValue != null) {
                objectHeader.setHeadline(dataValue.toString());
            } else {
                objectHeader.setHeadline(null);
            }
            // EntityKey in string format: '{"key":value,"key2":value2}'
            objectHeader.setSubheadline(EntityKeyUtil.getOptionalEntityKey(systemSearchEntity));
            objectHeader.setTag("#tag1", 0);
            objectHeader.setTag("#tag3", 2);
            objectHeader.setTag("#tag2", 1);

            objectHeader.setBody("You can set the header body text here.");
            objectHeader.setFootnote("You can set the header footnote here.");
            objectHeader.setDescription("You can add a detailed item description here.");

            setDetailImage(objectHeader, systemSearchEntity);
            objectHeader.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up databinding for this view
     *
     * @param inflater - layout inflater from onCreateView
     * @param container - view group from onCreateView
     * @return view - rootView from generated databinding code
     */
    private View setupDataBinding(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentSystemsearchsetDetailBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.setHandler(this);
        return rootView;
    }

    /**
     * Notify user of error encountered during operation execution
     *
     * @param ex - exception encountered
     */
    private void handleError(Exception ex) {
        ErrorMessage errorMessage;
        errorMessage = new ErrorMessage(currentActivity.getResources().getString(R.string.delete_failed),
                currentActivity.getResources().getString(R.string.delete_failed_detail), ex, false);
        errorHandler.sendErrorMessage(errorMessage);
    }
}
