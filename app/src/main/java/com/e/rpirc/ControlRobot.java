package com.e.rpirc;

import android.app.ProgressDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.UUID;

public class ControlRobot extends AppCompatActivity implements SensorEventListener {

    public static final String EXTRA_CAMERA_ADDRESS = "com.e.rpirc.camera.address";

    private final static String forward1String       = "F1";
    private final static String forward2String       = "F2";
    private final static String backward1String      = "B1";
    private final static String backward2String      = "B2";
    private final static String left1String          = "L1";
    private final static String left2String          = "L2";
    private final static String right1String         = "R1";
    private final static String right2String         = "R2";
    private final static String stopString           = "S";
    private final static String startCameraString    = "C1";
    private final static String stopCameraString     = "C0";
    private final static String startMotorsString    = "M1";
    private final static String stopMotorsString     = "M0";
    private final static String startObstaclesString = "O1";
    private final static String stopObstaclesString  = "O0";

    private BluetoothDevice device;
    private BluetoothSocket bluetoothSocket;
    private UUID            deviceUuid;
    private boolean         areSensorsInUse;
    private SensorManager   sensorManager;
    private long            lastUpdate;
    private ProgressDialog  progressDialog;
    private boolean         isUserInitiatedDisconnect = false;
    private boolean         isBluetoothConnected      = false;
    private String          cameraAddressPort         = "192.168.0.15:8000";

