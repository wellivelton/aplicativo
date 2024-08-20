package com.tv.telecine;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tv.telecine.data.RetrofitClient;
import com.tv.telecine.data.api.UserApi;
import com.tv.telecine.model.ActiveStatus;
import com.tv.telecine.model.User;
import com.tv.telecine.database.DatabaseHelper;
import com.zhy.autolayout.config.AutoLayoutConifg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail,etPass;
    private Button mLogar, mCriarConta;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoLayoutConifg.getInstance().useDeviceSize();
        //Auto size
        setContentView(R.layout.activity_login);

        etEmail=findViewById(R.id.mEmail);
        etPass=findViewById(R.id.mSenha);
        mLogar=findViewById(R.id.mLogar);
        mCriarConta=findViewById(R.id.mCriarConta);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Aguarde carregando dados...");
        dialog.setCancelable(false);

        mLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidEmailAddress(etEmail.getText().toString())) {
                    Toast.makeText(getApplicationContext(),"E-mail não e valido",Toast.LENGTH_SHORT).show();
                } else if (etPass.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),"Digite sua senha",Toast.LENGTH_SHORT).show();
                } else {
                    String email = etEmail.getText().toString();
                    String pass = etPass.getText().toString();
                    login(email, pass);
                }

            }
        });

        mCriarConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CadatrarActivity.class);
                startActivity(intent);
            }
        });

    }

    private void login(String email, final String password){

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        UserApi api = retrofit.create(UserApi.class);
        Call<User> call = api.getUsuario("user_login",email, password);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {

                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);

                        if (db.getUserDataCount() > 1) {
                            db.deleteUserData();
                        } else {
                            if (db.getUserDataCount() == 0) {
                                db.insertUserData(user);
                            } else {
                                db.updateUserData(user, Integer.parseInt(user.getUserId()));
                            }
                        }

                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        editor.putString("user_name", db.getUserData().getUserName());
                        editor.putString("user_email", etEmail.getText().toString());
                        editor.putString("user_id", db.getUserData().getUserId());
                        editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        editor.apply();

                        //save user login time, expire time
                        updateSubscriptionStatus(db.getUserData().getUserId());

                    }else {
                        Toast.makeText(getApplicationContext(),"Erro ao logar",Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

                Toast.makeText(getApplicationContext(),"erro no servidor",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateSubscriptionStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        UserApi subscriptionApi = retrofit.create(UserApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {

                        ActiveStatus activeStatus = response.body();

                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteAllActiveStatusData();
                        db.insertActiveStatusData(activeStatus);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        startActivity(intent);
                        finish();
                        dialog.cancel();
                    }
                }
            }
            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

}