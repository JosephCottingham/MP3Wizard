package com.teambuild.mp3wizard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText email, password;
    private Button login;
    private TextView signupSwitch;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the current firebase instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        // interface var config (instantiate)
        email = findViewById(R.id.EmailLogin);
        password = findViewById(R.id.PasswordLogin);
        login = findViewById(R.id.LoginBtn);
        signupSwitch = findViewById(R.id.SignupPageSwitch);

        // listen for current authstate in order to audio login in data is auth alread given
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            // gets the current user to know where to direct/retreave data
            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // if we have a user we signin else we do not and prompt user
                if(mFirebaseUser != null){
                    Toast.makeText(MainActivity.this,"You are logged in",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this,"Please Login",Toast.LENGTH_SHORT).show();
                }
            }
        };

        // if auth requred when app waits for login button to be clicked
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // collects data
                String emailTxt = email.getText().toString();
                String passwordTxt = password.getText().toString();
                // confirms that their is a value in each
                if(emailTxt.isEmpty()){
                    email.setError("Please enter email");
                    email.requestFocus();
                } else if (passwordTxt.isEmpty()) {
                    password.setError("Please enter password");
                    password.requestFocus();
                } else if (!(emailTxt.isEmpty() && passwordTxt.isEmpty())){
                    // signs in and waits for auth, if error user is notified otherwise home act is started
                    mFirebaseAuth.signInWithEmailAndPassword(emailTxt, passwordTxt).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Login Error, please try again", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intToHome = new Intent(MainActivity.this,HomeActivity.class);
                                intToHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intToHome);
                            }
                        }
                    });
                }
                else{
                    // check any potental errors becasue data doesnt follow our conventions
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // listener for the signup switch button
        signupSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // switch to signup act
                Intent intSignUp = new Intent(MainActivity.this,SignupActivity.class);
                startActivity(intSignUp);
            }
        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
