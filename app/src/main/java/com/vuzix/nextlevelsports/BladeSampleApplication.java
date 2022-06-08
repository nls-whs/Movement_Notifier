package com.vuzix.nextlevelsports;

import com.vuzix.hud.resources.DynamicThemeApplication;

/**
 * Passt das Theme je nach Helligkeit an.
 */
public class BladeSampleApplication extends DynamicThemeApplication {

    @Override
    protected int getNormalThemeResId() {
        return R.style.AppTheme;
    }

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme_Light;
    }

}
