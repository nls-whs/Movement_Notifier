package com.vuzix.nextlevelsports.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import java.lang.reflect.Field;


/**
 * Ermöglicht die Auswahl der Größe und speichert diese in den SharedPreferences ab.
 */
public class HeightActivity extends ActionMenuActivity {

    private int height;
    private NumberPicker npHeight;
    private static Context context;

    public static final String SHARED_PREFS = "sharedPrefs";

    /**
     * Erstellt die Oberfläche der HeightActivity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLightMode = isLightMode(getContext());

        if(isLightMode) {
            setContentView(R.layout.activity_height_light);
        } else {
            setContentView(R.layout.activity_height);
        }

        HeightActivity.context = getApplicationContext();

        loadHeight();

        npHeight = findViewById(R.id.numberPickerHeight);

        npHeight.setMinValue(50);
        npHeight.setMaxValue(250);
        npHeight.setWrapSelectorWheel(false);
        if (height > 0) {
            npHeight.setValue(height);
        }

        setDividerColor(npHeight);
    }

    /**
     * Erstellt das Action Menu.
     * @param menu Menu
     * @return Menu
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_disabled_back, menu);

        MenuItem backButton = menu.getItem(0).setVisible(false);

        return true;
    }

    /**
     * Stellt die Position des Action Menus ein.
     * @return BOTTOM.
     */
    @Override
    protected int getActionMenuGravity() { return Gravity.BOTTOM; }

    /**
     * Stellt ein, ob das ActionMenu immer dargestellt werden soll.
     * @return True.
     */
    @Override
    protected boolean alwaysShowActionMenu() { return true; }

    private static Context getAppContext() {
        return HeightActivity.context;
    }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context
     * @return
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    /**
     * Passt die Farben der Divider an, je nachdem welches Layout aktiv ist.
     * @param picker NumberPicker.
     */
    private static void setDividerColor(NumberPicker picker) {
        if (picker == null)
            return;

        final int count = picker.getChildCount();

        if(isLightMode(getAppContext())) {
            for (int i = 0; i < count; i++) {
                try {
                    Field dividerField = picker.getClass().getDeclaredField("mSelectionDivider");
                    dividerField.setAccessible(true);
                    ColorDrawable colorDrawable
                            = new ColorDrawable(picker.getResources().getColor(R.color.black));
                    colorDrawable.setAlpha(90);
                    dividerField.set(picker, colorDrawable);
                    picker.invalidate();
                } catch (Exception e) {
                    Log.w("setDividerColor", e);
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                try {
                    Field dividerField = picker.getClass().getDeclaredField("mSelectionDivider");
                    dividerField.setAccessible(true);
                    ColorDrawable colorDrawable
                            = new ColorDrawable(picker.getResources().getColor(R.color.vuzix_green));
                    colorDrawable.setAlpha(90);
                    dividerField.set(picker, colorDrawable);
                    picker.invalidate();
                } catch (Exception e) {
                    Log.w("setDividerColor", e);
                }
            }
        }

    }

    /**
     * Befasst sich mit den Eingaben des Touch-Bedienfelds.
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            saveHeight();
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Speichert die Körpergröße in den SharedPreferences ab.
     */
    public void saveHeight() {
        SharedPreferences sharedPreferences
                = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        height = npHeight.getValue();

        editor.putInt("height", height);

        editor.apply();

        this.finish();
    }

    /**
     * Lädt die Körpergröße aus den SharedPreferences.
     */
    public void loadHeight() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        height = sharedPreferences.getInt("height", 180);
    }

}
