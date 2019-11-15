package com.sap.support.app;

import android.content.Context;
import android.content.Intent;

import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

/**
 * {@link ErrorPresenter} implementation which is based on notification dialogs. If the error were
 * marked as fatal, then the application is shut down after pressing the 'OK' button.
 */
public class ErrorPresenterByNotification implements ErrorPresenter {

    private static Queue<Intent> notificationIntentQueue = new LinkedList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorPresenterByNotification.class);
    private static boolean isErrorDialogShowing = false;
    private Context context;

    public ErrorPresenterByNotification(Context currentContext) {
        context = currentContext.getApplicationContext();
    }

    @Override
    public void presentError(String errorTitle, String errorDetail, Exception exception, boolean isFatal) {

        Intent startNotification = new Intent(context, ErrorNotificationDialog.class);
        startNotification.putExtra(ErrorNotificationDialog.TITLE, errorTitle);
        startNotification.putExtra(ErrorNotificationDialog.MSG, errorDetail);
        String logString = errorTitle + ": " + errorDetail;
        if (isFatal) {
            logString = "Fatal - " + logString;
        }
        LOGGER.error(logString, exception);
        startNotification.putExtra(ErrorNotificationDialog.FATAL, isFatal);
        startNotification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        synchronized (notificationIntentQueue) {
            if (!isErrorDialogShowing) {
                context.startActivity(startNotification);
                isErrorDialogShowing = true;
            } else {
                notificationIntentQueue.add(startNotification);
            }
        }
    }

    public static void errorDialogDismissed() {
        synchronized (notificationIntentQueue) {
            Intent nextIntent = notificationIntentQueue.poll();
            if (nextIntent != null) {
                AppLifecycleCallbackHandler.getInstance().getActivity().startActivity(nextIntent);
            } else {
                isErrorDialogShowing = false;
            }
        }
    }
}
