package com.strawhats.soleia.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strawhats.soleia.Activity.EditEventsActivity;
import com.strawhats.soleia.Activity.OraganizerMainActivity;
import com.strawhats.soleia.Adapters.CatergoryAdapter;
import com.strawhats.soleia.Adapters.FeaturedEventsAdapter;
import com.strawhats.soleia.Adapters.OrganizerEventAdapter;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.R;
import com.strawhats.soleia.ViewModel.MainViewModel;
import com.strawhats.soleia.databinding.FragmentOrganizerViewEventBinding;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrganizerViewEventFragment extends Fragment {

    private FragmentOrganizerViewEventBinding binding;
    private MainViewModel viewModel;
    private OrganizerEventAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private FirebaseUser user;

    private SwipeRefreshLayout swipeRefreshLayout;  // Declare SwipeRefreshLayout

    private List<EventModel> eventModels = new ArrayList<>(); // Store events for filtering

    private ActivityResultLauncher<Intent> editEventLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrganizerViewEventBinding.inflate(inflater, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        // Set up SwipeRefreshLayout
        swipeRefreshLayout = binding.swipeRefresh;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData();
        });

        editEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        refreshData();
                        Log.d("OrganizerViewEvent", "onActivityResult: Refreshing data");
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Log.d("OrganizerViewEvent", "onActivityResult: Not Refreshing data");
                    }
                }
        );

        viewModel.loadEventsByOrganizer(user.getUid()).observe(getViewLifecycleOwner(), events  -> {
            if (events  != null) {
                eventModels = events;  // Store original list of events
                updateRecyclerView(events);  // Display the initial list
                binding.loadingProgress.setVisibility(View.GONE);

            } else {

               binding.loadingProgress.setVisibility(View.GONE);
               binding.emptyStateLayout.setVisibility(View.VISIBLE);
               binding.ticketsRecyclerView.setVisibility(View.GONE);
            }
        });


        binding.browseEventsButton.setOnClickListener(v -> {
            // Programmatically select the "create event" item in the BottomNavigationView
            OraganizerMainActivity activity = (OraganizerMainActivity) getActivity();
            if (activity != null) {
                activity.selectBottomNavItem(R.id.navigation_create_event_organizer); // Call this new method
            } else {
                Log.e("OrganizerViewEvent", "Activity is null!"); // Handle the case where the activity is null.
            }

        });


        binding.searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    filterEvents(query.toString(), getSelectedStatus());
                }
                return true; // Consume the event
            }
            return false; // Let other listeners handle it
        });

        // Handle Search Input Changes
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterEvents(charSequence.toString(), getSelectedStatus());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Handle Chip Selections (Approved / Not Approved)
        binding.filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.approvedChip) {
                filterEvents(binding.searchEditText.getText().toString(), "approved");
            } else if (checkedId == R.id.notApprovedChip) {
                filterEvents(binding.searchEditText.getText().toString(), "noapproved");
            }
        });


        return binding.getRoot();
    }

    private void filterEvents(String searchText, String status) {
        List<EventModel> filteredList = new ArrayList<>();

        for (EventModel event : eventModels) {
            boolean matchesSearch = event.getTitle().toLowerCase().contains(searchText.toLowerCase());
            boolean matchesStatus = status.isEmpty() || event.getStatus().equalsIgnoreCase(status);

            if (matchesSearch && matchesStatus) {
                filteredList.add(event);
            }
        }

        updateRecyclerView(filteredList);
    }

    private void updateRecyclerView(List<EventModel> events) {
        if (adapter == null) {
            adapter = new OrganizerEventAdapter(events);
            binding.ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
            binding.ticketsRecyclerView.setAdapter(adapter);

            // Set the click listener *after* setting the adapter
            adapter.setOnItemClickListener(position -> {
                EventModel selectedEvent = eventModels.get(position);
                Intent intent = new Intent(requireContext(), EditEventsActivity.class);
                intent.putExtra("event", selectedEvent);
                editEventLauncher.launch(intent);
            });

        } else {
            adapter.updateEvents(events);
        }
    }

    private String getSelectedStatus() {
        // Check if the 'Approved' or 'Not Approved' chip is selected
        if (binding.approvedChip.isChecked()) {
            return "approved";
        } else if (binding.notApprovedChip.isChecked()) {
            return "noapproved";
        }
        return ""; // Return empty string if no chip is selected
    }

    private void refreshData() {
        binding.loadingProgress.setVisibility(View.VISIBLE); // Show loading indicator
        binding.emptyStateLayout.setVisibility(View.GONE);  // Hide empty state
        binding.ticketsRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView

        viewModel.loadEventsByOrganizer(user.getUid()).observe(getViewLifecycleOwner(), events -> { // Use a better variable name
            if (events != null) {
                eventModels = events; // Update the eventModels list
                updateRecyclerView(events); // Use the updateRecyclerView method
                binding.loadingProgress.setVisibility(View.GONE); // Hide loading indicator
                swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
            } else {
                binding.loadingProgress.setVisibility(View.GONE); // Hide loading indicator
                binding.emptyStateLayout.setVisibility(View.VISIBLE); // Show empty state
                binding.ticketsRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
                swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
            }
        });

        binding.searchEditText.setText(""); // Clear search text
    }



}
