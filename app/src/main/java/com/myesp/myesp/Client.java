package com.myesp.myesp;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private final UICommunicatorIm uiCommunicatorIm;
    public final Log log;
    private Socket socket;
    private BufferedReader bufferedReader;
    private OutputStream outputStream;
    private Thread receiveThread;
    String address;
    String port;

    private class Log {
        static public final int DEBUG = Color.BLUE;
        static public final int INFO = Color.GREEN;
        static public final int WARN = Color.YELLOW;
        static public final int ERR = Color.RED;

        private final Map<Integer, String> logMap = new HashMap<Integer, String>() {{
            put(DEBUG, "D");
            put(INFO, "I");
            put(WARN, "W");
            put(ERR, "E");
        }};

        public void appendLog(String text) {
            appendLog(text, Color.WHITE);
        }

        public void appendLog(int level, String text) {
            appendLog(text, level);
        }

        public void appendLog(String text, int color) {
            String msg = "N";
            if (logMap.containsKey(color)) {
                msg = logMap.get(color);
            }
            LocalTime localTime = LocalTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            msg = "[" + localTime.format(dateTimeFormatter) + "]" + "[" + msg + "]: " + text;
            final SpannableString spannableString = new SpannableString(msg + "\n");
            spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            uiCommunicatorIm.updateLogTextView(spannableString);
        }
    }

    public Client(UICommunicatorIm uiCommunicatorIm) {
        this.uiCommunicatorIm = uiCommunicatorIm;
        this.log = new Log();
        this.address = "";
        this.port = "";
    }

    public void connectToServer() {
        new Thread(() -> {
            try {
                address = uiCommunicatorIm.getAddressEditText();
                port = uiCommunicatorIm.getPortEditText();
                socket = new Socket(address, Integer.parseInt(port));
                outputStream = socket.getOutputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                log.appendLog(Log.INFO, "已连接到服务器: " + address + ":" + port);
                startReceiving();
            } catch (Exception e) {
                log.appendLog(Log.ERR, "建立连接失败: " + address + ":" + port);
                uiCommunicatorIm.onSwitchToggle(false);
            }
        }).start();
    }

    public void disconnectFromServer() {
        try {
            if (socket != null) {
                socket.close();
                if (receiveThread != null) {
                    receiveThread.interrupt();
                }
                log.appendLog(Log.INFO, "连接已断开: " + address + ":" + port);
            }
        } catch (Exception e) {
            log.appendLog(Log.ERR, "无法安全断开连接: " + address + ":" + port);
        }
    }

    public void sendMessage(String message) {
        if (!TextUtils.isEmpty(message) && socket != null && socket.isConnected()) {
            new Thread(() -> {
                try {
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                    uiCommunicatorIm.updateMessageEditText("");
                    log.appendLog(Log.INFO, "发送消息:" + message);
                } catch (Exception e) {
                    log.appendLog(Log.ERR, "发送失败:" + e.getMessage());
                }
            }).start();
        }
    }

    private void startReceiving() {
        receiveThread = new Thread(() -> {
            try {
                String receivedMessage;
                while ((receivedMessage = bufferedReader.readLine()) != null) {
                    log.appendLog(Log.INFO, "收到消息:" + receivedMessage);
                }
            } catch (Exception e) {
                log.appendLog(Log.ERR, "接收消息出错: " + e.getMessage());
            }
        });
        receiveThread.start();
    }
}
