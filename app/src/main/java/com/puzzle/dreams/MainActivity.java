package com.puzzle.dreams;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity  {

    public TimePicker timePicker;
    public ImageView background;
    public RadioGroup radios;
    public RadioGroup date;
    public Button goToSleep;
    public Sensor mSensor;
    public SensorManager sensorManager;
    public SensorEvent sensorEvent;
    public MediaPlayer mediaPlayer;
    public MediaRecorder mediaRecorder;
    public Calendar c;
    public Button hearTone;
    public String tone;
    public Handler handler;
    public long alarmTime;
    public long sleepDuration;
    public long currentTime;
    public TextView remaining;
    public TextView left;
    soundMeter Meter;
    MediaRecorder mRecorder;
    public String sleepLeft;
    public Sensor accelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int MOVEMENT_THRESHOLD = 50;
    SensorEventListener eventListener;
    public TextView movement;
    public TextView noisetext;
    HashMap<Long, Double> noiseLevels = new HashMap<>();
    HashMap<Long, Float> movementLevels = new HashMap<>();
    public long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        timePicker = (TimePicker) findViewById(R.id.time);
        timePicker.setIs24HourView(true);
        radios = (RadioGroup) findViewById(R.id.radioGroup);
        date = (RadioGroup) findViewById(R.id.date);
        goToSleep = (Button) findViewById(R.id.sleep);
        background = (ImageView) findViewById(R.id.background);
        c = Calendar.getInstance();
        hearTone = (Button) findViewById(R.id.play);
        mediaPlayer = new MediaPlayer();
        left = (TextView) findViewById(R.id.left);
        movement = (TextView) findViewById(R.id.movement);
        noisetext = (TextView) findViewById(R.id.noise);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       // sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        eventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {




                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];


                    long curTime = System.currentTimeMillis();

                    if ((curTime - lastUpdate) > 100) {
                        long diffTime = (curTime - lastUpdate);
                        lastUpdate = curTime;

                        float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                        if (speed > MOVEMENT_THRESHOLD) {

                            Log.d("moving too fast", String.valueOf(speed));
                            movementLevels.put(curTime,speed);

                            movement.setText("moving");

                        }else{
                            movement.setText("");
                        }


                        last_x = x;
                        last_y = y;
                        last_z = z;
                    }
                }




            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };






                hearTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying() == false) {
                    int radio = radios.getCheckedRadioButtonId();
                    RadioButton rb = (RadioButton) findViewById(radio);

                    String tone = rb.getText().toString();
                    int resID = getApplicationContext().getResources().getIdentifier(tone, "raw", getApplicationContext().getPackageName());

                     mediaPlayer = MediaPlayer.create(getApplicationContext(), resID);

                     mediaPlayer.start();



                } else {
                    Log.d("stopped", "stopped");

                    mediaPlayer.stop();
                }
            }
        });

        goToSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 background.setVisibility(View.VISIBLE);
                hearTone.setVisibility(View.GONE);
                goToSleep.setVisibility(View.GONE);

               startTime = System.currentTimeMillis();

                int yourMinutes = (timePicker.getCurrentHour()*60) + timePicker.getCurrentMinute();


                int radio1 = date.getCheckedRadioButtonId();
                RadioButton rb1 = (RadioButton) findViewById(radio1);

                String date = rb1.getText().toString();

                if(date.equals("Tomorrow")) {

                    c.add(Calendar.DAY_OF_YEAR, 1);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    long mili = TimeUnit.MINUTES.toMillis(yourMinutes);
                    alarmTime = c.getTimeInMillis()+mili;

                }else{
                    c.add(Calendar.DAY_OF_YEAR, 0);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    long mili = TimeUnit.MINUTES.toMillis(yourMinutes);
                    alarmTime = c.getTimeInMillis()+mili;

                }





                Log.d("system  time = ", String.valueOf(currentTime));
                Log.d("wakeup time = ", String.valueOf(alarmTime));

                int radio = radios.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) findViewById(radio);

                tone = rb.getText().toString();
                Meter = new soundMeter();
                Meter.start();

                sensorManager.registerListener(eventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                monitorSleep.start();




            }
        });



    }


    Thread monitorSleep = new Thread() {
        @Override
        public void run() {
            try {
                while(System.currentTimeMillis() < alarmTime) {
                    sleep(1000);
                    sleepDuration = alarmTime - System.currentTimeMillis();


                    long minute = (sleepDuration / (1000 * 60)) % 60;
                    long hour = (sleepDuration / (1000 * 60 * 60)) % 24;

                     sleepLeft = String.format("%02d:%02d", hour, minute);

                    updateText(sleepLeft);






                    if(Meter != null) {

                       double noise = Meter.getAmplitude();
                        Log.d("noise", String.valueOf(noise));

                        updateNoiseText(String.valueOf(noise));
                        if(noise> 3000) {
                            noiseLevels.put(System.currentTimeMillis(), noise);

                        }
                    }




                }if(System.currentTimeMillis() > alarmTime){


                    sensorManager.unregisterListener(eventListener, accelerometer);


                    Map.Entry<Long,Double> maxEntry = null;

                    for(Map.Entry<Long,Double> entry : noiseLevels.entrySet()) {
                        if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                            maxEntry = entry;
                        }

                    }

                    String start = String.valueOf(startTime);
                    String alarm = String.valueOf(alarmTime);


                    Intent i = new Intent(getApplicationContext(), sleepResult.class);
                    i.putExtra("noise", noiseLevels);
                    i.putExtra("movement", movementLevels);
                    i.putExtra("start", start);
                    i.putExtra("alarm", alarm);
                    i.putExtra("tone", tone);

                    startActivity(i);

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    };


    public void updateText(final String leftText) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.d("sleepLeft", leftText);

                left.setText("Sleep Left: "+leftText);


            }
        });

    }
    public void updateNoiseText(final String noiseText) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                noisetext.setText("Noise Level: "+noiseText);


            }
        });

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
