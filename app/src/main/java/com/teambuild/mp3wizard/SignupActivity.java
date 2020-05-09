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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.teambuild.mp3wizard.ui.MainActivity;

import java.util.HashMap;
import java.util.Map;


public class SignupActivity extends AppCompatActivity {
    // interface var config (Declare)
    private EditText email, password, firstName, lastName, employeeId, confirmPassword, phoneNumber, companyId;
    private Button signup;
    private TextView loginSwitch;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // get the current firebase instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        // interface var config (instantiate)
        firstName = findViewById(R.id.firstNameText);
        lastName = findViewById(R.id.lastNameText);
        email = findViewById(R.id.emailText);
        password = findViewById(R.id.passwordText);
        confirmPassword = findViewById(R.id.confirmPasswordText);
        signup = findViewById(R.id.createButton);

        // to check for signup attempts
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // collect & store data inputs for usage
                Map<String, Object> userData = new HashMap<>();
                userData.put("email", email.getText().toString());
                userData.put("password", password.getText().toString());
                userData.put("firstName", firstName.getText().toString());
                userData.put("lastName", lastName.getText().toString());
                userData.put("confirmPassword", confirmPassword.getText().toString());
                boolean isValid = true; // stores the state of data
                // check if data/form is filled
                for (String key : userData.keySet()) {
                    String input = (String)userData.get(key);
                    if (input.isEmpty()) {
                        isValid = false;
                    }
                }
                // confirms the relationship between password and the confirmation password is 1
                if (!userData.get("password").equals(userData.get("confirmPassword"))) {
                    isValid = false;
                    confirmPassword.setError("Passwords Don't Match or Is Not Valid");
                    confirmPassword.requestFocus();
                }
                // moves to data send process/ account creation if data is valid
                // otherwise it notifies the user that their is a error in their input
                if (isValid) {
                    // This meathod manages the user creation proccess
                    createNewUser(userData);
                } else {
                    Toast.makeText(SignupActivity.this, "Error, all fields not filled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createNewUser(final Map<String, Object> userData){
        // creates users on firebase
        mFirebaseAuth.createUserWithEmailAndPassword(((String)userData.get("email")), ((String)userData.get("password"))).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
            // confirms that the signup was successful so that personal data can be stored in corisponding database
            // if signup was not success informers user that their was an error
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Signup unsuccessful, Try again", Toast.LENGTH_SHORT).show();
                } else {
                    sendNewUserData(userData, mFirebaseAuth);
                    // moves to main page
                    startActivity(new Intent(SignupActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
            }
        });
    }
    private void sendNewUserData(final Map<String, Object> userData, final FirebaseAuth mFirebaseAuth){
        // User id to store data under (unqiue id created by firebase)
        String userId = mFirebaseAuth.getCurrentUser().getUid();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        // configure location/dir for the data (called a collection path)
        DocumentReference mDocRef = fStore.collection("users").document(userId);
        // sends data and sets a listener to confirm success
        mDocRef.set(userData).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Didn't Work");
                // recursion if failure
                // else N/A
                sendNewUserData(userData, mFirebaseAuth);
            }
        });
    }
}
