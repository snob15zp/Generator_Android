package com.inhealion.generator.activity;

import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.inhealion.generator.R;
import com.inhealion.generator.folders;
import com.inhealion.generator.log_in;
import com.inhealion.networking.ApiCallback;
import com.inhealion.networking.GeneratorApiClient;
import com.inhealion.networking.api.model.User;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity  {
    private folders FoldersFragment;
    private log_in LogInFragment;

    String zipFilename = Environment.getExternalStorageDirectory() + "/unzipped/files.zip";
    String unzipLocation = Environment.getExternalStorageDirectory() + "/unzipped";
    GeneratorApiClient client = GeneratorApiClient.instance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        LogInFragment = new log_in();
        FoldersFragment = new folders();
        Bundle bundle = new Bundle();
        bundle.putParcelable("GeneratorApiClient", client); // There we should be able to pack client obj to bunble
       getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame,LogInFragment)
                .commit();
    }
}

