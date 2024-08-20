package com.tv.telecine;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tv.telecine.activity.CategoryActivity;
import com.tv.telecine.activity.FilmesActivity;
import com.tv.telecine.activity.SearcheActivity;
import com.tv.telecine.activity.TvActivity;
import com.tv.telecine.activity.UserActivity;
import com.tv.telecine.data.RetrofitClient;
import com.tv.telecine.data.api.BannerApi;
import com.tv.telecine.database.DatabaseHelper;
import com.tv.telecine.database.PreferenceUtils;
import com.tv.telecine.fragment.CategoriasFragment;
import com.tv.telecine.fragment.DestaquesFragment;
import com.tv.telecine.fragment.ExplorarFragment;
import com.tv.telecine.fragment.FilmesFragment;
import com.tv.telecine.fragment.InfantilFragment;
import com.tv.telecine.fragment.SeriesFragment;
import com.tv.telecine.model.Banners;
import com.zhy.autolayout.config.AutoLayoutConifg;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.signal.DialogPropertiesSignal;
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.tv.telecine.R.drawable.focus_circle;
import static com.tv.telecine.R.drawable.selector_navigation;

public class MainActivity extends AppCompatActivity {
    Dialog epicDialog;
    private DatabaseHelper db;
    private TextView destaque, filmes, series, kids, explorar, categorias, mTextAreaFlag, mTextAppVersao, mContinuar;
    private ImageView mUser, mNotfic, mConfig, mImageBanner,mSearsh;
    private Button mConfirmar;
    private boolean expired = false;
    private FrameLayout mPopup;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoLayoutConifg.getInstance().useDeviceSize();//Auto size
        setContentView(R.layout.activity_main);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_normal);
        PreferenceUtils.updateSubscriptionStatus(MainActivity.this);
        boolean statusUser = PreferenceUtils.isActivePlan(getApplicationContext());
        db = new DatabaseHelper(MainActivity.this);
        epicDialog = new Dialog(this);

