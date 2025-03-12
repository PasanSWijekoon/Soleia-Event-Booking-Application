package com.strawhats.soleia.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strawhats.soleia.Activity.WebViewActivity;
import com.strawhats.soleia.Models.UserDatabaseHelper;
import com.strawhats.soleia.R;
import com.strawhats.soleia.databinding.FragmentProfileBinding;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserDatabaseHelper dbHelper;
    private static final int PICK_IMAGE_REQUEST = 1;  // Constant for identifying the image pick request
    private static final int UCROP_REQUEST_CODE = 2;
    private Cloudinary cloudinary;
    @Override
    public void onResume() {
        super.onResume();

        String[] cities = getResources().getStringArray(R.array.city);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), R.layout.drpdownitem, cities);

        binding.usercity.setAdapter(arrayAdapter);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Initialize Cloudinary with your credentials
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "cloudname",
                "api_key", "mmmm",
                "api_secret", "pmmmmmmm"
        ));




        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new UserDatabaseHelper(getContext());
        FirebaseUser user = mAuth.getCurrentUser();
        loadUserFromSQLite();
        loadUserDataFromFirebase(user);

        binding.editProfileButton.setOnClickListener(view -> {
            updateUserDataToFirebase();
        });


        binding.profileImage.setOnClickListener(view -> {
            chooseProfilePicture();  // This will open the image picker
        });

        LinearLayout aboutAppLayout = binding.aboutAppLayout; // Assign IDs in XML
        LinearLayout privacyPolicyLayout = binding.privacyPolicyLayout;
        LinearLayout termsLayout = binding.termsLayout;

        aboutAppLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://project-soleia-web.vercel.app/", "About This Project");
            }
        });

        privacyPolicyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://policies.google.com/privacy?hl=en-US#intro", "Privacy Policy");
            }
        });

        termsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://policies.google.com/terms?hl=en-US", "Terms of Service");
            }
        });


        return binding.getRoot();
    }
    private void openWebView(String url , String title) {
        Intent intent = new Intent(getContext(), WebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
    }
    private void updateUserDataToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("Update Profile", "User not logged in.");
            return;
        }

        String userId = user.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        // 1. Get current data from Firestore *before* updating
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentPhone = documentSnapshot.getString("phone_number");
                String currentAddress = documentSnapshot.getString("address");
                String currentCity = documentSnapshot.getString("city");
                String currentCountry = documentSnapshot.getString("country");
                String currentFirstName = documentSnapshot.getString("first_name");
                String currentLastName = documentSnapshot.getString("last_name");


                // 2. Get updated values from input fields
                String updatedPhone = binding.userphoneno.getText().toString().trim();
                String updatedAddress = binding.useraddress.getText().toString().trim();
                String updatedCity = binding.usercity.getText().toString().trim();
                //String updatedCountry = binding.usercountry.getText().toString().trim();
                String updatedFirstName = binding.userfirstname.getText().toString().trim();
                String updatedLastName = binding.userlastname.getText().toString().trim();
                String UpdatedCountrycode = binding.ccp.getSelectedCountryNameCode().toString().trim();

                Log.i("logd",UpdatedCountrycode);

                // Phone number validation (adjust regex as needed)
                String phoneRegex = "^[0]{1}[7]{1}[01245678]{1}[0-9]{7}$";
                if (!updatedPhone.isEmpty() && !updatedPhone.matches(phoneRegex)) {
                    binding.userphoneno.setError("Invalid phone number");
                    return;
                } else {
                    binding.userphoneno.setError(null);
                }

                // 3. Compare current and updated values and build updatedData map
                Map<String, Object> updatedData = new HashMap<>();

                if (!updatedPhone.equals(currentPhone) && !updatedPhone.isEmpty() && updatedPhone.matches(phoneRegex)) {
                    updatedData.put("phone_number", updatedPhone);
                }
                if (!updatedAddress.equals(currentAddress) && !updatedAddress.isEmpty()) {
                    updatedData.put("address", updatedAddress);
                }
                if (!updatedCity.equals(currentCity) && !updatedCity.isEmpty()) {
                    updatedData.put("city", updatedCity);
                }
                if (!UpdatedCountrycode.equals(currentCountry) && !UpdatedCountrycode.isEmpty()) {
                    updatedData.put("country", UpdatedCountrycode);
                }
                if (!updatedFirstName.equals(currentFirstName) && !updatedFirstName.isEmpty()) {
                    updatedData.put("first_name", updatedFirstName);
                }
                if (!updatedLastName.equals(currentLastName) && !updatedLastName.isEmpty()) {
                    updatedData.put("last_name", updatedLastName);
                }


                // 4. Check if any data has been updated
                if (updatedData.isEmpty()) {
                    Toast.makeText(getContext(), "No changes to update.", Toast.LENGTH_SHORT).show();
                    return; // No need to call Firestore if nothing changed
                }

                // 5. Update Firestore document
                userRef.update(updatedData)
                        .addOnSuccessListener(aVoid -> {
                            // Clear focus from EditTexts after successful update
                            clearFocusFromEditTexts();
                            updateUserNameInSQLite(updatedFirstName + " " + updatedLastName);
                            binding.profileName.setText(updatedFirstName + " " + updatedLastName);
                            Log.d("Update Profile", "User profile updated successfully.");
                            Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Update Profile", "Failed to update profile", e);
                            Toast.makeText(getContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.d("Firestore", "No such document");
                Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error getting document", e);
            Toast.makeText(getContext(), "Error fetching user data.", Toast.LENGTH_SHORT).show();
        });
    }
    private void clearFocusFromEditTexts() {
        binding.userphoneno.clearFocus();
        binding.useraddress.clearFocus();
        binding.usercity.clearFocus();
        //binding.usercountry.clearFocus();
        binding.userfirstname.clearFocus();
        binding.userlastname.clearFocus();
    }
    private void setCityInDropdown(String userCity) {
        // Step 4: Find the city in the list and set it as selected
        String[] cities = getResources().getStringArray(R.array.city);
        for (String city : cities) {
            if (city.equals(userCity)) {
                binding.usercity.setText(city, false); // Set the text of the dropdown without triggering a search
                break;
            }
        }
    }
    private void loadUserDataFromFirebase(FirebaseUser user) {
        String userId = user.getUid();

        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Retrieve all necessary user data

                    String Phone = document.getString("phone_number"); // Assuming you store the image URL
                    String Address = document.getString("address");
                    String City = document.getString("city");
                    String Country = document.getString("country");
                    String firstname = document.getString("first_name");
                    String lastname = document.getString("last_name");
                    String email = document.getString("email");


                    // Update UI elements
                    binding.userphoneno.setText(Phone);
                    binding.useraddress.setText(Address);
                    binding.userfirstname.setText(firstname);
                    binding.userlastname.setText(lastname);
                    binding.ccp.setCountryForNameCode(Country);
                    setCityInDropdown(City);


                } else {
                    Log.d("Firestore", "No such document");
                    // Handle case where user data doesn't exist
                }
            } else {
                Log.e("Firestore", "Error getting user data", task.getException());
                // Handle error
            }
        });
    }
    private void applyFadeInAnimation(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(fadeIn);
    }
    private void loadUserFromSQLite() {
        Cursor cursor = dbHelper.getUserData();

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Safely retrieve the column indexes
                    int nameIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_NAME);
                    int profilePicIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_PROFILE_PIC);
                    int emailIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_EMAIL);

                    // Check if ALL necessary columns exist.  This is the key improvement.
                    if (nameIndex != -1 && profilePicIndex != -1 && emailIndex != -1) {
                        String name = cursor.getString(nameIndex);
                        String profilePic = cursor.getString(profilePicIndex);
                        String email = cursor.getString(emailIndex);

                        // Set user name
                        binding.profileName.setText(name);
                        binding.profileEmail.setText(email);

                        // Load profile image using Glide
                        if (profilePic != null && !profilePic.isEmpty()) {
                            Glide.with(this)
                                    .load(profilePic)
                                    .circleCrop()
                                    .into(binding.profileImage);
                        } else {
                            binding.profileImage.setImageResource(R.drawable.profile); // Fallback image
                        }
                        applyFadeInAnimation(binding.profileImage);
                        applyFadeInAnimation(binding.profileName);
                    } else {
                        Log.e("SQLite", "Not all required columns found in the cursor");
                        handleMissingUserData();
                    }
                } else {
                    Log.e("SQLite", "No user data found");
                    handleMissingUserData();
                }
            } finally {
                cursor.close(); // Important: Close the cursor in a finally block
            }
        } else {
            Log.e("SQLite", "Cursor is null");
            handleMissingUserData();
        }
    }
    private void handleMissingUserData() {
        binding.profileName.setText("Unknown User"); // Or appropriate default
        binding.profileEmail.setText(""); // Clear email if no data
        binding.profileImage.setImageResource(R.drawable.profile); // Fallback image
    }
    // Method to launch the image picker
    private void chooseProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    Uri imageuri;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri sourceUri = data.getData(); // The selected image URI
            imageuri = data.getData();

            // Destination URI (where the cropped image will be saved)
            Uri destinationUri = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped_profile.jpg"));

            // Start uCrop for cropping
            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1) // Make it a square crop
                    .withMaxResultSize(512, 512) // Set max size for the cropped image
                    .start(getActivity(), this, UCROP_REQUEST_CODE);
        }

        // Handle the cropped image result
        else if (requestCode == UCROP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri croppedImageUri = UCrop.getOutput(data);

            if (croppedImageUri != null) {
                File file = new File(getRealPathFromURI(croppedImageUri));
                if (file.exists()) {
                    // Display cropped image using Glide

                    Glide.with(ProfileFragment.this)
                            .load(imageuri)
                            .circleCrop()
                            .into(binding.profileImage);


                } else {
                    Log.e("ProfileFragment", "Cropped file doesn't exist.");
                    binding.profileImage.setImageResource(R.drawable.profile);  // Fallback
                }

                // Upload cropped image to Cloudinary
                uploadProfilePictureToCloudinary(croppedImageUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Log.e("Crop Error", "Error cropping image", cropError);
            Toast.makeText(getContext(), "Image cropping failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadProfilePictureToCloudinary(Uri imageUri) {
        // Get the real file path from URI
        String filePath = getRealPathFromURI(imageUri);

        if (filePath == null) {
            Log.e("Upload Error", "File path is null. Cannot upload.");
            Toast.makeText(getContext(), "Error: Unable to process the image.", Toast.LENGTH_SHORT).show();
            return; // Stop execution
        }

        File file = new File(filePath); // Convert path to File

        // Create an upload request for Cloudinary
        new Thread(() -> {
            try {
                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

                // Extract the URL of the uploaded image from Cloudinary's response
                String imageUrl = uploadResult.get("secure_url").toString(); // This returns an HTTPS URL
                //String imageUrl = (String) uploadResult.get("url").toString();

                // Update the profile picture URL in Firestore and SQLite
                updateUserProfilePictureInFirestore(imageUrl);
                updateUserProfilePictureInSQLite(imageUrl);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Profile Update", "Failed to upload to Cloudinary", e);
            }
        }).start();
    }
    private void updateUserProfilePictureInSQLite(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail(); // Use email as the identifier

            int rowsUpdated = dbHelper.updateUserProfilePicture(imageUrl, email);
            if (rowsUpdated > 0) {
                Log.d("SQLite", "Profile picture updated in SQLite");
            } else {
                Log.e("SQLite", "Failed to update profile picture in SQLite");
            }
        }
    }
    private void updateUserNameInSQLite(String username) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail(); // Use email as the identifier

            int rowsUpdated = dbHelper.updateUserName(username, email);
            if (rowsUpdated > 0) {
                Log.d("SQLite", "Profile picture updated in SQLite");
            } else {
                Log.e("SQLite", "Failed to update profile picture in SQLite");
            }
        }
    }
    private void updateUserProfilePictureInFirestore(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference userRef = db.collection("users").document(user.getUid());

            userRef.update("profile_picture", imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                        Log.d("Profile Update", "Profile picture updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Profile Update", "Failed to update profile picture", e);
                    });
        }
    }
    private String getRealPathFromURI(Uri uri) {
        String filePath = null;

        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (index != -1) {
                    filePath = cursor.getString(index);
                }
                cursor.close();
            }
        }
        // Handle file URI directly
        else if (uri.getScheme().equals("file")) {
            filePath = uri.getPath();
        }

        return filePath;
    }


}