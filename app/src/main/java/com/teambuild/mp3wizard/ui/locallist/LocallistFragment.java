package com.teambuild.mp3wizard.ui.locallist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.repository.RepositorySingleton;
import com.teambuild.mp3wizard.repository.database.local.LocalListAdapterSQLITE;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;

import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class LocallistFragment extends Fragment {

    private LocallistViewModel locallistViewModel;
    private ListView listView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        locallistViewModel = ViewModelProviders.of(this).get(LocallistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_locallist, container, false);

        listView = root.findViewById(R.id.downloadListView);
        listView.setAdapter(RepositorySingleton.getInstance().getLocalListAdapterSQLITE(getContext()));

        final TextView textView = root.findViewById(R.id.text_home);

        locallistViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }


    private  boolean removeFile(Book book, String userId){
        return true;
    }

}