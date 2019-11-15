package com.sap.support.app;

import android.app.Application;
import com.sap.cloud.mobile.foundation.common.EncryptionError;
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException;
import com.sap.cloud.mobile.foundation.usage.AppUsage;
import com.sap.cloud.mobile.foundation.usage.UsageBroker;

import java.net.MalformedURLException;
import java.util.UUID;

import static com.sap.support.app.SAPWizardApplication.APPLICATION_VERSION;

/**
 * Simple wrapper class for {@link UsageBroker}. It's handling UsageBroker lifecycle and user
 * consent regarding analytics/usage data capture.
 */
public class UsageUtil {

    /**
     * Application context reference for using with Usage.
     */
    private SAPWizardApplication application;

    /**
     * Flag if user accepted analytics capture. Default value is false.
     */
    private boolean acceptedUsage = false;
    /**
     * Flag for signalling that usageBroker has already been started or not.
     */
    private boolean usageBrokerStarted = false;

    /**
     * Flag to tell whether to register usage lifecycle callback or not.
     */
    private boolean usageBrokerInitialized = false;

    /**
     * Creates a new UsageUtil bound to the current application context.
     *
     * @param app Application context used to access usage
     */
    public UsageUtil(SAPWizardApplication app) {
        application = app;
    }

    /**
     * Initializes {@link UsageBroker} required for usage data collection.
     *
     * @throws OpenFailureException if the usage store cannot be initialized.
     * @throws EncryptionError      if the usage store encryption cannot be initialized.
     */
    public void initUsage() throws OpenFailureException, EncryptionError, MalformedURLException {
        if (!UsageBroker.isStarted()) {
            UsageBroker.start(application, application.getApplicationContext(), APPLICATION_VERSION, !usageBrokerInitialized);
            usageBrokerInitialized = true;
            UsageBroker.configure(application.getApplicationContext(), UUID.fromString("49965081-9090-4b10-9711-06c06cd87b0f"));
        }
        usageBrokerStarted = true;
    }

    /**
     * Tells if the user accepted to collect usage data by the application.
     *
     * @return true if usage data collection was granted by the user false otherwise.
     */
    public boolean isAcceptedUsage() {
        return acceptedUsage;
    }

    /**
     * Set if the user accepted to collect usage data by the application.
     *
     * @param acceptedUsage true if user accepted to collect usage data.
     */
    public void setAcceptedUsage(boolean acceptedUsage) {
        this.acceptedUsage = acceptedUsage;
        if (!acceptedUsage) {
            deleteStore(application);
        }
    }

    /**
     * Submit a ViewDisplayed analytics behavior event.
     *
     * @param viewId           Screen/View name
     * @param elementId        interacted UI Element or on screen control
     * @param action           user performed action
     * @param interactionValue value related to the interaction, if applicable
     */
    public void eventBehaviorViewDisplayed(String viewId, String elementId,
                                           String action, String interactionValue) {
        if (acceptedUsage && usageBrokerStarted) {
            AppUsage.eventBehaviorViewDisplayed(viewId, elementId, action, interactionValue);
        }
    }

    /**
     * Submit a UserInteraction analytics behavior event. For example, a Button-click or row select.
     *
     * @param viewId           Screen/View name
     * @param elementId        interacted UI Element or on screen control
     * @param action           user performed action. Ex "Row Selected"
     * @param interactionValue value related to the interaction, if applicable.
     */
    public void eventBehaviorUserInteraction(String viewId, String elementId,
                                             String action, String interactionValue) {
        if (acceptedUsage && usageBrokerStarted) {
            AppUsage.eventBehaviorUserInteraction(viewId, elementId, action, interactionValue);
        }
    }

    /**
     * Delete the event storage container
     */
    public void deleteStore(Application application) {
        if(usageBrokerStarted) {
            UsageBroker.deleteStore(application.getApplicationContext());
            usageBrokerStarted = false;
        }
    }

}