    CheckBox    useSensors, startCamera, startMotors, avoidObstacles;
    ImageButton forward1, forward2, backward1, backward2, stop, left1, left2, right1, right2;
    WebView     cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_robot);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        forward1       = findViewById(R.id.forward1       );
        forward2       = findViewById(R.id.forward2       );
        backward1      = findViewById(R.id.backward1      );
        backward2      = findViewById(R.id.backward2      );
        left1          = findViewById(R.id.left1          );
        left2          = findViewById(R.id.left2          );
        right1         = findViewById(R.id.right1         );
        right2         = findViewById(R.id.right2         );
        useSensors     = findViewById(R.id.use_sensors    );
        startCamera    = findViewById(R.id.show_camera    );
        startMotors    = findViewById(R.id.start_motors   );
        avoidObstacles = findViewById(R.id.avoid_obstacles);
        stop           = findViewById(R.id.stop           );
        cameraView     = findViewById(R.id.web_view       );

        cameraView.setWebViewClient(new WebViewClient());

        useSensors.setOnClickListener(new View.OnClickListener()
              {
                  @Override
                  public void onClick(View view) {
                      areSensorsInUse = useSensors.isChecked();
                      forward1.setEnabled      (!areSensorsInUse);
                      forward2.setEnabled      (!areSensorsInUse);
                      backward1.setEnabled     (!areSensorsInUse);
                      backward2.setEnabled     (!areSensorsInUse);
                      left1.setEnabled         (!areSensorsInUse);
                      left2.setEnabled         (!areSensorsInUse);
                      right1.setEnabled        (!areSensorsInUse);
                      right2.setEnabled        (!areSensorsInUse);
                      avoidObstacles.setEnabled(!areSensorsInUse);

                      if (areSensorsInUse == false) {
                          try {
                              bluetoothSocket.getOutputStream().write(stopString.getBytes());
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }
                  }
              }
        );

        startCamera.setOnClickListener(new View.OnClickListener()
              {
                  @Override
                  public void onClick(View view) {
                      try {
                          if (startCamera.isChecked() == true) {
                              bluetoothSocket.getOutputStream().write(startCameraString.getBytes());
                              Thread.sleep(500);
                              cameraView.loadUrl("http://" + cameraAddressPort);
                          } else {
                              bluetoothSocket.getOutputStream().write(stopCameraString.getBytes());
                              cameraView.loadUrl("about:blank");
                          }
                      } catch (IOException e) {
                          e.printStackTrace();
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                  }
              }
        );

        startMotors.setOnClickListener(new View.OnClickListener()
               {
                   @Override
                   public void onClick(View view) {
                       try {
                           if (startMotors.isChecked() == true) {
                               bluetoothSocket.getOutputStream().write(startMotorsString.getBytes());
                           } else {
                               bluetoothSocket.getOutputStream().write(stopMotorsString.getBytes());
                           }
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               }
        );

        avoidObstacles.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View view) {
                   boolean  isAvoidObstacles = avoidObstacles.isChecked();

                   areSensorsInUse = false;
                   useSensors.setEnabled(!isAvoidObstacles);
                   forward1.setEnabled  (!isAvoidObstacles);
                   forward2.setEnabled  (!isAvoidObstacles);
                   backward1.setEnabled (!isAvoidObstacles);
                   backward2.setEnabled (!isAvoidObstacles);
                   left1.setEnabled     (!isAvoidObstacles);
                   left2.setEnabled     (!isAvoidObstacles);
                   right1.setEnabled    (!isAvoidObstacles);
                   right2.setEnabled    (!isAvoidObstacles);

                   try {
                       if (isAvoidObstacles == true) {
                           bluetoothSocket.getOutputStream().write(startObstaclesString.getBytes());
                       } else {
                           bluetoothSocket.getOutputStream().write(stopObstaclesString.getBytes());
                       }
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                }
           }
        );

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        device        = bundle.getParcelable            (DevicesList.EXTRA_BT_DEVICE   );
        deviceUuid    = UUID.fromString(bundle.getString(DevicesList.EXTRA_DEVICE_UUID));

        forward1.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View view) {
               try {
                   bluetoothSocket.getOutputStream().write(forward1String.getBytes());
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }
        );

        forward2.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                try {
                    bluetoothSocket.getOutputStream().write(forward2String.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        );

        backward1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                try {
                    bluetoothSocket.getOutputStream().write(backward1String.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        );

        backward2.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View view) {
                 try {
                     bluetoothSocket.getOutputStream().write(backward2String.getBytes());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
        }
        );

        left1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                try {
                    bluetoothSocket.getOutputStream().write(left1String.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        );

        left2.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View view) {
                 try {
                     bluetoothSocket.getOutputStream().write(left2String.getBytes());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
        );

        right1.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View view) {
                 try {
                     bluetoothSocket.getOutputStream().write(right1String.getBytes());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
        );

        right2.setOnClickListener(new View.OnClickListener()
              {
                  @Override
                  public void onClick(View view) {
                  try {
                      bluetoothSocket.getOutputStream().write(right2String.getBytes());
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
          }
        );

        stop.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View view) {
                 areSensorsInUse         = false;
                 useSensors.setChecked    (false);
                 avoidObstacles.setChecked(false);
                 forward1.setEnabled      (true );
                 forward2.setEnabled      (true );
                 backward1.setEnabled     (true );
                 backward2.setEnabled     (true );
                 left1.setEnabled         (true );
                 left2.setEnabled         (true );
                 right1.setEnabled        (true );
                 right2.setEnabled        (true );
                 useSensors.setEnabled    (true );
                 avoidObstacles.setEnabled(true );

                 try {
                     bluetoothSocket.getOutputStream().write(stopString.getBytes());
                     bluetoothSocket.getOutputStream().flush();
                     bluetoothSocket.getOutputStream().write(stopObstaclesString.getBytes());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
        );

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        lastUpdate    = System.currentTimeMillis();

        Sensor rotationSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (rotationSensor == null) {
            msg("Rotation vector not available");
            finish();
        }

        return;
    }

    private void msg(final String str) {

        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            Intent intent = new Intent(getApplicationContext(), ControlSettings.class);
            intent.putExtra(EXTRA_CAMERA_ADDRESS, cameraAddressPort);
            startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (areSensorsInUse == true) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                getRotation(event);
            }
        }
        return;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (areSensorsInUse == true) {
        }
        return;
    }

    private void getRotation(SensorEvent event) {

        long actualTime = event.timestamp;
        byte outputBuffer[] = new byte[3];

        if (actualTime - lastUpdate < 50) {
            return;
        }
        lastUpdate = actualTime;

        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(
                rotationMatrix,
                event.values);

        float[] orientations = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientations);

        for(int i = 0; i < 3; i++) {
            orientations[i] = (float)(Math.toDegrees(orientations[i]));
        }

        if (bluetoothSocket != null && isBluetoothConnected == true) {
            try {
                outputBuffer[0] = (byte)orientations[0];
                outputBuffer[1] = (byte)orientations[1];
                outputBuffer[2] = (byte)orientations[2];
                bluetoothSocket.getOutputStream().write(outputBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DisconnectBt extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isBluetoothConnected = false;
            if (isUserInitiatedDisconnect == true) {
                finish();
            }
        }
    }

    @Override
    protected void onPause() {

        if (bluetoothSocket != null && isBluetoothConnected == true) {

            try {
                bluetoothSocket.getOutputStream().write(stopString.getBytes         ());
                bluetoothSocket.getOutputStream().flush();
                bluetoothSocket.getOutputStream().write(stopObstaclesString.getBytes());
                bluetoothSocket.getOutputStream().flush();
                bluetoothSocket.getOutputStream().write(stopCameraString.getBytes   ());

            } catch (IOException e) {
                e.printStackTrace();
            }

            new DisconnectBt().execute();
        }
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {

        areSensorsInUse        =  false;
        useSensors.setChecked    (false);
        startCamera.setChecked   (false);
        avoidObstacles.setChecked(false);
        forward1.setEnabled      (true );
        forward2.setEnabled      (true );
        backward1.setEnabled     (true );
        backward2.setEnabled     (true );
        left1.setEnabled         (true );
        left2.setEnabled         (true );
        right1.setEnabled        (true );
        right2.setEnabled        (true );
        useSensors.setEnabled    (true );
        avoidObstacles.setEnabled(true );

        if (bluetoothSocket == null || isBluetoothConnected == false) {
            new ConnectBT().execute();
        }
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean isConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(ControlRobot.this, "Hold on", "Connecting...");
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (bluetoothSocket == null || !isBluetoothConnected) {
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(deviceUuid);
                    bluetoothSocket.connect();
                }
            } catch (IOException e) {
                msg("Could not connect to BT device.\nPlease check your device or BT server");
                isConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (isConnectSuccessful == false) {
                msg("Could not connect to BT device.\nTurn your device ON?...");
                finish();
            } else {
                msg("Connected to BT device");
                isBluetoothConnected = true;
                try {
                    bluetoothSocket.getOutputStream().write(stopString.getBytes         ());
                    bluetoothSocket.getOutputStream().flush();
                    bluetoothSocket.getOutputStream().write(stopObstaclesString.getBytes());
                    bluetoothSocket.getOutputStream().flush();
                    bluetoothSocket.getOutputStream().write(stopCameraString.getBytes   ());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                String returnString = data.getStringExtra(ControlSettings.EXTRA_SETTINGS_RESULT);

                if (returnString.isEmpty() == false) {
                    cameraAddressPort = returnString;
                    cameraView.loadUrl("http://" + cameraAddressPort);
                }
            }
        }
    }
}
