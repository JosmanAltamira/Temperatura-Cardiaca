package com.example.temperaturaambiental3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorTemperatura;
    private Sensor sensorCorazon;
    private TextView txtValorTemperatura;
    private TextView txtValorCorazon;
    private TextView txtAlerta;
    private ProgressBar temperaturaProgressBar;

    //icono y animacion
    private ImageView iconoCorazon;
    private Animation corazonAnimacion;
    private boolean AnimacionCorriendo = false;

    private static final int CORAZON_LATIDO_DURACION_MIN = 800;  // Duración mínima de la animación
    //private static final int CORAZON_LATIDO_DURACION_MAX = 100;  // Duración máxima de la animación
    private static final int MAX_CORAZON = 500;// Valor máximo de la frecuencia cardíaca

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtValorTemperatura = findViewById(R.id.txtValorTemperatura);
        txtValorCorazon = findViewById(R.id.txtValorCorazon);
        txtAlerta = findViewById(R.id.txtAlerta);

        iconoCorazon = findViewById(R.id.iconoCorazon);
        corazonAnimacion = AnimationUtils.loadAnimation(this, R.anim.corazon_latido);
        temperaturaProgressBar = findViewById(R.id.temperaturaProgressBar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorTemperatura = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorCorazon = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BODY_SENSORS}, 1);
        } else {
            startSensors();
            startHeartAnimation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensors();
    }

    private void startSensors() {
        sensorManager.registerListener(this, sensorTemperatura, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorCorazon, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopSensors() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //obtener valores de los sensores temperatura y frecuencia cardiaca
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            float valorTemperatura = event.values[0];
            txtValorTemperatura.setText(String.valueOf(valorTemperatura));

            String valorCorazonStr = txtValorCorazon.getText().toString();
            if (!valorCorazonStr.isEmpty()) {
                float valorCorazon = Float.parseFloat(valorCorazonStr);
                Comparacion(valorTemperatura, valorCorazon);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float valorCorazon = event.values[0];
            txtValorCorazon.setText(String.valueOf(valorCorazon));

            String valorTemperaturaStr = txtValorTemperatura.getText().toString();
            if (!valorTemperaturaStr.isEmpty()) {
                float valorTemperatura = Float.parseFloat(valorTemperaturaStr);
                Comparacion(valorTemperatura, valorCorazon);
            }
        }

            if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                float valorCorazon = event.values[0];
                txtValorCorazon.setText(String.valueOf(valorCorazon));
                if (valorCorazon <= 0) {
                    stopHeartAnimation();
                } else {
                    float heartRatePercentage = (valorCorazon / MAX_CORAZON);
                    float speedMultiplier = 1 + (heartRatePercentage * 3);  // Ajusta el multiplicador de velocidad según tu preferencia
                    int heartBeatDuration = (int) (CORAZON_LATIDO_DURACION_MIN / speedMultiplier);
                    setHeartAnimationDuration(heartBeatDuration);
                    startHeartAnimation();
                }
            }
    }

    private void startHeartAnimation() {
        if (!AnimacionCorriendo && corazonAnimacion != null) {
            // Valores de animacion
            corazonAnimacion.setDuration(CORAZON_LATIDO_DURACION_MIN);
            iconoCorazon.startAnimation(corazonAnimacion);
            AnimacionCorriendo = true;
        }
    }

    private void setHeartAnimationDuration(int duration) {
        Animation corazonAnimacion = AnimationUtils.loadAnimation(this, R.anim.corazon_latido);
        corazonAnimacion.setDuration(duration);
        ImageView iconoCorazon = findViewById(R.id.iconoCorazon);
        iconoCorazon.startAnimation(corazonAnimacion);
    }

    private void stopHeartAnimation() {
        if (AnimacionCorriendo) {
            iconoCorazon.clearAnimation();
            AnimacionCorriendo = false;
        }
    }

    private void Comparacion(float valorTemperatura, float valorCorazon) {
        // Cambiar color del icono de corazón
        ImageView iconoCorazon = findViewById(R.id.iconoCorazon);
        if (valorCorazon <= 60) {
            iconoCorazon.setColorFilter(ContextCompat.getColor(this,R.color.azul));
        } else if (valorCorazon >= 100) {
            iconoCorazon.setColorFilter(ContextCompat.getColor(this, R.color.rojo));
        } else if (valorCorazon >= 61 && valorCorazon <= 99) {
            iconoCorazon.setColorFilter(ContextCompat.getColor(this, R.color.verde));
        }else {
            iconoCorazon.clearColorFilter();
        }

        // bara de progreso de temperatura
        int progreso = (int) ((valorTemperatura - (-273.1)) / (100 - (-273.1)) * 100);
        temperaturaProgressBar.setProgress(progreso);

        // Cambiar color de fondo
        RelativeLayout mainLayout = findViewById(R.id.mainLayout);
        boolean condicionesCumplidas = false;

        if (valorTemperatura <= 25 && valorCorazon <= 60 ) {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.frio));
            txtAlerta.setText(getString(R.string.valoresBajos));
            condicionesCumplidas = true;
        }

        if (valorTemperatura >= 40 && valorCorazon >= 100) {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.calor));
            txtAlerta.setText(getString(R.string.valoresAltos));
            condicionesCumplidas = true;
        }

        if (valorTemperatura > 25 && valorTemperatura < 40 && valorCorazon > 60 && valorCorazon < 100) {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.normal));
            txtAlerta.setText(getString(R.string.valoresNormales));
            condicionesCumplidas = true;
        }

        //si no hay valores
        if (valorTemperatura == 0 && valorCorazon == 0) {
            txtAlerta.setText("");
            iconoCorazon.clearColorFilter();
        }

        //si no se cumplen las condiciones
        if (!condicionesCumplidas) {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.desigual));
            txtAlerta.setText(getString(R.string.valoresDesiguales));
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}







