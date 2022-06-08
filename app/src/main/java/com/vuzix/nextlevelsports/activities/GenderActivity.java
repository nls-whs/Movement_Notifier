package com.vuzix.nextlevelsports.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

/**
 * Ermöglicht die Auswahl des Geschlechts und speichert das ausgewählte Geschlecht in den
 * SharedPreferences ab.
 */
public class GenderActivity extends ActionMenuActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    private String gender;

    /**
     * Erstellt die Oberfläche der GenderActivity.
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLightMode = isLightMode(getContext());
        if (isLightMode) {
            setContentView(R.layout.activity_gender_light);
        } else {
            setContentView(R.layout.activity_gender);
        }

    }

    /**
     * Erstellt das ActionMenu.
     * @param menu Menu
     * @return Menu
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        super.onCreateActionMenu(menu);
        getMenuInflater().inflate(R.menu.menu_gender, menu);

        MenuItem backButton = menu.getItem(0).setVisible(false);

        return true;
    }

    /**
     * Befasst sich mit den Eingaben der Menü-Elemente.
     * @param item Item
     * @return Action
     */
    @Override
    protected boolean onActionItemSelected(MenuItem item) {

        SharedPreferences sharedPreferences
                = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int id = item.getItemId();

        if (id == R.id.male) {
            editor.putString("gender", "Männlich");
        } else if (id == R.id.female) {
            editor.putString("gender", "Weiblich");
        }

        editor.apply();
        this.finish();

        return super.onActionItemSelected(item);
    }

    /**
     * Fokussiert, je nachdem welches Geschlecht in den SharedPreferences gespeichert ist, das
     * dementsprechende Menü-Element.
     * @return Index
     */
    @Override
    protected int getDefaultAction() {
        loadSavedGender();

        if (gender != null) {
            if (gender.equals("Männlich")) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 0;
        }

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
     * Menü immer sichtbar durch Überschreiben der Methode.
     * @return true
     */
    @Override
    protected boolean alwaysShowActionMenu() {
        return true;
    }

    /**
     * Bottom Darstellung des Menüs durch Überschreiben der Methode.
     * @return CENTER
     */
    @Override
    protected int getActionMenuGravity() {
        return Gravity.CENTER;
    }

    /**
     * Lädt das gespeicherte Geschlecht aus den SharedPreferences.
     */
    public void loadSavedGender() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        gender = sharedPreferences.getString("gender", "Männlich");
    }

}
