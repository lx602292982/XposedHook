package com.example.xposeddemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private EditText userName;
    private EditText userPsw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = (EditText) findViewById(R.id.name);
        userPsw = (EditText) findViewById(R.id.psw);

    }

    public void login(View v) {

        if (!checkSN(userName.getText().toString().trim(), userPsw.getText().toString().trim())) {
            Toast.makeText(this, "无效用户名或注册码！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkSN(String userName, String sn) {
        if ((userName == null) || (userName.length() == 0))
            return false;
        if ((sn == null) || (sn.length() == 0)) {
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(userName.getBytes());
            byte[] bytes = digest.digest();
            String hexString = toHexString(bytes, "");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hexString.length(); i += 2) {
                sb.append(hexString.charAt(i));
            }
            String userSn = sb.toString();
            if (!userSn.equalsIgnoreCase(sn)) {
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String toHexString(byte[] bytes, String separator) { //转为十六进制
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex).append(separator);
        }
        return hexString.toString();
    }
}
