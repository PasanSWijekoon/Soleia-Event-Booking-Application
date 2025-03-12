package com.strawhats.soleia.Activity;

import static com.strawhats.soleia.R.layout.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.strawhats.soleia.R;
import com.strawhats.soleia.Models.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Request no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Hide status bar & navigation bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |  // Hide navigation bar
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | // Prevents reappearing on touch
                        android.view.View.SYSTEM_UI_FLAG_FULLSCREEN         // Hide status bar
        );
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageView = findViewById(R.id.imageView);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // Start with Fade-In
        imageView.startAnimation(fadeIn);

        PrefManager prefManager = new PrefManager(this);



        // Delay, then start Fade-Out Animation
        new Handler().postDelayed(() -> {
            imageView.startAnimation(fadeOut);

            // Move to MainActivity after fade-out
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (prefManager.isFirstTimeLaunch()) {
                        startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                    } else {

                        // Initialize Firebase Auth
                        mAuth = FirebaseAuth.getInstance();
                        db = FirebaseFirestore.getInstance();


                        // Check if user is signed in
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (currentUser != null) {
                                if (currentUser.isEmailVerified()) { // Condition 2
                                    String userId = currentUser.getUid();
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
                                                            startActivity(new Intent(SplashActivity.this, OraganizerMainActivity.class));
                                                            finish();
                                                        } else {
                                                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                                            finish();
                                                        }
                                                    }
                                                } else {
                                                    Log.e("Firestore", "Error checking user existence", task.getException());
                                                }
                                            });
                                } else {

                                    startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                                    finish();
                                }

                        } else {
                            // No user is signed in, go to SignInActivity
                            startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                            finish();
                        }

                    }

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

        }, 2000); // 2 seconds delay before fade-out


    }
}