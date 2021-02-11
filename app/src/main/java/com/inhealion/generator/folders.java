package com.inhealion.generator;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.inhealion.networking.ApiCallback;
import com.inhealion.networking.GeneratorApiClient;
import com.inhealion.networking.api.model.Folder;
import com.inhealion.networking.api.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link folders#newInstance} factory method to
 * create an instance of this fragment.
 */
public class folders extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    static User user;
    static ListView lv;


    // Create an ArrayAdapter from List




    public folders() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment folders.
     */
    // TODO: Rename and change types and number of parameters
    public static folders newInstance(String param1, String param2) {
        folders fragment = new folders();
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
        View v =  inflater.inflate(R.layout.fragment_folders, container, false);
        user = (User) getArguments().getParcelable("User");
        lv = (ListView) v.findViewById(R.id.folderListView);
        ArrayList<String> listItems=new ArrayList<String>();
        String[] names = {};
        //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
        ArrayAdapter<String> adapter;


        adapter=new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                listItems);
        lv.setAdapter(adapter);
        adapter.add("ABC");
        GeneratorApiClient.instance().fetchFolders(user.getProfile().getId(), new ApiCallback<List<Folder>>() {
            @Override
            public void success(List<Folder> value) {
                int i = 0;
                while (i < value.size()) {
                    Timber.d("%s",value.get(i).getName());
                    //names[i]=value.get(i).getName();
                    i++;
                }

            }

            @Override
            public void failure(@NotNull Exception error) {
                Timber.d(error, " + FOLDERS PROCESSING ERROR");
            }
        });
        return v;
    }


}