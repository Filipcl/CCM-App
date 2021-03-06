package com.example.android.androidskeletonapp.ui.splash;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.data.Sdk;
import com.example.android.androidskeletonapp.data.service.ActivityStarter;
import com.example.android.androidskeletonapp.ui.login.LoginActivity;
import com.example.android.androidskeletonapp.ui.main.MainActivity;
import com.facebook.stetho.Stetho;
import com.google.android.material.snackbar.Snackbar;

import org.hisp.dhis.android.core.D2Manager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {

    private final static boolean DEBUG = true;
    private Disposable disposable;
    private Boolean wifiConnected = false;
    private Boolean mobileConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        checkNetworkConnection();
    }

    private void checkNetworkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()){
            wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if(wifiConnected || mobileConnected){
                disposable = D2Manager.instantiateD2(Sdk.getD2Configuration(this))
                        .flatMap(d2 -> d2.userModule().isLogged())
                        .doOnSuccess(isLogged -> {
                            if (isLogged) {
                                ActivityStarter.startActivity(this, MainActivity.getMainActivityIntent(this),true);
                            } else {
                                ActivityStarter.startActivity(this, LoginActivity.getLoginActivityIntent(this),true);
                            }
                        }).doOnError(throwable -> {
                            throwable.printStackTrace();
                            ActivityStarter.startActivity(this, LoginActivity.getLoginActivityIntent(this),true);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
            }
        }else{
            ActivityStarter.startActivity(this, MainActivity.getMainActivityIntent(this),true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }

    }
}