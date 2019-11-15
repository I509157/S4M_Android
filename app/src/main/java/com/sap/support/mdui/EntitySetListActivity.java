package com.sap.support.mdui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sap.support.app.SAPWizardApplication;
import com.sap.support.app.UsageUtil;
import com.sap.support.mdui.attachmentset.AttachmentSetActivity;
import com.sap.support.mdui.configurationdataset.ConfigurationDataSetActivity;
import com.sap.support.mdui.contactsset.ContactsSetActivity;
import com.sap.support.mdui.custprodinstset.CustProdInstSetActivity;
import com.sap.support.mdui.expertareaset.ExpertAreaSetActivity;
import com.sap.support.mdui.incidentcreateset.IncidentCreateSetActivity;
import com.sap.support.mdui.incidentinfoset.IncidentInfoSetActivity;
import com.sap.support.mdui.loginuserdataset.LoginUserDataSetActivity;
import com.sap.support.mdui.messagecoreset.MessageCoreSetActivity;
import com.sap.support.mdui.notesadvisorset.NotesAdvisorSetActivity;
import com.sap.support.mdui.systemsearchset.SystemSearchSetActivity;
import com.sap.cloud.mobile.fiori.object.ObjectCell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.support.R;


/*
 * An activity to display the list of all entity types from the OData service
 */
public class EntitySetListActivity extends AppCompatActivity {

    private static final int SETTINGS_SCREEN_ITEM = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySetListActivity.class);
    private static final int BLUE_ANDROID_ICON = R.drawable.ic_android_blue;
    private static final int WHITE_ANDROID_ICON = R.drawable.ic_android_white;

    public enum EntitySetName {
        AttachmentSet("AttachmentSet", R.string.eset_attachmentset,BLUE_ANDROID_ICON),
        ConfigurationDataSet("ConfigurationDataSet", R.string.eset_configurationdataset,WHITE_ANDROID_ICON),
        ContactsSet("ContactsSet", R.string.eset_contactsset,BLUE_ANDROID_ICON),
        CustProdInstSet("CustProdInstSet", R.string.eset_custprodinstset,WHITE_ANDROID_ICON),
        ExpertAreaSet("ExpertAreaSet", R.string.eset_expertareaset,BLUE_ANDROID_ICON),
        IncidentCreateSet("IncidentCreateSet", R.string.eset_incidentcreateset,WHITE_ANDROID_ICON),
        IncidentInfoSet("IncidentInfoSet", R.string.eset_incidentinfoset,BLUE_ANDROID_ICON),
        LoginUserDataSet("LoginUserDataSet", R.string.eset_loginuserdataset,WHITE_ANDROID_ICON),
        MessageCoreSet("MessageCoreSet", R.string.eset_messagecoreset,BLUE_ANDROID_ICON),
        NotesAdvisorSet("NotesAdvisorSet", R.string.eset_notesadvisorset,WHITE_ANDROID_ICON),
        SystemSearchSet("SystemSearchSet", R.string.eset_systemsearchset,BLUE_ANDROID_ICON);

        private int titleId;
        private int iconId;
        private String entitySetName;

        EntitySetName(String name, int titleId, int iconId) {
            this.entitySetName = name;
            this.titleId = titleId;
            this.iconId = iconId;
        }

        public int getTitleId() {
                return this.titleId;
        }

        public String getEntitySetName() {
                return this.entitySetName;
        }
    }

    private final List<String> entitySetNames = new ArrayList<>();
    private final Map<String, EntitySetName> entitySetNameMap = new HashMap<>();
    private UsageUtil usageUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entity_set_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        usageUtil = ((SAPWizardApplication) getApplication()).getUsageUtil();
        usageUtil.eventBehaviorViewDisplayed(EntitySetListActivity.class.getSimpleName(),
                "elementId", "onCreate", "called");

        entitySetNames.clear();
        entitySetNameMap.clear();
        for (EntitySetName entitySet : EntitySetName.values()) {
            String entitySetTitle = getResources().getString(entitySet.getTitleId());
            entitySetNames.add(entitySetTitle);
            entitySetNameMap.put(entitySetTitle, entitySet);
        }

        final ListView listView = findViewById(R.id.entity_list);
        final EntitySetListAdapter adapter = new EntitySetListAdapter(this, R.layout.element_entity_set_list, entitySetNames);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            EntitySetName entitySetName = entitySetNameMap.get(adapter.getItem(position));
            usageUtil.eventBehaviorUserInteraction(EntitySetListActivity.class.getSimpleName(),
                    "position: " + position, "onClicked", entitySetName.getEntitySetName());
            Context context = EntitySetListActivity.this;
            Intent intent;
            switch (entitySetName) {
                case AttachmentSet:
                    intent = new Intent(context, AttachmentSetActivity.class);
                    break;
                case ConfigurationDataSet:
                    intent = new Intent(context, ConfigurationDataSetActivity.class);
                    break;
                case ContactsSet:
                    intent = new Intent(context, ContactsSetActivity.class);
                    break;
                case CustProdInstSet:
                    intent = new Intent(context, CustProdInstSetActivity.class);
                    break;
                case ExpertAreaSet:
                    intent = new Intent(context, ExpertAreaSetActivity.class);
                    break;
                case IncidentCreateSet:
                    intent = new Intent(context, IncidentCreateSetActivity.class);
                    break;
                case IncidentInfoSet:
                    intent = new Intent(context, IncidentInfoSetActivity.class);
                    break;
                case LoginUserDataSet:
                    intent = new Intent(context, LoginUserDataSetActivity.class);
                    break;
                case MessageCoreSet:
                    intent = new Intent(context, MessageCoreSetActivity.class);
                    break;
                case NotesAdvisorSet:
                    intent = new Intent(context, NotesAdvisorSetActivity.class);
                    break;
                case SystemSearchSet:
                    intent = new Intent(context, SystemSearchSetActivity.class);
                    break;
                    default:
                        return;
            }
            context.startActivity(intent);
        });
            
    }

    public class EntitySetListAdapter extends ArrayAdapter<String> {

        EntitySetListAdapter(@NonNull Context context, int resource, List<String> entitySetNames) {
            super(context, resource, entitySetNames);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            EntitySetName entitySetName = entitySetNameMap.get(getItem(position));
            if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.element_entity_set_list, parent, false);
            }
            String headLineName = getResources().getString(entitySetName.titleId);
            ObjectCell entitySetCell = convertView.findViewById(R.id.entity_set_name);
            entitySetCell.setHeadline(headLineName);
            entitySetCell.setDetailImage(entitySetName.iconId);
            return convertView;
        }
    }
                
    @Override
    public void onBackPressed() {
            moveTaskToBack(true);
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SETTINGS_SCREEN_ITEM, 0, R.string.menu_item_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOGGER.debug("onOptionsItemSelected: " + item.getTitle());
        switch (item.getItemId()) {
            case SETTINGS_SCREEN_ITEM:
                LOGGER.debug("settings screen menu item selected.");
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivityForResult(intent, SETTINGS_SCREEN_ITEM);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOGGER.debug("EntitySetListActivity::onActivityResult, request code: " + requestCode + " result code: " + resultCode);
        if (requestCode == SETTINGS_SCREEN_ITEM) {
            LOGGER.debug("Calling AppState to retrieve settings after settings screen is closed.");
        }
    }

}
