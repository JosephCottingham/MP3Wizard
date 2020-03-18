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

        ArrayList<String> downloadList = new ArrayList<String>(Arrays.asList(ReadDownloadedList()));
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(getContext(), R.layout.book_info_downloads, downloadList);
        listView.setAdapter(arrayAdapter);

        listView.setAdapter(firebaseListAdapter);
        return root;
    }

    private String[] ReadDownloadedList(){
        String line = null;
        String dir = getContext().getFilesDir().getAbsolutePath();
        File FileDir = new File(dir + "/" + mFirebaseUser.getUid() + "/");
        String path = FileDir.getAbsolutePath();

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path + "downloaded.txt"));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d("Hello", ex.getMessage());
        }
        catch(IOException ex) {
            Log.d("Hello", ex.getMessage());
        }
        Log.d("Hello", "ReadFile: Line " + line);
        return line.split(System.getProperty("line.separator"));
    }

    private String[] ReadTimeData(String title){
        String line = null;
        String dir = getContext().getFilesDir().getAbsolutePath();
        File FileDir = new File(dir + "/" + mFirebaseUser.getUid() + "/" + title + "/");
        String path = FileDir.getAbsolutePath();

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path + "loc.txt"));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d("Hello", ex.getMessage());
        }
        catch(IOException ex) {
            Log.d("Hello", ex.getMessage());
        }
        Log.d("Hello", "ReadFile: Line " + line);
        return line.split(System.getProperty("line.separator"));
    }

    private  boolean removeFile(Book book, String userId){
        return true;
    }

}