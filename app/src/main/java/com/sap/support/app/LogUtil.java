package com.sap.support.app;

import android.content.res.Resources;
import com.sap.support.R;
import java.util.Arrays;
import ch.qos.logback.classic.Level;

/**
 * Utility class to handle log level and string translation.
 */
public class LogUtil {

    /**
     * Array holding log level values.
     */
    private String[] levelValues;

    /**
     * Array holding log level translated strings.
     */
    private String[] levelStrings;

    /**
     * Creates a new LogUtil bound to the current application context.
     *
     * @param application application context
     */
    public LogUtil(SAPWizardApplication application) {
        Resources res = application.getResources();

        levelValues = new String[]{
                String.valueOf(Level.ALL.levelInt),
                String.valueOf(Level.DEBUG.levelInt),
                String.valueOf(Level.INFO.levelInt),
                String.valueOf(Level.WARN.levelInt),
                String.valueOf(Level.ERROR.levelInt),
                String.valueOf(Level.OFF.levelInt)};

        levelStrings = new String[]{
                res.getString(R.string.log_level_path),
                res.getString(R.string.log_level_debug),
                res.getString(R.string.log_level_info),
                res.getString(R.string.log_level_warning),
                res.getString(R.string.log_level_error),
                res.getString(R.string.log_level_none)};
    }

    /**
     * Get log level values.
     *
     * @return log level values
     */
    public String[] getLevelValues() {
        return levelValues;
    }

    /**
     * Get log level translated strings.
     *
     * @return translated log level strings
     */
    public String[] getLevelStrings() {
        return levelStrings;
    }

    /**
     * Get translated string for log level object
     *
     * @param level log level object
     * @return translated string for log level
     */
    public String getLevelString(Level level) {
        int index = Arrays.asList(levelValues).indexOf(String.valueOf(level.levelInt));
        return levelStrings[index];
    }

    /**
     * Get translated string for log level value
     *
     * @param levelValue log level value
     * @return translated string for log level value
     */
    public String getLevelString(String levelValue) {
        int index = Arrays.asList(levelValues).indexOf(levelValue);
        return levelStrings[index];
    }
}
