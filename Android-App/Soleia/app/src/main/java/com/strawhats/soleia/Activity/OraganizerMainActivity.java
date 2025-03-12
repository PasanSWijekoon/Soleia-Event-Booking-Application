package com.strawhats.soleia.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.strawhats.soleia.Fragment.ExploreFragment;
import com.strawhats.soleia.Fragment.HomeFragment;
import com.strawhats.soleia.Fragment.OrganizerCreateEventFragment;
import com.strawhats.soleia.Fragment.OrganizerHomeFragment;
import com.strawhats.soleia.Fragment.OrganizerProfileFragment;
import com.strawhats.soleia.Fragment.OrganizerViewEventFragment;
import com.strawhats.soleia.Fragment.ProfileFragment;
import com.strawhats.soleia.Fragment.TicketsFragment;
import com.strawhats.soleia.R;
import com.strawhats.soleia.databinding.ActivityOraganizerMainBinding;

public class OraganizerMainActivity extends AppCompatActivity {

    ActivityOraganizerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOraganizerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationViewOrganizer.setBackground(null);

        replaFragment(new OrganizerHomeFragment());
        binding.bottomNavigationViewOrganizer.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId(); // Store the item ID to avoid repeated calls

            if (itemId == R.id.navigation_home_organizer) {
                replaFragment(new OrganizerHomeFragment());
            } else if (itemId == R.id.navigation_create_event_organizer) {
                replaFragment(new OrganizerCreateEventFragment());
            } else if (itemId == R.id.navigation_view_events) {
                replaFragment(new OrganizerViewEventFragment());
            } else if (itemId == R.id.navigation_userprofile_organizer) {
                replaFragment(new ProfileFragment());
            }

            return true;
        });

        binding.faboranizer.setOnClickListener(view -> {
            Log.i("test","test");
            Intent i = new Intent(OraganizerMainActivity.this,ScanTicketActivity.class);
            startActivity(i);
        });
    }

    private void replaFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainframe_layout_Organizer, fragment);
        fragmentTransaction.commit();
    }

    public void selectBottomNavItem(int itemId) {
        binding.bottomNavigationViewOrganizer.setSelectedItemId(itemId);
    }
}