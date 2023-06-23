package com.example.temperaturaambiental3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor temperatureSensor;
    private Sensor heartRateSensor;
    private TextView txtValorTemperatura;
    private TextView txtValorCorazon;

    // Valores Minimos
    private static final int FRIO_MINIMO = 25;
    private static final int FRECUENCIA_MIN = 60;

    // Valores Maximos
    public static final int CALOR_MAXIMO = 40;
    public static final int FRECUENCIA_MAX = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtValorTemperatura = findViewById(R.id.txtValorTemperatura);
        txtValorCorazon = findViewById(R.id.txtValorCorazon);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
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
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensors();
    }

    private void startSensors() {
        sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
            Comparacion(valorTemperatura, 0);
        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float valorCorazon = event.values[0];
            txtValorCorazon.setText(String.valueOf(valorCorazon));
            float valorTemperatura = Float.parseFloat(txtValorTemperatura.getText().toString());
            Comparacion(valorTemperatura, valorCorazon);
        }

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float valor = event.values[0];
            txtValorCorazon.setText(String.valueOf(valor));
            ImageView iconoCorazon = findViewById(R.id.iconoCorazon);
            if (valor <= FRECUENCIA_MIN) {
                iconoCorazon.setColorFilter(ContextCompat.getColor(this, R.color.azul));
                //iconoCorazon.setColorFilter(ContextCompat.getColor(this,R.color.rojo));
            } else if (valor >= FRECUENCIA_MAX) {
                iconoCorazon.setColorFilter(ContextCompat.getColor(this, R.color.rojo));
            } else {
                iconoCorazon.clearColorFilter();
            }
        }
    }

    private void Comparacion(float valorTemperatura, float valorCorazon) {
        // Cambiar color del icono de temperatura
        ImageView iconoTemperatura = findViewById(R.id.iconoTemperatura);
        if (valorTemperatura <= FRIO_MINIMO) {
            iconoTemperatura.setColorFilter(ContextCompat.getColor(this, R.color.azul));
        } else if (valorTemperatura >= CALOR_MAXIMO) {
            iconoTemperatura.setColorFilter(ContextCompat.getColor(this, R.color.rojo));
        } else {
            iconoTemperatura.clearColorFilter();
        }

        // Cambiar color del icono de corazón
       /* ImageView iconoCorazon = findViewById(R.id.iconoCorazon);
        if (valorCorazon <= FRECUENCIA_MIN) {
            iconoCorazon.setColorFilter(ContextCompat.getColor(this,R.color.azul));
            //iconoCorazon.setColorFilter(ContextCompat.getColor(this,R.color.rojo));
        } else if (valorCorazon >= FRECUENCIA_MAX) {
            iconoCorazon.setColorFilter(ContextCompat.getColor(this, R.color.rojo));
        } else {
            iconoCorazon.clearColorFilter();
        }*/

        if (valorTemperatura <= FRIO_MINIMO && valorCorazon <= FRECUENCIA_MIN && valorTemperatura > 0 && valorCorazon> 0) {
            showTemperatureAlert("¡Cuidate! 🥶 Muevete a un lugar cálido.");
        } else if (valorTemperatura >= CALOR_MAXIMO && valorCorazon >= FRECUENCIA_MAX && valorTemperatura > 0 && valorCorazon > 0) {
            showTemperatureAlert("¡Cuidate! ☀️ Muevete a un lugar fresco.");
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    private void showTemperatureAlert(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}







