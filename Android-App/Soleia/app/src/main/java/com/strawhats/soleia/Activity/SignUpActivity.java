package com.strawhats.soleia.Activity;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strawhats.soleia.R;
import com.strawhats.soleia.Models.CustomToast;
import com.strawhats.soleia.Models.UserDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {

    boolean isFLip;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailField, passwordField, confirmPasswordField, firstNameField, lastNameField;
    private Button signUpButton;
    CustomToast customToast = new CustomToast(this);

    private TextView signInRedirectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        emailField = findViewById(R.id.email);
        firstNameField = findViewById(R.id.FirstName);
        lastNameField = findViewById(R.id.LastName);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.signUpButton);
        signInRedirectButton = findViewById(R.id.signInRedirect);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float values[] = event.values;

                float z = values[2];

                if(z<-9 && !isFLip){
                    emailField.setText("");
                    firstNameField.setText("");
                    lastNameField.setText("");
                    passwordField.setText("");
                    confirmPasswordField.setText("");
                    isFLip = true;

                }

                if(z>9 && isFLip){

                    isFLip = false;
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = passwordField.getCompoundDrawables()[2] != null ? passwordField.getWidth() - passwordField.getPaddingEnd() - 80 : 0;
                if (event.getRawX() >= drawableEnd) {
                    if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                    } else {
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                    }
                    passwordField.setSelection(passwordField.getText().length());
                    return true;
                }
            }
            return false;
        });

        confirmPasswordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = confirmPasswordField.getCompoundDrawables()[2] != null ? confirmPasswordField.getWidth() - confirmPasswordField.getPaddingEnd() - 80 : 0;
                if (event.getRawX() >= drawableEnd) {
                    if (confirmPasswordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        confirmPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        confirmPasswordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                    } else {
                        confirmPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        confirmPasswordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                    }
                    confirmPasswordField.setSelection(confirmPasswordField.getText().length());
                    return true;
                }
            }
            return false;
        });

        signUpButton.setOnClickListener(v -> signUp());

        signInRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        });

    }

    private void signUp() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        // Validating fields
        if (firstName.isEmpty()) {
            showError(firstNameField, "First name is required", shake);
            return;
        }
        if (lastName.isEmpty()) {
            showError(lastNameField, "Last name is required", shake);
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(emailField, "Invalid email format", shake);
            return;
        }
        if (password.isEmpty() || password.length() < 8) {
            showError(passwordField, "Password must be at least 8 characters", shake);
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(passwordField, "Passwords do not match", shake);
            showError(confirmPasswordField, "Passwords do not match", shake);
            return;
        }

        // Register the user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " " + lastName)
                                    .build();
                            user.updateProfile(profileUpdates);
                            user.sendEmailVerification();

                            // Save user data to Firestore
                            saveUserToFirestore(user, firstName, lastName);
                        }
                    } else {
                        customToast.showToast("Sign-up failed", R.raw.failed);
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String firstName, String lastName) {
        String userId = user.getUid();

        // Create a map to store user data in Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("first_name", firstName);
        userData.put("last_name", lastName);
        userData.put("email", user.getEmail());
        userData.put("phone_number", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        userData.put("profile_picture", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("preferences", new ArrayList<>()); // ✅ Fixed: Use ArrayList instead of Array
        userData.put("registered_events", new HashMap<>()); // ✅ Empty events map
        userData.put("created_at", com.google.firebase.Timestamp.now());

        // Save the user data in Firestore under the 'users' collection
        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    customToast.showToast("Registered successfully! Please verify your email.", R.raw.success);
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user", e));
    }


    private void showError(EditText field, String message, Animation animation) {
        field.setError(message);
        field.startAnimation(animation);
        customToast.showToast(message, R.raw.warning);
        vibratePhone();
    }

    private void saveUserToSQLite(FirebaseUser user) {
        String name = user.getDisplayName() != null ? user.getDisplayName() : "Unknown";
        String email = user.getEmail() != null ? user.getEmail() : "No Email";
        String profilePic = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);
        long result = dbHelper.insertUser(name, email, profilePic);

        if (result != -1) {
            Log.d("SQLite", "User data saved successfully in SQLite!");
        } else {
            Log.e("SQLite", "Error saving user data in SQLite.");
        }
    }

    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500); // Vibrate for 500ms on older devices
            }
        }
    }
}