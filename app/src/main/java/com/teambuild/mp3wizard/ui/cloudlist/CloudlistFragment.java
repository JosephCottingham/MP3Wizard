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

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.repository.RepositorySingleton;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;

import java.io.FileOutputStream;
import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class CloudlistFragment extends Fragment {
    private FileOutputStream fileOutputStream;
    private CloudlistViewModel cloudlistViewModel;
    private StorageReference StorageReference;
    private StorageReference ref;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ListView listView;
    private FirebaseListAdapter firebaseListAdapter;
    private LocalSQLiteDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cloudlistViewModel = ViewModelProviders.of(this).get(CloudlistViewModel.class);

        View root = inflater.inflate(R.layout.fragment_cloudlist, container, false);
        db = new LocalSQLiteDatabase();
        listView = root.findViewById(R.id.cloudListView);
        listView.setAdapter(RepositorySingleton.getInstance().getCloudListAdapterFirebase(CloudlistFragment.this)); //TODO get firebase adaptor
        ArrayList<Book> arrayList = db.getAllDownloadData();

        for (int x = 0; x < arrayList.size(); x++){
            Book book = arrayList.get(x);
            Log.d("TEST", "printDB: DBRow " + String.valueOf(x) + ": " + book.getTitle() + " | " + book.getCurrentFile() + " | " + book.getFileNum() + " | " + book.getLocSec() + " | " + book.getID());
        }
        final TextView textView = root.findViewById(R.id.text_dashboard);

        cloudlistViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}