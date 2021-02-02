package com.inhealion.generator;

import android.app.Application;

import com.inhealion.networking.GeneratorApiClient;
import com.inhealion.networking.account.SharedPrefAccountStore;

import timber.log.Timber;

public class GeneratorApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        GeneratorApiClient.initialize(BuildConfig.BASE_URL, new SharedPrefAccountStore(this));
    }
}
