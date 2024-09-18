package com.example.messagingapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.messagingapp.model.user;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileSetUp extends AppCompatActivity {
    private String email;
    private FirebaseFirestore db;

    // Declaring UI elements
    private EditText nameEditText, BioEditText;          // Input fields for name and bio (username)
    private Button Finish;                               // Finish button
    private LottieAnimationView logoAnimation, logoAnimationLoading; // Lottie animations for profile and loading
    private TextView usernameAvailabilityTextView;       // TextView to display username availability

    private Handler handler = new Handler();
    private Runnable checkUsernameRunnable;
    private static final long DELAY_MILLIS = 500; // Delay in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display for a full immersive experience
        setContentView(R.layout.activity_profile_set_up); // Set the layout resource file

        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Initialize UI elements
        initializeViews();

        // Retrieve email from the previous intent
        Intent incomingIntent = getIntent();
        if (incomingIntent != null && incomingIntent.hasExtra("user_email")) {
            email = incomingIntent.getStringExtra("user_email");
            checkIfEmailExists(email);
        }

        // Set the TextWatcher to check username availability while typing
        BioEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Cancel any previous checkUsernameRunnable
                handler.removeCallbacks(checkUsernameRunnable);

                // Schedule a new checkUsernameRunnable
                checkUsernameRunnable = () -> {
                    String username = BioEditText.getText().toString().trim();
                    if (!username.isEmpty()) {
                        Log.d("ProfileSetUp", "Checking availability for username: " + username);
                        checkUsernameAvailability(username);
                    }
                };
                handler.postDelayed(checkUsernameRunnable, DELAY_MILLIS);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Set the click listener for the Finish button
        Finish.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String username = BioEditText.getText().toString().trim();

            // Input validation with constraints
            if (!validateFields(name, username)) {
                // Do nothing, errors will be shown on invalid fields
                return;
            }

            // Check if the username is available
            String availabilityStatus = usernameAvailabilityTextView.getText().toString();
            if (availabilityStatus.equals("Username not available")) {
                // Show a toast message and prevent submission
                Toast.makeText(ProfileSetUp.this, "Username is already taken. Please choose another one.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed to push data to the database
            checkUser();
        });
    }

    // Method to initialize all views
    private void initializeViews() {
        // Initializing EditText fields
        nameEditText = findViewById(R.id.nameEditText);  // Input field for name
        BioEditText = findViewById(R.id.BioEditText);    // Input field for bio (username)

        // Initializing Lottie animations
        logoAnimation = findViewById(R.id.logoAnimation); // Animation for profile (shown initially)
        logoAnimationLoading = findViewById(R.id.logoAnimationLoading); // Animation for loading (shown during loading)

        // Initializing Finish button
        Finish = findViewById(R.id.Finish); // Finish button to complete profile setup

        // Initializing TextView for username availability message
        usernameAvailabilityTextView = findViewById(R.id.usernameAvailabilityTextView); // TextView to show availability status for the username
    }

    // Method to validate Name and Username
    private boolean validateFields(String name, String username) {
        // Regex to check if the name/username starts with a letter
        String namePattern = "^[a-zA-Z].*";

        // Validate Name
        if (!name.matches(namePattern)) {
            nameEditText.setError("Name cannot start with a number or special character");
            return false;
        }
        if (name.length() > 20) {
            nameEditText.setError("Name cannot exceed 20 characters");
            return false;
        }

        // Validate Username
        if (!username.matches(namePattern)) {
            BioEditText.setError("Username cannot start with a number or special character");
            return false;
        }
        if (username.length() > 15) {
            BioEditText.setError("Username cannot exceed 15 characters");
            return false;
        }

        return true;
    }

    // Method to check if the email already exists in Firestore
    private void checkIfEmailExists(String email) {
        CollectionReference usersRef = db.collection("users");

        // Query Firestore for documents where the email matches
        usersRef.whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Email found in Firestore, fetch user data and populate fields
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            user existingUser = documentSnapshot.toObject(user.class);
                            if (existingUser != null) {
                                populateFields(existingUser);
                            }
                        } else {
                            // Email not found, allow user to input new data
                            enableUserInput();
                        }
                    } else {
                        Toast.makeText(ProfileSetUp.this, "Error checking email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileSetUp.this, "Failed to check email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to populate fields if user exists
    private void populateFields(user existingUser) {
        nameEditText.setText(existingUser.getName());
        BioEditText.setText(existingUser.getUsername());
        BioEditText.setEnabled(false); // Make the username uneditable
        usernameAvailabilityTextView.setText("Username cannot be changed");
        usernameAvailabilityTextView.setTextColor(Color.GRAY); // Set text color to gray
    }

    // Method to allow new user input if email doesn't exist
    private void enableUserInput() {
        BioEditText.setEnabled(true); // Allow user to input username
    }

    // Method to check username availability in Firestore
    // Method to check username availability in Firestore
    private void checkUsernameAvailability(String username) {
        CollectionReference usersRef = db.collection("users");

        // Query the users collection to check if the username exists
        usersRef.whereEqualTo("username", username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Check if the found username belongs to the current user
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String foundUserEmail = documentSnapshot.getString("email");

                            if (foundUserEmail.equals(email)) {
                                // Username belongs to the current user, so it's fine
                                usernameAvailabilityTextView.setText("Username cannot be changed");
                                usernameAvailabilityTextView.setTextColor(Color.GRAY); // Set text color to gray
                            } else {
                                // Username is already taken by another user
                                usernameAvailabilityTextView.setText("Username not available");
                                usernameAvailabilityTextView.setTextColor(Color.RED); // Set text color to red
                            }
                        } else {
                            // Username is available
                            usernameAvailabilityTextView.setText("Username available");
                            usernameAvailabilityTextView.setTextColor(Color.GREEN); // Set text color to green
                        }
                    } else {
                        Toast.makeText(ProfileSetUp.this, "Error checking username", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileSetUp.this, "Failed to check username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // Method to handle database updates or insertions
// Method to handle database updates or insertions
    private void checkUser() {
        String name = nameEditText.getText().toString().trim();
        String username = BioEditText.getText().toString().trim();

        CollectionReference usersRef = db.collection("users");

        // Check if the email already exists
        usersRef.whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Update existing user
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        DocumentReference userDocRef = documentSnapshot.getReference();
                        userDocRef.update("name", name, "username", username)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ProfileSetUp.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    navigateToHome();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfileSetUp.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Create a new user document with the username as the document ID
                        DocumentReference userDocRef = usersRef.document(username);
                        user newUser = new user(name, username, email);
                        userDocRef.set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ProfileSetUp.this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                                    navigateToHome();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfileSetUp.this, "Failed to create profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }


    // Method to navigate to the home activity
    private void navigateToHome() {
        Intent intent = new Intent(ProfileSetUp.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
