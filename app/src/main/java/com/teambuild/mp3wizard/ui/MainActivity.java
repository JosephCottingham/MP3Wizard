package com.teambuild.mp3wizard.ui;

import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;


import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.audioplayer.AudioServiceConnectionSingleton;
import com.teambuild.mp3wizard.repository.RepositorySingleton;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private RepositorySingleton repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudioServiceConnectionSingleton.getInstance();
        RepositorySingleton.getInstance();

        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_locallist, R.id.navigation_cloudlist, R.id.navigation_upload, R.id.navigation_settings)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(navView, navController);
    }
}

