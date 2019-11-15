package com.sap.support.test.pages;

import com.pgssoft.espressodoppio.idlingresources.ViewIdlingResource;
import com.sap.support.test.core.UIElements;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class UserConsentPage {

    // Default constructor
    public UserConsentPage() {
        ViewIdlingResource viewIdlingResource = (ViewIdlingResource) new ViewIdlingResource(
                withId(UIElements.UserConsentPage.usageDenyButton)).register();
    }

    public void clickDeny() {
        // Close the soft keyboard first, since it might be covering the button.
        onView(withId(UIElements.UserConsentPage.usageDenyButton)).perform(closeSoftKeyboard(), click());
    }

    public void clickAllow() {
        // Close the soft keyboard first, since it might be covering the button.
        onView(withId(UIElements.UserConsentPage.usageAllowButton)).perform(closeSoftKeyboard(), click());
    }
}
