package com.strawhats.soleia.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.strawhats.soleia.Fragment.ExploreFragment;
import com.strawhats.soleia.Fragment.HomeFragment;
import com.strawhats.soleia.Fragment.ProfileFragment;
import com.strawhats.soleia.R;
import com.strawhats.soleia.Fragment.TicketsFragment;
import com.strawhats.soleia.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private TextView userEmailText, userNameText;
    private Button logoutButton;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        Map<String, Object> category = new HashMap<>();
//        category.put("Id",9);
//        category.put("Name","Adventure Events");
//        category.put("Picture","https://res.cloudinary.com/dlpgnolqp/image/upload/v1739288962/soleia/ub6mujw3dxv6bp8mf8k6.png");
//
//        db.collection("Category")
//                .add(category)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("Firestore", "Document successfully written!");
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Firestore", "Error writing document", e);
//                });

        replaFragment(new HomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId(); // Store the item ID to avoid repeated calls

            if (itemId == R.id.navigation_home) {
                replaFragment(new HomeFragment());
            } else if (itemId == R.id.navigation_explore) {
                replaFragment(new ExploreFragment());
            } else if (itemId == R.id.navigation_tickets) {
                replaFragment(new TicketsFragment());
            } else if (itemId == R.id.navigation_userprofile) {
                replaFragment(new ProfileFragment());
            }

          return true;
        });

//        mAuth = FirebaseAuth.getInstance();
//
//        // Configure Google Sign In (needed for sign out)
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        // Initialize views
//        userEmailText = findViewById(R.id.userEmail);
//        userNameText = findViewById(R.id.userName);
//        logoutButton = findViewById(R.id.logoutButton);
//
//        // Display user info
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user != null) {
//            userEmailText.setText("Email: " + user.getEmail());
//            userNameText.setText("Name: " + (user.getDisplayName() != null ?
//                    user.getDisplayName() : "N/A"));
//        }
//
//        logoutButton.setOnClickListener(v -> signOut());
//
    }

    private void replaFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainframe_layout,fragment);
        fragmentTransaction.commit();
    }



    private void signOut() {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                });
    }
}