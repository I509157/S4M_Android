package com.sap.support.logon;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.sap.support.R;
import com.sap.support.app.ErrorHandler;
import com.sap.support.app.ErrorMessage;
import com.sap.support.app.SAPWizardApplication;
import com.sap.cloud.mobile.foundation.common.EncryptionError;
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

/**
 * An example full-screen activity that shows a User consent screen regarding usage data collection.
 */
public class AskUsagePermissionActivity extends AppCompatActivity {
    static Logger LOGGER = LoggerFactory.getLogger(AskUsagePermissionActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_usage_permission);
    }

    /**
     * Called if user clicked on OK to collect usage data.
     *
     * @param view
     */
    public void onAllow(View view) {
        LOGGER.debug("Usage collection allowed.");
        ((SAPWizardApplication) getApplication()).getSecureStoreManager().setUsageAccepted(true);
        ((SAPWizardApplication) getApplication()).getUsageUtil().setAcceptedUsage(true);
        try {
            ((SAPWizardApplication) getApplication()).getUsageUtil().initUsage();
        } catch (OpenFailureException | EncryptionError | MalformedURLException e) {
            String errorCause = e.getLocalizedMessage();
            ErrorMessage errorMessage = new ErrorMessage(getResources().getString(R.string.usage_init_failed), errorCause, new Exception(e), false);
            ErrorHandler errorHandler = ((SAPWizardApplication) getApplication()).getErrorHandler();
            errorHandler.sendErrorMessage(errorMessage);
            LOGGER.error("Usage initialization failed with error message: " + errorCause);
        }
        finish();
    }

    /**
     * Called if user clicked on not to accept to collect usage data.
     *
     * @param view
     */
    public void onDeny(View view) {
        LOGGER.debug("Usage collection denied.");
        ((SAPWizardApplication) getApplication()).getSecureStoreManager().setUsageAccepted(false);
        ((SAPWizardApplication) getApplication()).getUsageUtil().setAcceptedUsage(false);
        finish();
    }
}
