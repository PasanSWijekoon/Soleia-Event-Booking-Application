package com.strawhats.soleia.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.strawhats.soleia.R;
import com.strawhats.soleia.Models.CustomToast;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField;
    private Button resetButton;
    private TextView backToSignIn;
    private ProgressDialog progressDialog;
    CustomToast customToast = new CustomToast(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailField = findViewById(R.id.emailReset);
        resetButton = findViewById(R.id.resetButton);
        backToSignIn = findViewById(R.id.backToSignIn);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending reset link...");
        progressDialog.setCancelable(false);

        // Set click listeners
        resetButton.setOnClickListener(v -> sendResetLink());
        backToSignIn.setOnClickListener(v -> finish());

    }

    private void sendResetLink() {
        String email = emailField.getText().toString().trim();

        if (email.isEmpty()) {
            emailField.setError("Email is required");
            customToast.showToast("Email is required", R.raw.warning);
            emailField.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Please enter a valid email");
            customToast.showToast("Please enter a valid email", R.raw.warning);
            emailField.requestFocus();
            return;
        }

        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        customToast.showToast("Password reset link has been sent to your email", R.raw.success);
                        finish();
                    } else {
                        customToast.showToast("Failed to send reset link. Please try again.", R.raw.failed);
                        finish();
                    }
                });
    }

}
