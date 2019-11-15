package com.sap.support.test.testcases.ui;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.sap.support.logon.LogonActivity;
import com.sap.support.test.core.BaseTest;
import com.sap.support.test.core.Constants;
import com.sap.support.test.core.UIElements;
import com.sap.support.test.core.Utils;
import com.sap.support.test.pages.DetailPage;
import com.sap.support.test.pages.EntityListPage;
import com.sap.support.test.pages.MasterPage;
import com.sap.support.test.pages.SettingsListPage;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.sap.support.test.core.UIElements.EntityListScreen.entityList;

@RunWith(AndroidJUnit4.class)
public class ConfigurationProviderTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);

    @Test
    public void testConfigurationProviderBasicFlow() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        // Actions on the entitylist Page
        EntityListPage entityListPage = new EntityListPage(entityList);
        entityListPage.clickFirstElement();
        entityListPage.leavePage();

        // Actions on the master Page
        MasterPage masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickFirstElement();
        masterPage.leavePage();

        DetailPage detailPage = new DetailPage();
        detailPage.clickBack();

        masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickBack();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }

    @Before
    public void discoverySetUp() {
        Constants.ONBOARDING_TYPE = Constants.OnboardingType.DISCOVERY_SERVICE;
    }

    @After
    public void discoveryTearDown() {
        Constants.ONBOARDING_TYPE = Constants.OnboardingType.STANDARD;
    }
}
