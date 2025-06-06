package com.liccsu.tcpc;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements UICommunicatorIm {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private EditText addressEditText;
    private EditText portEditText;
    private EditText messageEditText;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch connectSwitch;
    private TextView logTextView;
    private ScrollView logScrollView;
    Button sendButton;

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressEditText = findViewById(R.id.addressEditText);
        portEditText = findViewById(R.id.portEditText);
        messageEditText = findViewById(R.id.messageEditText);
        connectSwitch = findViewById(R.id.connectSwitch);
        logTextView = findViewById(R.id.logTextView);
        logScrollView = findViewById(R.id.logScrollView);
        sendButton = findViewById(R.id.sendButton);

        client = new Client(this);

        connectSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                client.connectToServer();
            } else {
                client.disconnectFromServer();
            }
        });

        sendButton.setOnClickListener(v -> {
            String message = getMessageEditText();
            client.sendMessage(message);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnectFromServer();
    }

    @Override
    public String getAddressEditText() {
        return addressEditText.getText().toString();
    }

    @Override
    public void updateAddressEditText(String text) {
        handler.post(() -> addressEditText.setText(text));
    }

    @Override
    public String getPortEditText() {
        return portEditText.getText().toString();
    }

    @Override
    public void updatePortEditText(String text) {
        handler.post(() -> portEditText.setText(text));
    }

    @Override
    public String getMessageEditText() {
        return messageEditText.getText().toString();
    }

    @Override
    public void updateMessageEditText(String text) {
        handler.post(() -> messageEditText.setText(text));
    }

    @Override
    public void onSwitchToggle(boolean isCheck) {
        handler.post(() -> connectSwitch.setChecked(isCheck));
    }

    @Override
    public void onButtonClick() {
        handler.post(() -> sendButton.callOnClick());
    }

    @Override
    public void updateLogTextView(CharSequence text) {
        handler.post(() -> {
            logTextView.append(text);
            logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
        });
    }
}