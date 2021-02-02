package com.inhealion.generator.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inhealion.generator.R;
import com.inhealion.networking.ApiCallback;
import com.inhealion.networking.GeneratorApiClient;
import com.inhealion.networking.api.model.User;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GeneratorApiClient client = GeneratorApiClient.instance();

        client.signIn("admin", "admin", new ApiCallback<User>() {
            @Override
            public void success(User user) {
                Timber.d("Login Success%s", user);
            }

            @Override
            public void failure(@NotNull Exception error) {
                Timber.e(error, "Login Failed");
            }
        });
    }
}
