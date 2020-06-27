package com.example.tripcheck;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author glsite.com
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_ERROR = 2;
    private EditText mEtNumber;
    private EditText mEtPassword;
    private CheckBox mCbRemember;
    private Button mBtLogin;
    private SharedPreferences mSp;
    private String state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        // 统一的去获取权限
        performCodeWithPermission("读写sd卡", new PermissionCallback() {
                    @Override
                    public void hasPermission() {

                    }

                    @Override
                    public void noPermission() {

                    }
                },  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.READ_PHONE_STATE);
        mSp = this.getSharedPreferences("config", this.MODE_PRIVATE);

        mEtNumber = findViewById(R.id.et_number);
        mEtPassword = findViewById(R.id.et_password);
        mCbRemember = findViewById(R.id.cb_remember);
        mBtLogin = findViewById(R.id.bt_login);
        mBtLogin.setOnClickListener(this);
        restoreInfo();
    }

    /**
     * 从sp文件当中读取信息
     */
    private void restoreInfo() {
        String number = mSp.getString("number", "");
        String password = mSp.getString("password", "");
        mEtNumber.setText(number);
        mEtPassword.setText(password);
    }

    /**
     * 登录按钮的点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        final String number =  mEtNumber.getText().toString().trim();
        final String password =  mEtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(password)) {
            Toast.makeText(this,"用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // 判断是否需要记录用户名和密码
            if (mCbRemember.isChecked()) {
                // 被选中状态，需要记录用户名和密码
                // 需要将数据保存到sp文件当中
                SharedPreferences.Editor editor = mSp.edit();
                editor.putString("number", number);
                editor.putString("password", password);
                editor.commit();// 提交数据，类似关闭流，事务
            }

            new Thread(){
                @Override
                public void run() {
                    try {
                        String urlPath = getString(R.string.map_login_url);
                        URL url = new URL(urlPath);
                        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setConnectTimeout(5000);
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");// 设置发送的数据为表单类型，会被添加到http body当中
                        String data = "post=" + URLEncoder.encode("login", "utf-8")+"&username=" + URLEncoder.encode(number, "utf-8") + "&password=" + URLEncoder.encode(password, "utf-8");
                        conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
                        // post的请求是把数据以流的方式写了服务器
                        // 指定请求输出模式
                        conn.setDoOutput(true);
                        conn.getOutputStream().write(data.getBytes());
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            InputStream is = conn.getInputStream();
                            String str = StreamUtils.readStream(is);
                            state=str.substring(str.indexOf("state=")+"state=".length(),str.length());
                            if(state.equals("login success")) {
                                showToastInAnyThread("login success");
                                System.out.println("登陆成功");
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }else if(state.equals("login failed")){
                                System.out.println("用户或密码错误");
                                showToastInAnyThread("用户或密码错误");
                            }
                        } else {
                            showToastInAnyThread("服务器未响应"+code);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToastInAnyThread("POST请求失败");
                    }
                }
            }.start();
        }

    }

    /**
     * 在任意现成当中都可以调用弹出吐司的方法
     * @param result
     */
    private void showToastInAnyThread(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
