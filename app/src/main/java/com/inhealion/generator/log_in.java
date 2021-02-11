package com.inhealion.generator;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.inhealion.networking.ApiCallback;
import com.inhealion.networking.GeneratorApiClient;
import com.inhealion.networking.api.model.Folder;
import com.inhealion.networking.api.model.User;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link log_in#newInstance} factory method to
 * create an instance of this fragment.
 */
public class log_in extends Fragment {


    private Button logInBtn;
    private EditText unField, pwFiled;
    private folders FoldersFragment;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public log_in() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment log_in.
     */
    // TODO: Rename and change types and number of parameters
    public static log_in newInstance(String param1, String param2) {
        log_in fragment = new log_in();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_log_in, container, false);
        logInBtn = v.findViewById( R.id.logInBtn);
        unField = v.findViewById( R.id.unField);
        pwFiled = v.findViewById( R.id.pwFiled);
        logInBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String login = unField.getText().toString();
                String password = pwFiled.getText().toString();
                //there we should get client object from bundle
                //GeneratorApiClient client = GeneratorApiClient.instance();
                /////////////////GeneratorApiClient.instance().signIn(login, password, new ApiCallback<User>() {
                GeneratorApiClient.instance().signIn("test", "admin", new ApiCallback<User>() {
                    @Override
                    public void success(User user) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("User", user); // There we should be able to pack client obj to bunble
                        FoldersFragment = new folders();
                        FoldersFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.frame,FoldersFragment)
                                .commit();
                        Timber.d("Login Success%s", user);

                    }

                    @Override
                    public void failure(@NotNull Exception error) {
                        Timber.e(error, "Login Failed");
                    }
                });
            }
        });
        return v;
    }

}