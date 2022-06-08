package com.vuzix.nextlevelsports.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.hud.actionmenu.DefaultActionMenuItemView;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import java.text.DecimalFormat;

/**
 * Einstellungsmöglichkeiten werden dargestellt.
 */
public class SettingsActivity extends ActionMenuActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    private static final String GENDER_PREF = "gender";
    private static final String AGE_PREF = "age";
    private static final String HEIGHT_PREF = "height";
    private static final String WEIGHT_PREF = "weight";
    private static final String NOTIFICATION_PREF = "notificationsEnabled";

    private boolean statusState = true;
    private boolean statusStateLocation = true;

    private MenuItem locationMenuItem;
    private MenuItem notificationsMenuItem;

    private TextView mainTitle;
    private TextView mainValue;
    private ImageView mainImage;

    /**
     * Erstellt die Oberfläche der SettingsActivity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLightMode = isLightMode(getContext());

        if(isLightMode) {
            setContentView(R.layout.activity_settings_light);
        } else {
            setContentView(R.layout.activity_settings);
        }

        mainTitle = findViewById(R.id.settingsTV);
        mainValue = findViewById(R.id.main_value);
        mainImage = findViewById(R.id.icon);

        loadData();
        updateMenuItems();
    }

    /**
     * Erstellt das Menü der SettingsActivity.
     * @param menu Menü
     * @return true
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        super.onCreateActionMenu(menu);
        getMenuInflater().inflate(R.menu.menu_settings, menu);

        updateMenuItems();

        return true;
    }

    /**
     * Fokussiert das Element mit dem übergebenen Index bei Start der Activity.
     * @return Index
     */
    @Override
    protected int getDefaultAction() {
        return 1;
    }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context
     * @return
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
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
     * Zentrierte Darstellung des Menüs durch Überschreiben der Methode.
     * @return CENTER
     */
    @Override
    protected int getActionMenuGravity() {
        return Gravity.CENTER;
    }

    /**
     * Erneuert bei Neustart der Aktivität die Oberfläche.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    /**
     * Updatet die Menüelemente.
     */
    private void updateMenuItems() {
        if (locationMenuItem == null) {
            return;
        }

    }


    /**
     * Aktionen bei fokussiertem Element.
     * @param item Menüelement, das fokussiert ist.
     */
    @Override
    protected void onActionItemFocused(MenuItem item) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        int id = item.getItemId();

        if (id == R.id.personal) {
            mainValue.setText("");
        } else if (id == R.id.stepGoal) {
            DecimalFormat nf = new DecimalFormat();
            int stepGoal = sharedPreferences.getInt("stepGoal", 10000);
            mainValue.setText(nf.format(stepGoal).replace(",", ".") + " Schritte / Tag");
        } else if (id == R.id.reset) {
            mainValue.setText("");
        }

    }

    /**
     * Aktionen bei ausgewähltem Element.
     * @param item Element
     * @return Action.
     */
    @Override
    protected boolean onActionItemSelected(MenuItem item) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        int id = item.getItemId();

        if(id == R.id.personal) {
            startActivity(new Intent(this, PersonalSettingsActivity.class));
        } else if (id == R.id.stepGoal) {
            startActivity(new Intent(this, StepGoalActivity.class));
        } else if (id == R.id.reset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setTitle("App-Daten löschen?");
            builder.setMessage("Alle Daten dieser App werden endgültig gelöscht. Dazu zählen Statistiken, Einstellungen, etc.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getApplicationContext().getSharedPreferences("sharedPrefs", 0).edit()
                            .clear().apply();
                    // Statisitiken löschen !!!
                    recreate();
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        return super.onActionItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
