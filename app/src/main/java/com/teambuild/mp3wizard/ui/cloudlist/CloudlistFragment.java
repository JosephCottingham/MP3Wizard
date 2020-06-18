package com.teambuild.mp3wizard.ui.cloudlist;

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

import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.repository.RepositorySingleton;

public class CloudlistFragment extends Fragment {
    private CloudlistViewModel cloudlistViewModel;
    private ListView listView;

    // displays content currently stored in firebase and permits interaction

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // set the view model
        Log.d("CloudlistFragment", "onCreateView: set the view model");
        cloudlistViewModel = ViewModelProviders.of(this).get(CloudlistViewModel.class);

        // get view displayed
        Log.d("CloudlistFragment", "onCreateView: get view displayed");
        View root = inflater.inflate(R.layout.fragment_cloudlist, container, false);

        // sets listview with data
        listView = root.findViewById(R.id.cloudListView);
        Log.d("CloudlistFragment", "onCreateView: set Adaptor");
        listView.setAdapter(RepositorySingleton.getInstance().getCloudListAdapterFirebase(CloudlistFragment.this)); //TODO get firebase adaptor


        // configs header
        final TextView textView = root.findViewById(R.id.text_cloud);
        cloudlistViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}