package com.strawhats.soleia.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.strawhats.soleia.Activity.MainActivity;
import com.strawhats.soleia.Adapters.UserTicketAdapter;
import com.strawhats.soleia.Domain.TicketModel;

import com.strawhats.soleia.R;
import com.strawhats.soleia.ViewModel.MainViewModel;
import com.strawhats.soleia.databinding.FragmentTicketsBinding;

import java.util.ArrayList;
import java.util.List;

public class TicketsFragment extends Fragment {

    private FragmentTicketsBinding binding;
    private UserTicketAdapter ticketAdapter;
    private MainViewModel viewModel;
    private List<TicketModel> ticketList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketsBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        // Initialize RecyclerView
        binding.ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ticketAdapter = new UserTicketAdapter(ticketList);
        binding.ticketsRecyclerView.setAdapter(ticketAdapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Get logged-in user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId != null) {
            // Show loading state
            binding.loadingProgress.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.ticketsRecyclerView.setVisibility(View.GONE);


            // Observe ticket data and update RecyclerView
            viewModel.loadUserTickets(userId).observe(getViewLifecycleOwner(), tickets -> {

                binding.loadingProgress.setVisibility(View.GONE); // Hide loading indicator

                if (tickets != null && !tickets.isEmpty()) {
                    ticketAdapter.updateTickets(tickets);
                    binding.ticketsRecyclerView.setVisibility(View.VISIBLE);
                    binding.emptyStateLayout.setVisibility(View.GONE);


                } else {

                    binding.ticketsRecyclerView.setVisibility(View.GONE);
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        // Swipe-to-refresh functionality
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (userId != null) {
                viewModel.loadUserTickets(userId).observe(getViewLifecycleOwner(), tickets -> {
                    binding.swipeRefresh.setRefreshing(false); // Stop refreshing animation

                    if (tickets != null && !tickets.isEmpty()) {
                        ticketAdapter.updateTickets(tickets);
                        binding.ticketsRecyclerView.setVisibility(View.VISIBLE);
                        binding.emptyStateLayout.setVisibility(View.GONE);

                    } else {
                        binding.ticketsRecyclerView.setVisibility(View.GONE);
                        binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        // Handle "Browse Events" button click
        binding.browseEventsButton.setOnClickListener(v -> {
            // Navigate to ExploreFragment
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ExploreFragment exploreFragment = new ExploreFragment();
            fragmentTransaction.replace(R.id.mainframe_layout, exploreFragment);
            fragmentTransaction.addToBackStack(null); // Optional: for back navigation
            fragmentTransaction.commit();

            // Update BottomNavigationView icon
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.binding.bottomNavigationView.setSelectedItemId(R.id.navigation_explore);
            }

            // Optional: Show toast for feedback (can be removed after testing)
           // Toast.makeText(getContext(), "Browse Events Clicked", Toast.LENGTH_SHORT).show();
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
