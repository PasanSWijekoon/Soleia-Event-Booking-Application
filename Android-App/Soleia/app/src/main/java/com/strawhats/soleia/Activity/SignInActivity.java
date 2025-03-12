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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.strawhats.soleia.R;
import com.strawhats.soleia.Models.CustomToast;
import com.strawhats.soleia.Models.UserDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText emailField, passwordField;
    private Button signInButton ;
    private SignInButton googleSignInButton;

    private TextView forgotPasswordLink;

    boolean isFLip;

    private FirebaseFirestore db;

    CustomToast customToast = new CustomToast(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(com.strawhats.soleia.R.layout.activity_sign_in);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.strawhats.soleia.R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



        db = FirebaseFirestore.getInstance();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            if (currentUser.isEmailVerified()) { // Condition 2
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
            }
        }



        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.strawhats.soleia.R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        emailField = findViewById(com.strawhats.soleia.R.id.email);
        passwordField = findViewById(com.strawhats.soleia.R.id.password);
        signInButton = findViewById(com.strawhats.soleia.R.id.emailSignInButton);
        googleSignInButton = findViewById(com.strawhats.soleia.R.id.googleSignInButton);
        TextView signUpRedirectButton = findViewById(com.strawhats.soleia.R.id.signUpRedirect);


        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float values[] = event.values;

                float z = values[2];

                if(z<-9 && !isFLip){
                    emailField.setText("");
                    passwordField.setText("");
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
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, com.strawhats.soleia.R.drawable.baseline_visibility_24, 0);
                    } else {
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, com.strawhats.soleia.R.drawable.baseline_visibility_off_24, 0);
                    }
                    passwordField.setSelection(passwordField.getText().length());
                    return true;
                }
            }
            return false;
        });

        // Email/Password sign in
        signInButton.setOnClickListener(v -> signInWithEmailPassword());

        // Google sign in
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Redirect to sign up
        signUpRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            finish();
        });

        // Add this to your existing view initializations
        forgotPasswordLink = findViewById(com.strawhats.soleia.R.id.forgotPasswordLink);

        // Add this to your existing click listeners
        forgotPasswordLink.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class))
        );




    }



    private void signInWithEmailPassword() {

        Animation shake = AnimationUtils.loadAnimation(this, com.strawhats.soleia.R.anim.fade_in);

        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (email.isEmpty()) {
            emailField.setError("Email is required");
            emailField.startAnimation(shake);
            vibratePhone();


            customToast.showToast("Email is Required", com.strawhats.soleia.R.raw.warning);


            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format");
            emailField.startAnimation(shake);
            customToast.showToast("Invalid email format", com.strawhats.soleia.R.raw.warning);
            vibratePhone();
            return;
        }

        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            passwordField.startAnimation(shake);
            customToast.showToast("Password is required", com.strawhats.soleia.R.raw.warning);
            vibratePhone();
            return;
        }


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        assert user != null;
                        if (user.isEmailVerified()) {
                            saveUserToSQLite(user);
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        } else {
                            customToast.showToast("Please verify your email!", com.strawhats.soleia.R.raw.warning);
                        }
                    } else {
                        customToast.showToast("Login failed!", com.strawhats.soleia.R.raw.failed);
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                customToast.showToast("Google sign in failed!", com.strawhats.soleia.R.raw.failed);
            }
        }
    }

    // Firebase Authentication with Google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            saveUserToFirestore(user);
                        }
                    } else {
                        customToast.showToast("Authentication failed.", R.raw.failed);
                    }
                });
    }

    // Save user data to Firestore after successful Google sign-in
    private void saveUserToFirestore(FirebaseUser user) {
        String userId = user.getUid();

        // Reference to the Firestore collection
        DocumentReference userRef = db.collection("users").document(userId);

        // Check if the user exists
        userRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Document exists, retrieve the status field
                            String status = document.getString("status");

                            if ("organizer".equals(status)) {
                                startActivity(new Intent(SignInActivity.this, OraganizerMainActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                finish();
                            }

                            // Save existing user data to SQLite
                            saveUserToSQLite(document);
                        } else {
                            // User does not exist, create new user
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("first_name", user.getDisplayName());
                            userData.put("last_name", ""); // You can leave this empty or get it from Google Sign-In if available
                            userData.put("email", user.getEmail());
                            userData.put("phone_number", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                            userData.put("profile_picture", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                            userData.put("preferences", new ArrayList<>()); // Use ArrayList instead of Array
                            userData.put("registered_events", new HashMap<>()); // Empty events map
                            userData.put("created_at", com.google.firebase.Timestamp.now()); // Adding a creation timestamp

                            // Save new user document
                            userRef.set(userData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error saving user", e));

                            // Save the new user data to SQLite
                            saveUserToSQLite(user);
                        }
                    } else {
                        Log.e("Firestore", "Error checking user existence", task.getException());
                    }
                });
    }

    private void saveUserToSQLite(DocumentSnapshot document) {
        String firstName = document.getString("first_name") != null ? document.getString("first_name") : "Unknown";
        String lastName = document.getString("last_name") != null ? document.getString("last_name") : "";
        String name = firstName + " " + lastName;
        String email = document.getString("email") != null ? document.getString("email") : "No Email";
        String profilePic = document.getString("profile_picture") != null ? document.getString("profile_picture") : "";

        UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);
        long result = dbHelper.insertUser(name, email, profilePic);

        if (result != -1) {
            Log.d("SQLite", "User data saved successfully in SQLite!");
        } else {
            Log.e("SQLite", "Error saving user data in SQLite.");
        }
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

    // Phone vibration for feedback
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