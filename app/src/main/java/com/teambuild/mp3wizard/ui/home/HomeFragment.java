package com.teambuild.mp3wizard.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.ui.dashboard.DashboardFragment;
import com.teambuild.mp3wizard.ui.dataStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ListView listView;
    private FirebaseListAdapter firebaseListAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }



    protected View listViewPopulate(View root) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userId = mFirebaseUser.getUid();
//        storageRef = storage.getReference();

        listView = root.findViewById(R.id.downloadListView);
        Query query = FirebaseDatabase.getInstance().getReference().child(userId);

        ArrayList<String> downloadList = new ArrayList<String>(Arrays.asList(dataStorage.ReadDownloadedList(getContext(), userId)));
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(getContext(), R.layout.book_info_downloads, downloadList);
        listView.setAdapter(arrayAdapter);

        listView.setAdapter(firebaseListAdapter);
        return root;
    }


    private  boolean removeFile(Book book, String userId){
        return true;
    }

}