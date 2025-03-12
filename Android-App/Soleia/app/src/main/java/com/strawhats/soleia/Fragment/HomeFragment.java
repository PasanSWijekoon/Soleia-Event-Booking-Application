package com.strawhats.soleia.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strawhats.soleia.Activity.DetailActivity;
import com.strawhats.soleia.Activity.ShowNotificationActivity;
import com.strawhats.soleia.Activity.SignInActivity;
import com.strawhats.soleia.Adapters.CatergoryAdapter;
import com.strawhats.soleia.Adapters.FeaturedEventsAdapter;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.R;
import com.strawhats.soleia.ViewModel.MainViewModel;
import com.strawhats.soleia.databinding.FragmentHomeBinding;
import com.strawhats.soleia.Models.UserDatabaseHelper;

import java.util.Calendar;
import java.util.List;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; // Use FragmentHomeBinding
    private MainViewModel viewModel;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private UserDatabaseHelper dbHelper;

    private GoogleSignInClient mGoogleSignInClient;

    private SwipeRefreshLayout swipeRefreshLayout;  // Declare SwipeRefreshLayout

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout using the binding class
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot(); // Get the root view
        setGreetingMessage();

        // Set up SwipeRefreshLayout
        swipeRefreshLayout = binding.swiperefresh;  // Initialize the swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Call your refresh method here
            refreshData();
        });

        ImageView notificationIcon = binding.homelogout;
        // Set click listener on the ImageView
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), ShowNotificationActivity.class);
                startActivity(intent);
            }
        });

        binding.textView6.setOnClickListener(view1 -> {


            // Navigate to ExploreFragment with the search query
            ExploreFragment exploreFragment = new ExploreFragment();

            // Begin the fragment transaction
            getFragmentManager().beginTransaction()
                    .replace(R.id.mainframe_layout, exploreFragment)
                    .addToBackStack(null) // Add to backstack if needed
                    .commit();

        });


        dbHelper = new UserDatabaseHelper(getContext());


        EditText editText = binding.editTextText;

        // Set an OnTouchListener to detect drawable end clicks
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Check if the event is at the drawable end area
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Drawable drawable = editText.getCompoundDrawables()[2]; // Get drawable at the end (index 2)
                    if (drawable != null) {
                        int drawableWidth = drawable.getBounds().width();
                        float touchX = event.getX();
                        if (touchX >= (editText.getWidth() - drawableWidth)) {
                            // Drawable end was clicked, show a Toast
                            String searchQuery = binding.editTextText.getText().toString().trim();

                            if (!searchQuery.isEmpty()) {
                                // Pass the search query to ExploreFragment
                                Bundle bundle = new Bundle();
                                bundle.putString("searchQuery", searchQuery);

                                // Navigate to ExploreFragment with the search query
                                ExploreFragment exploreFragment = new ExploreFragment();
                                exploreFragment.setArguments(bundle);

                                // Begin the fragment transaction
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.mainframe_layout, exploreFragment)
                                        .addToBackStack(null) // Add to backstack if needed
                                        .commit();
                            }
                            return true; // Return true to consume the touch event
                        }
                    }
                }
                return false; // Return false to let the event propagate to other listeners
            }
        });


        // Inside HomeFragment
        binding.editTextText.setOnEditorActionListener((v, actionId, event) -> {
            // Check if the action is "Done" (Enter key press)
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Capture the search query when the Enter key is pressed
                String searchQuery = binding.editTextText.getText().toString().trim();

                if (!searchQuery.isEmpty()) {
                    // Pass the search query to ExploreFragment
                    Bundle bundle = new Bundle();
                    bundle.putString("searchQuery", searchQuery);

                    // Navigate to ExploreFragment with the search query
                    ExploreFragment exploreFragment = new ExploreFragment();
                    exploreFragment.setArguments(bundle);

                    // Begin the fragment transaction
                    getFragmentManager().beginTransaction()
                            .replace(R.id.mainframe_layout, exploreFragment)
                            .addToBackStack(null) // Add to backstack if needed
                            .commit();
                }

                // Return true to indicate that the action has been handled
                return true;
            }

            // Return false if the action is not "Done"
            return false;
        });


        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

         //Configure Google Sign In (needed for sign out)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);


        // Set placeholder name & hide profile section initially
        binding.textView2.setText("Loading...");
        binding.imageView4.setVisibility(View.INVISIBLE); // Hide until loaded
        binding.textView2.setVisibility(View.INVISIBLE); // Hide until loaded

        // Load user data from SQLite
        loadUserFromSQLite();

        // Load user profile data;
        // loadUserProfile();

        // Handle logout button click
        binding.imageView5.setOnClickListener(v -> logoutUser());


        // Inflate the layout for this fragment
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.progressBarCat.setVisibility(View.VISIBLE);

        viewModel.loadCatergory().observe(getViewLifecycleOwner(), categoryModels -> {
            if (categoryModels != null) {
                binding.catView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)); // Corrected line
                binding.catView.setAdapter(new CatergoryAdapter(categoryModels));
                binding.progressBarCat.setVisibility(View.GONE);
            } else {
                binding.progressBarCat.setVisibility(View.GONE);
            }
        });


        binding.progressBarFeaturedEV.setVisibility(View.VISIBLE);
        viewModel.loadEvents().observe(getViewLifecycleOwner(), eventModels -> {
            if (eventModels != null) {
                binding.FeaturedEventView.setLayoutManager(
                        new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                );

                binding.FeaturedEventView.setAdapter(new FeaturedEventsAdapter(eventModels));
                binding.progressBarFeaturedEV.setVisibility(View.GONE);
            } else {
                binding.progressBarFeaturedEV.setVisibility(View.GONE);
            }
        });

        return view;


    }



    private void setGreetingMessage() {
        TextView greetingTextView = binding.textView; // Reference to TextView
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // Get current hour

        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning ☀️";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon 🌤️";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening 🌆";
        } else {
            greeting = "Good Night 🌙";
        }

        greetingTextView.setText(greeting);
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("first_name");
                    String lastName = documentSnapshot.getString("last_name");
                    String profilePicture = documentSnapshot.getString("profile_picture");

                    // Set user name
                    binding.textView2.setText(firstName + " " + lastName);

                    // Load profile image using Glide
                    if (profilePicture != null && !profilePicture.isEmpty()) {
                        Glide.with(this)
                                .load(profilePicture)
                                .circleCrop()
                                .into(binding.imageView4);
                    }
                    else {
                        binding.imageView4.setImageResource(R.drawable.profile); // Fallback image
                    }

                    // Apply fade-in animation
                    applyFadeInAnimation(binding.imageView4);
                    applyFadeInAnimation(binding.textView2);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadUserFromSQLite() {
        Cursor cursor = dbHelper.getUserData();

        if (cursor != null && cursor.moveToFirst()) {
            // Safely retrieve the column indexes
            int nameIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_NAME);
            int profilePicIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_PROFILE_PIC);

            // Check if the columns exist
            if (nameIndex != -1 && profilePicIndex != -1) {
                String name = cursor.getString(nameIndex);
                String profilePic = cursor.getString(profilePicIndex);

                // Set user name
                binding.textView2.setText(name);

                // Load profile image using Glide
                if (profilePic != null && !profilePic.isEmpty()) {
                    Glide.with(this)
                            .load(profilePic)
                            .circleCrop()
                            .into(binding.imageView4);
                } else {
                    binding.imageView4.setImageResource(R.drawable.profile); // Fallback image
                }
                applyFadeInAnimation(binding.imageView4);
                applyFadeInAnimation(binding.textView2);
            } else {
                Log.e("SQLite", "Columns not found in the cursor");
                binding.textView2.setText("Unknown User");
                binding.imageView4.setImageResource(R.drawable.profile); // Fallback image
            }
        } else {
            Log.e("SQLite", "Cursor is null or empty");
            binding.textView2.setText("No user data");
            binding.imageView4.setImageResource(R.drawable.profile); // Fallback image
        }
    }


    private void applyFadeInAnimation(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(fadeIn);
    }

    // Refresh data when swipe-to-refresh is triggered
    private void refreshData() {
        // Reload data from the ViewModel or network/database
        viewModel.loadCatergory().observe(getViewLifecycleOwner(), categoryModels -> {
            if (categoryModels != null) {
                binding.catView.setAdapter(new CatergoryAdapter(categoryModels));
            }
        });

        viewModel.loadEvents().observe(getViewLifecycleOwner(), eventModels -> {
            if (eventModels != null) {
                binding.FeaturedEventView.setAdapter(new FeaturedEventsAdapter(eventModels));
            }
        });

        binding.editTextText.setText("");

        // Stop refreshing animation
        swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
    }

    private void logoutUser() {
        mAuth.signOut();

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // Remove user data from SQLite
            UserDatabaseHelper dbHelper = new UserDatabaseHelper(getContext());
            dbHelper.deleteUserData();

            NotificationDatabaseHelper notificationDatabaseHelper1 = new NotificationDatabaseHelper(getContext());
            notificationDatabaseHelper1.clearNotifications();

            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), SignInActivity.class));
            getActivity().finish();
        });
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}