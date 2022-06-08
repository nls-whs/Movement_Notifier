package com.vuzix.nextlevelsports.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.hud.actionmenu.DefaultActionMenuItemView;
import com.vuzix.nextlevelsports.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * Initialisiert die Kamera und führt die Bildklassifizierung durch. Wird ein passendes Bild
 * erkannt, wird das Ergebnis an die ExerciseActivity übergeben.
 */
public class CameraActivity extends ActionMenuActivity implements LifecycleOwner {
    private LifecycleRegistry mLifecycleRegistry;

    public static final String SHARED_PREFS = "sharedPrefs";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;

    private final LifecycleRegistry registry = new LifecycleRegistry(this);

    private ImageLabelerOptions options;

    private ImageAnalysis imageAnalysis;

    private boolean scanState = false;

    private Executor executor;
    private ImageLabeler labeler;
    private Handler handler;

    private SwitchMenuItemView switchMenuItemView;
    private SwitchMenuItemView2 switchMenuItemView2;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.viewFinder);
        executor = ContextCompat.getMainExecutor(this);

        /*
         * Überprüft, ob alle notwendigen Zulassungen gewährt wurden
         */
        if(allPermissionsGranted()){
            startCamera();
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }

        options = new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.8f)
                        .build();

        labeler = ImageLabeling.getClient(options);

        /*
         * Falls die App das erste mal gestartet wurde, erhält der Benutzer eine kurze Einführung
         * zum Verständnis.
         */
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean previouslyStarted = sharedPreferences.getBoolean("previously_started", false);
        if(!previouslyStarted) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("previously_started", Boolean.TRUE);
            editor.apply();

            new AlertDialog.Builder(CameraActivity.this)
                    .setTitle("Umgebung ermitteln")
                    .setMessage("Starte die Umgebungserkennung und schaue dich in deinem Umfeld " +
                            "um, damit dir eine dazu passende Übung vorgeschlagen werden kann.")

                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
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

        getMenuInflater().inflate(R.menu.menu_camera, menu);

        MenuItem previewMenuItem = menu.findItem(R.id.preview);
        previewMenuItem.setActionView(switchMenuItemView = new SwitchMenuItemView(this));

        MenuItem scanMenuItem = menu.findItem(R.id.scan);
        scanMenuItem.setActionView(switchMenuItemView2 = new SwitchMenuItemView2(this));

        updateMenuItems();

        return true;
    }

    /**
     * Befasst sich mit den Aktionen bei Auswahl der einzelnen Menüelemente.
     * @param item Menüelement
     * @return
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected boolean onActionItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.scan) {
            handler = new Handler();

            // Bildanalyse wird gestartet
            if(!scanState) {
                Toast.makeText(this, "Umgebung wird ermittelt ...",
                        Toast.LENGTH_SHORT).show();
                switchMenuItemView2.setSwitchState(false);
                scanState = true;

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                imageAnalysis.setAnalyzer(executor,
                        new ImageAnalysis.Analyzer() {
                            @Override
                            public void analyze(@NonNull ImageProxy imageProxy) {
                                @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage =
                                        imageProxy.getImage();
                                if (mediaImage != null) {
                                    processImage(mediaImage, imageProxy)
                                            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                                @Override
                                                public void onSuccess(List<ImageLabel> labels) {
                                                    imageProxy.close();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    imageProxy.close();
                                                }
                                            });
                                }
                            }
                        });

                handler.postDelayed(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Umgebung wurde nicht erkannt. " +
                                "Versuche es erneut.",Toast.LENGTH_SHORT).show();
                        switchMenuItemView2.setSwitchState(true);
                        scanState = false;
                        imageAnalysis.clearAnalyzer();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }, 15 * 1000);

            } else {
                switchMenuItemView2.setSwitchState(true);
                scanState = false;
                imageAnalysis.clearAnalyzer();
                handler.removeCallbacksAndMessages(null);
                handler.removeMessages(0);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

        } else if (id == R.id.preview) {
            if (previewView.getAlpha() == 0) {
                previewView.setAlpha(1);
                switchMenuItemView.setSwitchState(true);
            } else if (previewView.getAlpha() != 0) {
                previewView.setAlpha(0);
                switchMenuItemView.setSwitchState(false);
            }
        }
        return super.onActionItemSelected(item);
    }

    /**
     * Wählt das Menü-Element mit dem Index 1 als Start-Element.
     * @return 1
     */
    @Override
    protected int getDefaultAction() {
        return 1;
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
     * Methode, um die Kamera zu starten
     */
    public void startCamera(){
        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Initialisiert den CameraProvider
                ProcessCameraProvider cameraProvider
                        = (ProcessCameraProvider) cameraProviderFuture.get();
                // Vorschau der Kamera
                Preview preview = new Preview.Builder().build();
                // Wählt die Kamera aus
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Bildanalyse
                imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Verbindet die Vorschau der Kamera mit der PreviewView
                preview.setSurfaceProvider(previewView.createSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview,
                        imageAnalysis);

            } catch (InterruptedException | ExecutionException e) {

            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Analysiert die übergebenen Bilder.
     * @param mediaImage MediaImage
     * @param imageProxy ImageProxy
     * @return
     */
    private Task<List<ImageLabel>> processImage(Image mediaImage, ImageProxy imageProxy) {
        InputImage image =
                InputImage.fromMediaImage(mediaImage,
                        imageProxy.getImageInfo().getRotationDegrees());

        // Ausgewählte Alltagssituationen des Models
        Resources res = getResources();
        String[] dailySituations = res.getStringArray(R.array.daily_situations);
        Set<String> dailySituationsSet = new HashSet<String>(Arrays.asList(
                dailySituations
        ));

        // Übersetzungen der ausgewählten Alltagssituationen des Models
        String[] dailySituationsTranslations = res.getStringArray(R.array.daily_situations_translations);
        Map<String, String> translationsMap = new HashMap<String, String>();
        int i = 0;
        while (i < dailySituations.length && i < dailySituationsTranslations.length) {
            translationsMap.put(dailySituations[i], dailySituationsTranslations[i]);
            i++;
        }

        // Analysiert das übergebene Bild
        return labeler.process(image)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String error = "Failed to process. Error: " + e.getMessage();
                        // Log.e(TAG, error);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        for (ImageLabel label : labels) {

                            // Wenn Liste ein weiteres Element als "Zimmer" enthält
                            if (label.getText().equalsIgnoreCase("Room") &&
                                    labels.size() > 1) {
                                continue;
                            }

                            /*
                             * Wenn Liste der Alltagssituationen das gefundene Label enthält,
                             * wird die ExerciseActivity gestartet.
                             */
                            if(dailySituationsSet.contains(label.getText())) {
                                Intent intent = new Intent(CameraActivity.this,
                                        ExerciseActivity.class);
                                String situation = "exercises_" + label.getText();
                                intent.putExtra("situation", situation);
                                CameraActivity.this.startActivity(intent);

                                /*
                                 * Vibriert, falls Übereinstimmung mit einer der ausgewählten
                                 * Alltagssituationen
                                 */
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    v.vibrate(VibrationEffect.createOneShot(400,
                                            VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    v.vibrate(400);
                                }

                                handler.removeCallbacksAndMessages(null);
                                switchMenuItemView2.setSwitchState(true);
                                scanState = false;
                                imageAnalysis.clearAnalyzer();
                                getWindow().clearFlags(WindowManager.LayoutParams
                                        .FLAG_KEEP_SCREEN_ON);

                                Toast.makeText(CameraActivity.this, "\""
                                        + translationsMap.get(label.getText()) + "\""
                                        + " erkannt.", Toast.LENGTH_SHORT).show();

                                break;
                            }
                        }
                    }
                });
    }

    /**
     * Startet Kamera, falls die nötigen Befugnisse erteilt wurden. Falls nicht, wird die Activity
     * beendet
     * @param requestCode Code der angefragten Zulassung
     * @param permissions Zulassungen
     * @param grantResults Ergebnis, ob zugelassen oder nicht
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Zulassungen nicht gewährt.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Überprüft, ob alle nötigen Befugnisse erteilt wurden.
     * @return True / False
     */
    private boolean allPermissionsGranted(){
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    /**
     * Befasst sich mit den Aktionen bei betätigen des Zurück-Buttons.
     */
    @Override
    public void onBackPressed() {
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (imageAnalysis != null) {
            imageAnalysis.clearAnalyzer();
        }
        switchMenuItemView2.setSwitchState(true);
        scanState = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        finish();
    }

    /**
     * Updatet die Menü-Items.
     */
    private void updateMenuItems() {
        switchMenuItemView.setSwitchState(true);
        switchMenuItemView2.setSwitchState(true);
    }

    /**
     * LifeCycle Registrierung
     */
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

    /**
     * Erlaubt es, das Menüelement der Preview live zu verändern.
     */
    private static class SwitchMenuItemView extends DefaultActionMenuItemView {

        public SwitchMenuItemView(Context context) {
            super(context);
        }

        private void setSwitchState(boolean on) {
            if (on) {
                setIcon(getResources().getDrawable(R.drawable.ic_baseline_remove_red_eye_24,
                        getContext().getTheme()));
                setTitle("Preview");
            } else {
                setIcon(getResources().getDrawable(R.drawable.ic_eye_off,getContext().getTheme()));
                setTitle("Preview");
            }
        }
    }

    /**
     * Erlaubt es, das Menüelement zum Scannen live zu verändern.
     */
    private static class SwitchMenuItemView2 extends DefaultActionMenuItemView {

        public SwitchMenuItemView2(Context context) {
            super(context);
        }

        private void setSwitchState(boolean on) {
            if (on) {
                setIcon(getResources().getDrawable(R.drawable.ic_play_circle_outline,
                        getContext().getTheme()));
                setTitle("Umgebung ermitteln");
            } else {
                setIcon(getResources().getDrawable(R.drawable.ic_pause_circle_outline,
                        getContext().getTheme()));
                setTitle("Umgebung ermitteln");
            }
        }
    }

}
