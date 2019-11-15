package com.sap.support.app;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.sap.support.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetApplicationActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetApplicationActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ResetApplicationDialogFragment dialog = new ResetApplicationDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "RESET_APPLICATION_TAG");
    }

    /**
     * Represents the reset application confirmation dialog
     */
    public static class ResetApplicationDialogFragment extends DialogFragment {
        public ResetApplicationDialogFragment() {
            super();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Activity activity = getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogStyle));
            builder.setMessage(R.string.reset_app_confirmation)
                    // Setting OK Button
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        // reset the application
                        ((SAPWizardApplication)activity.getApplication()).resetApp(activity);
                        LOGGER.info("Yes button is clicked. The all information related to this application will be deleted.");
                        activity.finish();
                    })
                    // Setting Cancel Button
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        LOGGER.info("The Cancel button is clicked.");
                        activity.finish();
                    });
            return builder.create();
        }
    }
}
