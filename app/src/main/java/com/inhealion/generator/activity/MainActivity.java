package com.inhealion.generator.activity;

import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inhealion.generator.R;
import com.inhealion.networking.ApiCallback;
import com.inhealion.networking.GeneratorApiClient;
import com.inhealion.networking.api.model.User;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    String zipFilename = Environment.getExternalStorageDirectory() + "/unzipped/files.zip";
    String unzipLocation = Environment.getExternalStorageDirectory() + "/unzipped";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GeneratorApiClient client = GeneratorApiClient.instance();
        Decompress d = new Decompress(zipFilename, unzipLocation);
        d.unzip();

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
