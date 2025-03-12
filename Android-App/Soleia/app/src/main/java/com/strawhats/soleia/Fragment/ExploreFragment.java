package com.strawhats.soleia.Fragment;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.strawhats.soleia.Adapters.FeaturedEventsAdapter;
import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.R;
import com.strawhats.soleia.ViewModel.EventViewModel;
import com.strawhats.soleia.databinding.FragmentExploreBinding;

import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private EventViewModel eventViewModel;
    private List<String> selectedCategories = new ArrayList<>();
    private FeaturedEventsAdapter featuredEventsAdapter;
    private ChipGroup chipGroup; // Declare ChipGroup variable
    private String selectedCategory;  // Variable to hold the selected category name

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class); // Initialize ViewModel

        // Setup RecyclerView and Adapter
        RecyclerView recyclerView = binding.AllEventView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        featuredEventsAdapter = new FeaturedEventsAdapter(new ArrayList<>());
        recyclerView.setAdapter(featuredEventsAdapter);


        // Observe the filtered events based on selected category
        loadFilteredEvents(selectedCategory);
        chipGroup = binding.chipgroupcat; // Initialize ChipGroup
        // Load categories and display them as chips
        // Load categories and display them as chips
        eventViewModel.loadCatergory().observe(getViewLifecycleOwner(), new Observer<List<CategoryModel>>() {
            @Override
            public void onChanged(List<CategoryModel> categories) {
                if (categories != null) {
                    for (CategoryModel category : categories) {
                        Chip chip = new Chip(getContext());
                        chip.setText(category.getName()); // Set category name as chip text
                        chip.setCheckable(true);
                        chip.setChipStartPadding(20);
                        chip.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); // Set font style

                        // Load the image into the chip icon using Glide
                        String imageUrl = category.getPicture();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(imageUrl)
                                    .into(new SimpleTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                            chip.setChipIcon(resource);
                                        }
                                    });
                        }

                        // Add chip to ChipGroup
                        chipGroup.addView(chip);

                        // Set the chip click listener to handle selection
                        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                // Add category if not already present (to avoid duplicates)
                                if (!selectedCategories.contains(category.getName())) {
                                    selectedCategories.add(category.getName());
                                }
                            } else {
                                // Remove category when unchecked
                                selectedCategories.remove(category.getName());
                            }

                            // After update, remove duplicates if any
                            removeDuplicateCategories();

                            loadFilteredEvents(null);  // Reload events based on the updated selected categories
                        });

                        // Check if the chip matches the selected category and set it checked
                        if (category.getName().equals(selectedCategory)) {
                            chip.setChecked(true);  // If the passed category matches, set the chip checked
                        }
                    }
                }
            }

            private void removeDuplicateCategories() {
                List<String> uniqueCategories = new ArrayList<>();
                for (String category : selectedCategories) {
                    if (!uniqueCategories.contains(category)) {
                        uniqueCategories.add(category);
                    }
                }
                selectedCategories.clear();
                selectedCategories.addAll(uniqueCategories);
            }
        }); // Retrieve the passed category from the Bundle (if it exists)



        if (getArguments() != null) {
            Bundle args = getArguments(); // Store arguments in a variable for easier access

            if (args.containsKey("category")) { // Check if the key exists before trying to access it.
                selectedCategory = ((CategoryModel) getArguments().getSerializable("category")).getName();
                // Add the passed category if it's not already in the list
                if (!selectedCategories.contains(selectedCategory)) {
                    selectedCategories.add(selectedCategory);
                }
            }

            String searchQuery = args.getString("searchQuery");

            // Simplified searchQuery handling
            searchQuery = (searchQuery == null) ? "" : searchQuery; // Use ternary operator for conciseness
            loadFilteredEvents(searchQuery); // No need for separate if/else
        }

        // Method to remove duplicates from selectedCategories



        // Add TextWatcher to EditText to filter events as the user types
        binding.editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No need to implement
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Get the current search query and reload events
                String searchQuery = charSequence.toString().trim();
                loadFilteredEvents(searchQuery);  // Pass the query to load filtered events
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No need to implement
            }
        });

        // Add swipe refresh functionality
        binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Call the method to refresh the event data
                binding.editTextText.setText("");
                loadFilteredEvents(null); // Pass null to remove the search filter, refreshing all events
            }
        });

        // Button to get the current location and filter events
        binding.button.setOnClickListener(view -> {
            checkPermissions(); // Check if permission is granted to access location
        });

        // Initially load all events without category filter
        loadFilteredEvents(null);





        return binding.getRoot(); // Return the root view of the fragment
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getCurrentLocation();  // Permission granted, get current location
        }
    }

    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            loadFilteredEvents(latitude, longitude);  // Pass the current location to filter events
                            // Use Geocoder to get the location address
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String locationName = address.getAddressLine(0); // Get the full address line
                                  //  Toast.makeText(getContext(), "Current Location: " + locationName, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "Unable to get location name", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Geocoder failed", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getContext(), "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void loadFilteredEvents(double latitude, double longitude) {
        // Use the getFilteredEventsNearby method and pass the selected categories and the location
        eventViewModel.getFilteredEventsNearby(latitude, longitude, 5, selectedCategories, null)
                .observe(getViewLifecycleOwner(), new Observer<List<EventModel>>() {
                    @Override
                    public void onChanged(List<EventModel> eventModels) {
                        if (eventModels != null) {
                            featuredEventsAdapter.updateEvents(eventModels); // Update the adapter with the filtered events
                        }
                        binding.swiperefresh.setRefreshing(false); // Stop the refreshing animation
                    }
                });
    }

    private void loadFilteredEvents(String searchQuery) {
        // Use the getFilteredEventsNearby method and pass the selected categories as filters
        eventViewModel.getFilteredEventsNearby(0, 0, 100, selectedCategories, searchQuery)
                .observe(getViewLifecycleOwner(), new Observer<List<EventModel>>() {
                    @Override
                    public void onChanged(List<EventModel> eventModels) {
                        if (eventModels != null) {
                            featuredEventsAdapter.updateEvents(eventModels); // Update the adapter with the filtered events
                        }
                        binding.swiperefresh.setRefreshing(false); // Stop the refreshing animation
                    }
                });
    }
}
