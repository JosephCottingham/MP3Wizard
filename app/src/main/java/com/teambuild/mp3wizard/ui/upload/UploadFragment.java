package com.teambuild.mp3wizard.ui.upload;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.teambuild.mp3wizard.R;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {

    private UploadViewModel uploadViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        uploadViewModel =
                ViewModelProviders.of(this).get(UploadViewModel.class);
        View root = inflater.inflate(R.layout.fragment_upload, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        uploadViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        final Button fileAddBtn = root.findViewById(R.id.FileBtn);
        fileAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileNavIntent=new Intent(Intent.ACTION_GET_CONTENT);
                fileNavIntent.setType("audio/mpeg4-generic");
                startActivityForResult(fileNavIntent, 10);
            }
        });
        return root;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("Hello", data.getDataString());
        Log.d("Hello", String.valueOf(requestCode));
        Log.d("Hello", String.valueOf(resultCode));
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if (requestCode==RESULT_OK){
                    String path = data.getDataString();
                    Log.d("Hello", "onActivityResult: "+ path);

                }
                break;
        }
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