package com.strawhats.soleia.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.strawhats.soleia.Activity.ShowNotificationActivity;
import com.strawhats.soleia.Activity.SignInActivity;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.Models.UserDatabaseHelper;
import com.strawhats.soleia.R;
import com.strawhats.soleia.ViewModel.MainViewModel;
import com.strawhats.soleia.databinding.FragmentHomeBinding;
import com.strawhats.soleia.databinding.FragmentOrganizerHomeBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class OrganizerHomeFragment extends Fragment {

    FragmentOrganizerHomeBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private UserDatabaseHelper dbHelper;

    private NotificationDatabaseHelper notificationDatabaseHelper;

    private GoogleSignInClient mGoogleSignInClient;
    private List<EventModel> eventModels = new ArrayList<>(); // Store events for filtering
    private SwipeRefreshLayout swipeRefreshLayout;  // Declare SwipeRefreshLayout
    private MainViewModel viewModel;

    private PieChart ticketSalesPieChart;
    private BarChart registeredUsersBarChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerHomeBinding.inflate(inflater, container, false);
        setGreetingMessage();

        // Set up SwipeRefreshLayout
        swipeRefreshLayout = binding.swiperefresh;  // Initialize the swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Call your refresh method here
            refreshData();
        });



        dbHelper = new UserDatabaseHelper(getContext());
        notificationDatabaseHelper = new NotificationDatabaseHelper(getContext());
        // Initialize Firebase
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        // Get and store FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved"))
                                .addOnFailureListener(e -> Log.e("FCM", "Token save failed", e));
                    } else {
                        Log.e("FCM", "Token retrieval failed", task.getException());
                    }
                });

        //Configure Google Sign In (needed for sign out)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Set placeholder name & hide profile section initially
        binding.organizerName.setText("Loading...");
        binding.imageView4.setVisibility(View.INVISIBLE); // Hide until loaded
        binding.organizerName.setVisibility(View.INVISIBLE); // Hide until loaded

        // Load user data from SQLite
        loadUserFromSQLite();
        binding.imageView5.setOnClickListener(v -> logoutUser());

        ImageView notificationIcon = binding.homelogout;
        // Set click listener on the ImageView
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), ShowNotificationActivity.class);
                startActivity(intent);
            }
        });

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.loadEventsByOrganizer(user.getUid()).observe(getViewLifecycleOwner(), events  -> {
            if (events  != null) {
                eventModels = events;  // Store original list of events
                updateUI();
            }
        });

        return binding.getRoot();
    }

    private void loadBarChartData() {
        // Create a list of BarEntries based on registered users
        List<BarEntry> barEntries = new ArrayList<>();
        int index = 0;
        for (EventModel event : eventModels) {
            barEntries.add(new BarEntry(index++, event.getAttendeesCount()));
        }

        // Create a BarDataSet and BarData
        BarDataSet dataSet = new BarDataSet(barEntries, "Registered Users by Event");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(dataSet);

        // Set data and customize chart
        registeredUsersBarChart.setData(barData);
        registeredUsersBarChart.getDescription().setEnabled(false);
        registeredUsersBarChart.animateY(1400);
    }


    private void loadPieChartData() {
        // Calculate the total ticket sales
        double totalSales = 0;
        for (EventModel event : eventModels) {
            totalSales += event.getTicketPrice() * event.getAttendeesCount();  // Assuming ticket sales = price * attendees
        }

        // Create a list of PieEntries
        List<PieEntry> pieEntries = new ArrayList<>();
        for (EventModel event : eventModels) {
            double eventSales = event.getTicketPrice() * event.getAttendeesCount();
            float percentage = (float) ((eventSales / totalSales) * 100);
            pieEntries.add(new PieEntry(percentage, event.getTitle()));
        }

        // Create a PieDataSet and PieData
        PieDataSet dataSet = new PieDataSet(pieEntries, "Ticket Sales by Event");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);

        // Set data and customize chart
        ticketSalesPieChart.setData(pieData);
        ticketSalesPieChart.setUsePercentValues(true);
        ticketSalesPieChart.getDescription().setEnabled(false);
        ticketSalesPieChart.setDrawHoleEnabled(true);
        ticketSalesPieChart.setHoleColor(R.color.maincolor);
        ticketSalesPieChart.setEntryLabelColor(R.color.brutal_red);
        ticketSalesPieChart.animateY(1400);
    }


    // Method to get the total count of events
    private int getTotalEventCount() {
        return eventModels.size();  // Return the size of the eventModels list
    }

    private double calculateTotalEarnings() {
        double totalEarnings = 0;
        for (EventModel event : eventModels) {
            totalEarnings += event.getTicketPrice();  // Assuming getPrice() returns the earnings for each event
        }
        return totalEarnings;
    }

    private int calculateApprovedEvents() {
        int approvedCount = 0;
        for (EventModel event : eventModels) {
            if ("approved".equals(event.getStatus())) {  // Assuming status field has values "approved" and "pending"
                approvedCount++;
            }
        }
        return approvedCount;
    }

    private int calculatePendingEvents() {
        int pendingCount = 0;
        for (EventModel event : eventModels) {
            if ("noapproved".equals(event.getStatus())) {  // Assuming status field has values "approved" and "pending"
                pendingCount++;
            }
        }
        return pendingCount;
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
                binding.organizerName.setText(name);

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
                applyFadeInAnimation(binding.organizerName);
            } else {
                Log.e("SQLite", "Columns not found in the cursor");
                binding.organizerName.setText("Unknown User");
                binding.imageView4.setImageResource(R.drawable.profile); // Fallback image
            }
        } else {
            Log.e("SQLite", "Cursor is null or empty");
            binding.organizerName.setText("No user data");
            binding.imageView4.setImageResource(R.drawable.profile); // Fallback image
        }
    }

    private void applyFadeInAnimation(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(fadeIn);
    }

    private void refreshData() {

        swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
        viewModel.loadEventsByOrganizer(user.getUid()).observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                eventModels = events;
                updateUI();
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void updateUI() {
        binding.totalEvents.setText(String.valueOf(getTotalEventCount()));
        binding.totalEarnings.setText("RS " + String.format("%.2f", calculateTotalEarnings()));
        binding.approvedEvents.setText(String.valueOf(calculateApprovedEvents()));
        binding.pendingEvents.setText(String.valueOf(calculatePendingEvents()));

        // Initialize the charts
        ticketSalesPieChart = binding.ticketSalesPieChart;
        registeredUsersBarChart = binding.registeredUsersBarChart;

        // Load data for Pie Chart
        loadPieChartData();

        // Load data for Bar Chart
        loadBarChartData();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important: Clear binding to prevent memory leaks
    }
}