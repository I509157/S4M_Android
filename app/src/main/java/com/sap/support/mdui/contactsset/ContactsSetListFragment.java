package com.sap.support.mdui.contactsset;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import com.sap.support.R;
import com.sap.support.app.ErrorHandler;
import com.sap.support.app.ErrorMessage;
import com.sap.support.app.SAPWizardApplication;
import com.sap.support.mdui.BundleKeys;
import com.sap.support.mdui.EntitySetListActivity;
import com.sap.support.mdui.EntitySetListActivity.EntitySetName;
import com.sap.support.mdui.InterfacedFragment;
import com.sap.support.mdui.UIConstants;
import com.sap.support.mdui.EntityKeyUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.sap.support.mediaresource.EntityMediaResource;
import com.sap.support.repository.OperationResult;
import com.sap.support.service.SAPServiceManager;
import com.sap.support.viewmodel.EntityViewModelFactory;
import com.sap.support.viewmodel.contacts.ContactsViewModel;
import com.sap.cloud.android.odata.am_incident_srv_entities.AM_INCIDENT_SRV_EntitiesMetadata.EntitySets;
import com.sap.cloud.android.odata.am_incident_srv_entities.Contacts;
import com.sap.cloud.mobile.fiori.object.ObjectCell;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.EntityValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ContactsSetListFragment extends InterfacedFragment<Contacts> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactsSetActivity.class);

    /**
     * Service manager to provide root URL of OData Service for Glide to load images if there are media resources
     * associated with the entity type
     */
    private SAPServiceManager sapServiceManager;

    /**
     * Error handler to display message should error occurs
     */
    private ErrorHandler errorHandler;

    /**
     * List adapter to be used with RecyclerView containing all instances of contactsSet
     */
    private ContactsListAdapter adapter;

    private SwipeRefreshLayout refreshLayout;

    /**
     * View model of the entity type
     */
    private ContactsViewModel viewModel;

    private ActionMode actionMode;
    private Boolean isInActionMode = false;
    private List<Integer> selectedItems = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshLayout.setRefreshing(true);
                refreshListData();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTitle = getString(EntitySetListActivity.EntitySetName.ContactsSet.getTitleId());
        menu = R.menu.itemlist_menu;
        setHasOptionsMenu(true);
        if( savedInstanceState != null ) {
            isInActionMode = savedInstanceState.getBoolean("ActionMode");
        }

        sapServiceManager = ((SAPWizardApplication) currentActivity.getApplication()).getSAPServiceManager();
        errorHandler = ((SAPWizardApplication) currentActivity.getApplication()).getErrorHandler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if( objectHeader != null) {
            objectHeader.setVisibility(View.GONE);
        }
        return inflater.inflate(R.layout.fragment_entityitem_list, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(this.menu, menu);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        currentActivity.setTitle(activityTitle);
        RecyclerView recyclerView = currentActivity.findViewById(R.id.item_list);
        if (recyclerView == null) throw new AssertionError();
        this.adapter = new ContactsListAdapter(currentActivity);
        recyclerView.setAdapter(adapter);

        setupRefreshLayout();
        refreshLayout.setRefreshing(true);

        navigationPropertyName = currentActivity.getIntent().getStringExtra("navigation");
        parentEntityData = currentActivity.getIntent().getParcelableExtra("parent");

        FloatingActionButton floatButton = currentActivity.findViewById(R.id.fab);
        if (floatButton != null) {
            if (navigationPropertyName != null && parentEntityData != null) {
                floatButton.hide();
            } else {
                floatButton.setOnClickListener((v) -> {
                    listener.onFragmentStateChange(UIConstants.EVENT_CREATE_NEW_ITEM, null);
                });
            }
        }

        sapServiceManager.openODataStore(() -> {
            prepareViewModel();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("ActionMode", isInActionMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshListData();
    }

    /** Initializes the view model and add observers on it */
    private void prepareViewModel() {
        if( navigationPropertyName != null && parentEntityData != null ) {
            viewModel = ViewModelProviders.of(currentActivity, new EntityViewModelFactory(currentActivity.getApplication(), navigationPropertyName, parentEntityData))
                .get(ContactsViewModel.class);
        } else {
            viewModel = ViewModelProviders.of(currentActivity).get(ContactsViewModel.class);
            viewModel.initialRead();
        }

        viewModel.getObservableItems().observe(this, contactsSet -> {
            if (contactsSet != null) {
                adapter.setItems(contactsSet);

                Contacts item = containsItem(contactsSet, viewModel.getSelectedEntity().getValue());
                if (item == null) {
                    item = contactsSet.isEmpty() ? null : contactsSet.get(0);
                }

                if (item == null) {
                    hideDetailFragment();
                } else {
                    viewModel.setInFocusId(adapter.getItemIdForContacts(item));
                    if (currentActivity.getResources().getBoolean(R.bool.two_pane)) {
                        viewModel.setSelectedEntity(item);
                        if (!isInActionMode && !((ContactsSetActivity) currentActivity).isNavigationDisabled) {
                            listener.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            refreshLayout.setRefreshing(false);
        });

        viewModel.getReadResult().observe(this, state -> {
            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        });

        viewModel.getDeleteResult().observe(this, result -> {
            onDeleteComplete(result);
        });
    }

    /** Searches 'item' in the refreshed list, if found, returns the one in list */
    private Contacts containsItem(List<Contacts> items, Contacts item) {
        Contacts found = null;
        if( item != null ) {
            for( Contacts entity: items ) {
                if( adapter.getItemIdForContacts(entity) == adapter.getItemIdForContacts(item)) {
                    found = entity;
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Hides the detail fragment.
     */
    private void hideDetailFragment() {
        Fragment detailFragment = currentActivity.getSupportFragmentManager().findFragmentByTag(UIConstants.DETAIL_FRAGMENT_TAG);
        if( detailFragment != null ) {
            currentActivity.getSupportFragmentManager().beginTransaction()
                .remove(detailFragment).commit();
        }
        if( secondaryToolbar != null ) {
            secondaryToolbar.getMenu().clear();
            secondaryToolbar.setTitle("");
        }
        View objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if( objectHeader != null) {
            objectHeader.setVisibility(View.GONE);
        }
    }

    /** Callback function for delete operation */
    private void onDeleteComplete(@NonNull OperationResult<Contacts> result) {
        if( progressBar != null ) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        viewModel.removeAllSelected();
        if (actionMode != null) {
            actionMode.finish();
            isInActionMode = false;
        }

        if (result.getError() != null) {
            handleDeleteError(result.getError());
        } else {
            refreshListData();
        }
    }

    /** Refreshes the list data */
    void refreshListData() {
        if (navigationPropertyName != null && parentEntityData != null) {
            viewModel.refresh((EntityValue) parentEntityData, navigationPropertyName);
        } else {
            viewModel.refresh();
        }
        adapter.notifyDataSetChanged();
    }

    /** Sets the selected item id into view model */
    private Contacts setItemIdSelected(int itemId) {
        LiveData<List<Contacts>> liveData = viewModel.getObservableItems();
        List<Contacts> contactsSet = liveData.getValue();
        if (contactsSet != null && contactsSet.size() > 0) {
            viewModel.setInFocusId(adapter.getItemIdForContacts(contactsSet.get(itemId)));
            return contactsSet.get(itemId);
        }
        return null;
    }

    /** Sets up the refresh layout */
    private void setupRefreshLayout() {
        refreshLayout = currentActivity.findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeColors(UIConstants.FIORI_STANDARD_THEME_GLOBAL_DARK_BASE);
        refreshLayout.setProgressBackgroundColorSchemeColor(UIConstants.FIORI_STANDARD_THEME_BACKGROUND);
        refreshLayout.setOnRefreshListener(this::refreshListData);
    }

    /** Callback function to handle deletion error */
    private void handleDeleteError(@NonNull Exception exception) {
        ErrorMessage errorMessage;
        errorMessage = new ErrorMessage(getResources().getString(R.string.delete_failed),
                getResources().getString(R.string.delete_failed_detail), exception, false);
        errorHandler.sendErrorMessage(errorMessage);
        refreshLayout.setRefreshing(false);
    }

    /**
     * Represents the action mode of the list.
     */
    public class ContactsSetListActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            isInActionMode = true;
            FloatingActionButton fab = currentActivity.findViewById(R.id.fab);
            if (fab != null) {
                fab.hide();
            }
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.itemlist_view_options, menu);
            hideDetailFragment();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.update_item:
                    Contacts contactsEntity = viewModel.getSelected(0);
                    if (viewModel.numberOfSelected() == 1 && contactsEntity != null) {
                        isInActionMode = false;
                        actionMode.finish();
                        viewModel.setSelectedEntity(contactsEntity);
                        if( currentActivity.getResources().getBoolean(R.bool.two_pane)) {
                            listener.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, contactsEntity);
                        }
                        listener.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, contactsEntity);
                    }
                    return true;
                case R.id.delete_item:
                    listener.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION, null);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            isInActionMode = false;
            if (!(navigationPropertyName != null && parentEntityData != null)) {
                FloatingActionButton fab = currentActivity.findViewById(R.id.fab);
                if (fab != null) {
                    fab.show();
                }
            }
            selectedItems.clear();
            viewModel.removeAllSelected();
            refreshListData();
        }
    }

    /**
     * List adapter to be used with RecyclerView. It contains the set of contactsSet.
     */
    public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ViewHolder> {

        private Context context;

        /** Entire list of Contacts collection */
        private List<Contacts> contactsSet;

        /** RecyclerView this adapter is associate with */
        private RecyclerView recyclerView;

        /** Flag to indicate whether we have checked retained selected contactsSet */
        private boolean checkForSelectedOnCreate = false;

        public ContactsListAdapter(Context context) {
            this.context = context;
            this.recyclerView = currentActivity.findViewById(R.id.item_list);
            if (this.recyclerView == null) throw new AssertionError();
            setHasStableIds(true);
        }

        /**
         * Use DiffUtil to calculate the difference and dispatch them to the adapter
         * Note: Please use background thread for calculation if the list is large to avoid blocking main thread
         */
        @WorkerThread
        public void setItems(@NonNull List<Contacts> currentContactsSet) {
            if (contactsSet == null) {
                contactsSet = new ArrayList<>(currentContactsSet);
                notifyItemRangeInserted(0, currentContactsSet.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return contactsSet.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return currentContactsSet.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return contactsSet.get(oldItemPosition).getEntityKey().toString().equals(
                                currentContactsSet.get(newItemPosition).getEntityKey().toString());
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Contacts contactsEntity = contactsSet.get(oldItemPosition);
                        return !contactsEntity.isUpdated() && currentContactsSet.get(newItemPosition).equals(contactsEntity);
                    }

                    @Nullable
                    @Override
                    public Object getChangePayload(final int oldItemPosition, final int newItemPosition) {
                        return super.getChangePayload(oldItemPosition, newItemPosition);
                    }
                });
                contactsSet.clear();
                contactsSet.addAll(currentContactsSet);
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public final long getItemId(int position) {
            return getItemIdForContacts(contactsSet.get(position));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_entityitem_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

            checkForRetainedSelection();

            final Contacts contactsEntity = contactsSet.get(holder.getAdapterPosition());
            DataValue dataValue = contactsEntity.getDataValue(Contacts.contTyp);
            if (dataValue != null) {
                holder.masterPropertyValue = dataValue.toString();
            }
            populateObjectCell(holder, contactsEntity);

            boolean isActive = getItemIdForContacts(contactsEntity) == viewModel.getInFocusId();
            if (isActive) {
                setItemIdSelected(holder.getAdapterPosition());
            }
            boolean isContactsSelected = viewModel.selectedContains(contactsEntity);
            setViewBackground(holder.view, isContactsSelected, isActive);

            holder.view.setOnLongClickListener(new onActionModeStartClickListener(holder));
            setOnClickListener(holder, contactsEntity);

            setOnCheckedChangeListener(holder, contactsEntity);
            holder.setSelected(isContactsSelected);
            setDetailImage(holder, contactsEntity);
        }

        /**
         * Check to see if there are an retained selected contactsEntity on start.
         * This situation occurs when a rotation with selected contactsSet is triggered by user.
         */
        private void checkForRetainedSelection() {
            if (!checkForSelectedOnCreate) {
                checkForSelectedOnCreate = true;
                if (viewModel.numberOfSelected() > 0) {
                    manageActionModeOnCheckedTransition();
                }
            }
        }

        /**
         * If there are selected contactsSet via long press, clear them as click and long press are mutually exclusive
         * In addition, since we are clearing all selected contactsSet via long press, finish the action mode.
         */
        private void resetSelected() {
            if (viewModel.numberOfSelected() > 0) {
                viewModel.removeAllSelected();
                if (actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                }
            }
        }

        /**
         * Attempt to locate previously clicked view and reset its background
         * Reset view model's inFocusId
         */
        private void resetPreviouslyClicked() {
            long inFocusId = viewModel.getInFocusId();
            ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForItemId(inFocusId);
            if (viewHolder != null) {
                setViewBackground(viewHolder.view, viewHolder.isSelected, false);
            } else {
                viewModel.refresh();
            }
        }

        private void processClickAction(@NonNull View view, @NonNull Contacts contactsEntity) {
            resetPreviouslyClicked();
            setViewBackground(view, false, true);
            viewModel.setInFocusId(getItemIdForContacts(contactsEntity));
        }

        /**
         * Set ViewHolder's view onClickListener
         *
         * @param holder
         * @param contactsEntity associated with this ViewHolder
         */
        private void setOnClickListener(@NonNull ViewHolder holder, @NonNull Contacts contactsEntity) {
            holder.view.setOnClickListener(view -> {
                boolean isNavigationDisabled = ((ContactsSetActivity) currentActivity).isNavigationDisabled;
                if(isNavigationDisabled) {
                    Toast.makeText(currentActivity, "Please save your changes first...", Toast.LENGTH_LONG).show();
                } else {
                    resetSelected();
                    resetPreviouslyClicked();
                    processClickAction(view, contactsEntity);
                    viewModel.setSelectedEntity(contactsEntity);
                    listener.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, contactsEntity);
                }
            });
        }

        /**
         * Represents the listener to start the action mode
         */
        public class onActionModeStartClickListener implements View.OnClickListener, View.OnLongClickListener {

            ViewHolder holder;

            public onActionModeStartClickListener(@NonNull ViewHolder viewHolder) {
                this.holder = viewHolder;
            }

            @Override
            public void onClick(View view) {
                onAnyKindOfClick();
            }

            @Override
            public boolean onLongClick(View view) {
                return onAnyKindOfClick();
            }

            /** callback function for both normal and long click on an entity */
            private boolean onAnyKindOfClick() {
                boolean isNavigationDisabled = ((ContactsSetActivity) currentActivity).isNavigationDisabled;
                if( isNavigationDisabled ) {
                    Toast.makeText(currentActivity, "Please save your changes first...", Toast.LENGTH_LONG).show();
                } else {
                    if (!isInActionMode) {
                        actionMode = ((AppCompatActivity) currentActivity).startSupportActionMode(new ContactsSetListActionMode());
                        adapter.notifyDataSetChanged();
                    }
                    holder.setSelected(!holder.isSelected);
                }
                return true;
            }
        }

        /** sets the detail image to the given <code>viewHolder</code> */
        private void setDetailImage(@NonNull ViewHolder viewHolder, @NonNull Contacts contactsEntity) {
            if (isInActionMode) {
                int drawable;
                if (viewHolder.isSelected) {
                    drawable = R.drawable.ic_check_circle_black_24dp;
                } else {
                    drawable = R.drawable.ic_uncheck_circle_black_24dp;
                }
                viewHolder.objectCell.prepareDetailImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(context)
                        .load(getResources().getDrawable(drawable, null))
                        .apply(new RequestOptions().fitCenter())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(viewHolder.objectCell.prepareDetailImageView());
            } else if (EntityMediaResource.hasMediaResources(EntitySets.contactsSet)) {
                viewHolder.objectCell.prepareDetailImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(context)
                        .load(EntityMediaResource.getMediaResourceUrl(contactsEntity, sapServiceManager.getServiceRoot()))
                        .apply(new RequestOptions().fitCenter())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(viewHolder.objectCell.prepareDetailImageView());
            } else if (viewHolder.masterPropertyValue != null && !viewHolder.masterPropertyValue.isEmpty()) {
                viewHolder.objectCell.setDetailImageCharacter(viewHolder.masterPropertyValue.substring(0, 1));
            } else {
                viewHolder.objectCell.setDetailImageCharacter("?");
            }
        }

        /**
         * Set ViewHolder's CheckBox onCheckedChangeListener
         *
         * @param holder
         * @param contactsEntity associated with this ViewHolder
         */
        private void setOnCheckedChangeListener(@NonNull ViewHolder holder, @NonNull Contacts contactsEntity) {
            holder.checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked) {
                    viewModel.addSelected(contactsEntity);
                    manageActionModeOnCheckedTransition();
                    resetPreviouslyClicked();
                } else {
                    viewModel.removeSelected(contactsEntity);
                    manageActionModeOnUncheckedTransition();
                }
                setViewBackground(holder.view, viewModel.selectedContains(contactsEntity), false);
                setDetailImage(holder, contactsEntity);
            });
        }

        /*
         * Start Action Mode if it has not been started
         * This is only called when long press action results in a selection. Hence action mode may
         * not have been started. Along with starting action mode, title will be set.
         * If this is an additional selection, adjust title appropriately.
         */
        private void manageActionModeOnCheckedTransition() {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) currentActivity).startSupportActionMode(new ContactsSetListActionMode());
            }
            if (viewModel.numberOfSelected() > 1) {
                actionMode.getMenu().findItem(R.id.update_item).setVisible(false);
            }
            actionMode.setTitle(String.valueOf(viewModel.numberOfSelected()));
        }

        /*
         * This is called when one of the selected contactsSet has been de-selected
         * On this event, we will determine if update action needs to be made visible or
         * action mode should be terminated (no more selected)
         */
        private void manageActionModeOnUncheckedTransition() {
            switch (viewModel.numberOfSelected()) {
                case 1:
                    actionMode.getMenu().findItem(R.id.update_item).setVisible(true);
                    break;

                case 0:
                    if (actionMode != null) {
                        actionMode.finish();
                        actionMode = null;
                    }
                    return;

                default:
            }
            actionMode.setTitle(String.valueOf(viewModel.numberOfSelected()));
        }

        private void populateObjectCell(@NonNull ViewHolder viewHolder, @NonNull Contacts contactsEntity) {

            DataValue dataValue = contactsEntity.getDataValue(Contacts.contTyp);
            String masterPropertyValue = null;
            if (dataValue != null) {
                masterPropertyValue = dataValue.toString();
            }
            viewHolder.objectCell.setHeadline(masterPropertyValue);
            viewHolder.objectCell.setDetailImage(null);
            setDetailImage(viewHolder, contactsEntity);

            viewHolder.objectCell.setSubheadline("Subheadline goes here");
            viewHolder.objectCell.setFootnote("Footnote goes here");
            if (masterPropertyValue == null || masterPropertyValue.isEmpty()) {
                viewHolder.objectCell.setIcon("?", 0);
            } else {
                viewHolder.objectCell.setIcon(masterPropertyValue.substring(0, 1), 0);
            }
            viewHolder.objectCell.setIcon(R.drawable.default_dot, 1, R.string.attachment_item_content_desc);
            viewHolder.objectCell.setIcon("!", 2);
        }

        /**
         * Set background of view to indicate contactsEntity selection status
         * Selected and Active are mutually exclusive. Only one can be true
         *
         * @param view
         * @param isContactsSelected - true if contactsEntity is selected via long press action
         * @param isActive               - true if contactsEntity is selected via click action
         */
        private void setViewBackground(@NonNull View view, boolean isContactsSelected, boolean isActive) {
            boolean isMasterDetailView = currentActivity.getResources().getBoolean(R.bool.two_pane);
            if (isContactsSelected) {
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.list_item_selected));
            } else if (isActive && isMasterDetailView && !isInActionMode) {
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.list_item_active));
            } else {
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.list_item_default));
            }
        }

        @Override
        public int getItemCount() {
            if (contactsSet == null) {
                return 0;
            } else {
                return contactsSet.size();
            }
        }

        /**
         * Computes a stable ID for each Contacts object for use to locate the ViewHolder
         *
         * @param contactsEntity
         * @return an ID based on the primary key of Contacts
         */
        private long getItemIdForContacts(Contacts contactsEntity) {
            return contactsEntity.getEntityKey().toString().hashCode();
        }

        /**
         * ViewHolder for RecyclerView.
         * Each view has a Fiori ObjectCell and a checkbox (used by long press)
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;

            public boolean isSelected;

            public String masterPropertyValue;

            /** Fiori ObjectCell to display contactsEntity in list */
            public final ObjectCell objectCell;

            /** Checkbox for long press selection */
            public final CheckBox checkBox;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                objectCell = view.findViewById(R.id.content);
                checkBox = view.findViewById(R.id.cbx);
                isSelected = false;
            }

            public void setSelected(Boolean selected) {
                isSelected = selected;
                checkBox.setChecked(selected);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + objectCell.getDescription() + "'";
            }
        }
    }
}
