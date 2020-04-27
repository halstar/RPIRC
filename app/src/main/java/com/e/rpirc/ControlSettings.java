package com.e.rpirc;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ControlSettings extends AppCompatActivity {

    public  static final String  EXTRA_SETTINGS_RESULT = "com.e.rpirc.settings.result";

    private Button ok, cancel;

    public static String cameraAddressPort;

    EditText cameraAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ok     = findViewById(R.id.okButton    );
        cancel = findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        cameraAddressPort = bundle.getString(ControlRobot.EXTRA_CAMERA_ADDRESS);
        cameraAddress     = findViewById(R.id.camera_address);
        cameraAddress.setHint(cameraAddressPort);

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SETTINGS_RESULT, cameraAddress.getText().toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SETTINGS_RESULT, "");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
