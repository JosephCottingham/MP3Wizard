package com.teambuild.mp3wizard.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.teambuild.mp3wizard.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView textView = root.findViewById(R.id.text_settings);
        settingsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}



//TODO
//        addFileBtn = findViewById(R.id.addFileBtn);
//                addFileBtn.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        Intent fileNavIntent=new Intent(Intent.ACTION_GET_CONTENT);
//        fileNavIntent.setType("*/*");
//        startActivityForResult(fileNavIntent, 10);
//
//        }
//        });
//@Override
//protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode){
//        case 10:
//        if (requestCode==RESULT_OK){
//        String path = data.getDataString();
//        }
//        break;
//        }
//        }