//        WVersionManager versionManager = new WVersionManager(this);
//        versionManager.setVersionContentUrl("https://portal.daweve.com.br/uploads/update.txt");
//        versionManager.setUpdateUrl("https://portal.daweve.com.br/uploads/app-debug.apk");
//        versionManager.checkVersion();

        initMana();
        initView();
        getBanner();

        String versao = BuildConfig.VERSION_NAME;
        mTextAppVersao.setText("V" + versao);
        mTextAreaFlag.setText("Uma nova atualização já disponival. Versão " + versao);
        //mTextAreaFlag.setSelected(true);

        mSearsh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearcheActivity.class);
                startActivity(intent);
            }
        });
        mUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        mNotfic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifications();
            }
        });
        mConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder builder = new NoInternetDialogSignal.Builder(this,
                getLifecycle()
        );
        DialogPropertiesSignal properties = builder.getDialogProperties();

        properties.setConnectionCallback(new ConnectionCallback() { // Optional
            @Override
            public void hasActiveConnection(boolean hasActiveConnection) {

            }
        });
        properties.setCancelable(true); // Optional
        properties.setNoInternetConnectionTitle("Sem internet"); // Optional
        properties.setNoInternetConnectionMessage("Verifique sua conexão com a Internet e tente novamente"); // Optional
        properties.setShowInternetOnButtons(true); // Optional
        properties.setPleaseTurnOnText("Conectar"); // Optional
        properties.setWifiOnButtonText("Wifi"); // Optional
        properties.setMobileDataOnButtonText("Dados"); // Optional
        builder.build();


    }
    
    private void initMana() {
        mUser = findViewById(R.id.mUser);
        mUser.setClickable(true);
        mNotfic = findViewById(R.id.mNotification);
        mNotfic.setClickable(true);
        mConfig = findViewById(R.id.mConfig);
        mConfig.setClickable(true);
        mSearsh = findViewById(R.id.mSearsh);
        mSearsh.setClickable(true);
        mTextAreaFlag = findViewById(R.id.mTextAreaFlag);
        mTextAppVersao = findViewById(R.id.mTextAppVersao);
        mPopup = findViewById(R.id.mPopup);

        destaque = findViewById(R.id.destaque);
        destaque.setClickable(true);
        filmes = findViewById(R.id.filmes);
        filmes.setClickable(true);
        series = findViewById(R.id.series);
        series.setClickable(true);
        kids = findViewById(R.id.kids);
        kids.setClickable(true);
        explorar = findViewById(R.id.explorar);
        explorar.setClickable(true);
        categorias = findViewById(R.id.categorias);
        categorias.setClickable(true);

        mImageBanner = findViewById(R.id.mImageBanner);

    }

    private void initView() {
        //replaceFraguiment(new DestaquesFragment());
        homeMenu(destaque, new DestaquesFragment());
        homeMenu(filmes, new FilmesFragment());
        homeMenu(series, new SeriesFragment());
        homeMenu(kids, new InfantilFragment());
        kids.setNextFocusDownId(R.id.mPosterView01);
        homeMenu(explorar, new ExplorarFragment());
        explorar.setNextFocusDownId(R.id.mPosterView01);
        homeMenu(categorias, new CategoriasFragment());
        categorias.setNextFocusDownId(R.id.mCategoria1);
    }

    private void homeMenu(View view, Fragment fragment) {

        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    v.setBackgroundResource(selector_navigation);
                    v.setPadding(0, 15, 0, 15);

                    replaceFraguiment(fragment);
                }else {
                    v.setBackgroundResource(selector_navigation);
                    v.setPadding(0, 15, 0, 15);
                }
            }
        });

    }

    private void replaceFraguiment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_direita, R.anim.anim_esquerda, R.anim.anim_direita, R.anim.anim_esquerda);
        transaction.replace(R.id.mFramerLayout, fragment);
        transaction.commit();
    }

    private void getBanner() {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        BannerApi api = retrofit.create(BannerApi.class);
        Call<List<Banners>> call = api.getBanners();
        call.enqueue(new Callback<List<Banners>>() {
            @Override
            public void onResponse(Call<List<Banners>> call, Response<List<Banners>> response) {
                if (response.code() == 200) {
                    List<Banners> banner = response.body();
                    for (Banners bannerlist : banner) {

                        if (bannerlist.getType().equals("filme")) {

                            ImageView mImagePopup = findViewById(R.id.mImagePopup);
                            Glide.with(getApplicationContext()).load(bannerlist.getImage()).into(mImagePopup);
                            Button mBtnOk = findViewById(R.id.mBtnOk);
                            mPopup.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_vod_filter));
                            mPopup.setVisibility(View.VISIBLE);
                            mBtnOk.setFocusable(true);
                            mBtnOk.setSelected(true);
                            mPopup.bringToFront();

                            mBtnOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mPopup.setVisibility(View.GONE);
                                }
                            });
                            mPopup.setVisibility(View.GONE);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Banners>> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"error no servidor", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void history() {

        epicDialog.setContentView(R.layout.dialog_error_login);
        mContinuar = (TextView) epicDialog.findViewById(R.id.bt_getcode);
        mContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
            }
        });
        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();

    }

    private void notifications() {
        epicDialog.setContentView(R.layout.dialog_msg_upgrade);
        mConfirmar = (Button) epicDialog.findViewById(R.id.mConfirmar);

        mConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
            }
        });
        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();

    }

    private Toast exitToast;

    @Override
    public void onBackPressed() {
        mPopup.setVisibility(View.GONE);
        if (exitToast == null || exitToast.getView() == null || exitToast.getView().getWindowToken() == null) {
            exitToast = Toast.makeText(this, "Pressione novamente para sair!", Toast.LENGTH_LONG);
            exitToast.show();
        } else {
            exitToast.cancel();
            finish();
        }
    }



}