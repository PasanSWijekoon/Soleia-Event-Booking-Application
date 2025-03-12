package com.strawhats.soleia.Fragment;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.strawhats.soleia.Activity.MainActivity;
import com.strawhats.soleia.Activity.MapActivity;
import com.strawhats.soleia.Activity.OraganizerMainActivity;
import com.strawhats.soleia.Adapters.CatergoryAdapter;
import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Models.CustomToast;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.R;
import com.strawhats.soleia.ViewModel.MainViewModel;
import com.strawhats.soleia.databinding.FragmentOrganizerCreateEventBinding;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class OrganizerCreateEventFragment extends Fragment {

    private FragmentOrganizerCreateEventBinding binding;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener accelerometerListener;
    private boolean isFlip = false; // Use isFlip instead of isFLip (typo)

    CustomToast customToast;
    private MainViewModel viewModel;
    private EditText TxtLocation;
    private Button BtnOpenMap;
    private ActivityResultLauncher<Intent> mapActivityLauncher;
    private TextInputEditText eventDateInput;
    private TextInputEditText eventTimeInput;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Cloudinary cloudinary;
    private Uri sourceUri;
    private Uri croppedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int UCROP_REQUEST_CODE = 2;
    //location data
    private Double latitude;
    private Double longitude;

    private AutoCompleteTextView categoryDropdown;

    private String selectedCategory;

    Animation shake;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentOrganizerCreateEventBinding.inflate(inflater, container, false);

        // Initialize Cloudinary with your credentials
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "jjj",
                "api_key", "jjj",
                "api_secret", "jjjjjj"
        ));

        shake = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        eventDateInput = binding.eventDateInput;
        eventTimeInput = binding.eventTimeInput;

        // Initialize calendar and formatters
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Set up click listeners for date and time fields
        setupDatePicker();
        setupTimePicker();

        //Load caterogry data

        categoryDropdown = binding.categoryDropdown; // Find the view

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.loadCatergory().observe(getViewLifecycleOwner(), categoryModels -> {
            if (categoryModels != null) {
                // 1. Create an ArrayAdapter
                List<String> categoryNames = new ArrayList<>(); // List to store category names
                for (CategoryModel category : categoryModels) {
                    categoryNames.add(category.getName()); // Extract names
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), // Use requireContext() in Fragments
                        android.R.layout.simple_dropdown_item_1line, // Standard layout
                        categoryNames // The data to display
                );

                // 2. Set the adapter to the AutoCompleteTextView
                categoryDropdown.setAdapter(adapter);

                // 3. Optional: Set a listener for item clicks (if needed)
                categoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCategory = (String) parent.getItemAtPosition(position);

                    binding.categoryDropdown.setError(null);
                    // Do something with the selected category (e.g., save it)
                    Log.d("Category Selection", "Selected: " + selectedCategory);
                });

            } else {
                // Handle the case where categoryModels is null (e.g., show an error message)
                Toast.makeText(requireContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
                Log.e("Category Loading", "Category data is null"); // Log the error
            }
        });




        BtnOpenMap = binding.selectVenueButton;
        TxtLocation = binding.venueNameInput;

        // Initialize ActivityResultLauncher
        mapActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        latitude = data.getDoubleExtra("latitude", 0.0);
                        longitude = data.getDoubleExtra("longitude", 0.0);
                        String locationName = data.getStringExtra("locationName");

                        TxtLocation.setText(locationName);
                    }
                }
        );


        BtnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MapActivity.class);
            mapActivityLauncher.launch(intent); // Use the launcher
        });

        customToast = new CustomToast(getContext());

        binding.selectImageButton.setOnClickListener(v -> chooseProfilePicture()); // Set click listener

        binding.createEventButton.setOnClickListener(view -> {
            saveEventdata();
        });

        // Initialize Sensor Manager and Accelerometer
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) { // Check if the device has an accelerometer
            accelerometerListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float[] values = event.values;
                    float z = values[2];

                    if (z < -9 && !isFlip) {
                        clearEventData();
                        vibratePhone();
                        isFlip = true;
                    }

                    if (z > 9 && isFlip) {
                        isFlip = false;
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Handle accuracy changes if needed
                }
            };

            sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // Handle the case where the device doesn't have an accelerometer

        }

        return binding.getRoot();

    }



    private void setupDatePicker() {
        eventDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    R.style.CustomDatePickerDialog,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            // Set title
            datePickerDialog.setTitle("Select Event Date");

            datePickerDialog.show();
        });
    }

    private void setupTimePicker() {
        eventTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    R.style.CustomTimePickerDialog,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateTimeField();
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // 12-hour format
            );

            // Set title
            timePickerDialog.setTitle("Select Event Time");

            timePickerDialog.show();
        });
    }

    private void updateDateField() {
        eventDateInput.setText(dateFormat.format(calendar.getTime()));
        eventDateInput.setError(null);
    }

    private void updateTimeField() {
        eventTimeInput.setText(timeFormat.format(calendar.getTime()));
        eventTimeInput.setError(null);
    }

    public Calendar getSelectedDateTime() {
        return (Calendar) calendar.clone();
    }

    public boolean isDateTimeSelected() {
        return !eventDateInput.getText().toString().isEmpty()
                && !eventTimeInput.getText().toString().isEmpty();
    }

    private void chooseProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            sourceUri = data.getData(); // The selected image URI


            // Destination URI (where the cropped image will be saved)
            Uri destinationUri = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped_profile.jpg"));

            // Start uCrop for cropping
            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(4, 3)
                    .withMaxResultSize(1024, 768) // Set max size for the cropped image
                    .start(getActivity(), this, UCROP_REQUEST_CODE);
        }

        // Handle the cropped image result
        else if (requestCode == UCROP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            croppedImageUri = UCrop.getOutput(data);

            if (croppedImageUri != null) {
                    // Display cropped image using Glide
                Log.i("Crop", croppedImageUri.toString());
                    Glide.with(OrganizerCreateEventFragment.this)
                            .load(sourceUri)
                            .transform(new RoundedCorners(16)) // 16 pixels corner radius
                            .into(binding.eventImagePreview);
                    binding.eventImagePreview.setVisibility(VISIBLE);



            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Log.e("Crop Error", "Error cropping image", cropError);
            Toast.makeText(getContext(), "Image cropping failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper function to avoid repetition and make code cleaner
    private void showErrorToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showError(EditText field, String message, Animation animation) {
        field.setError(message);
        field.startAnimation(animation);
        customToast.showToast(message, R.raw.warning);
        vibratePhone();

        buttonsBack();

    }

    private void buttonsGone() {
        binding.progressBar3.setVisibility(VISIBLE);
        binding.createEventButton.setVisibility(GONE);
        binding.selectVenueButton.setVisibility(GONE);
        binding.selectImageButton.setVisibility(GONE);
    }

    private void buttonsBack() {
        binding.progressBar3.setVisibility(GONE);
        binding.createEventButton.setVisibility(VISIBLE);
        binding.selectVenueButton.setVisibility(VISIBLE);
        binding.selectImageButton.setVisibility(VISIBLE);
    }


    private void saveEventdata() {

        buttonsGone();


        String eventTitle = String.valueOf(binding.eventTitleInput.getText());
        String eventDescription = String.valueOf(binding.eventDescriptionInput.getText());
        String ticketPriceStr = binding.ticketPriceInput.getText().toString();
        String availableTicketsStr = binding.availableTicketsInput.getText().toString();
        String venueName = binding.venueNameInput.getText().toString();



        if (eventTitle.trim().isEmpty()) { // Check for whitespace too
            showError(binding.eventTitleInput, "Please Enter Event Title", shake);
            return;
        }

        if (eventDescription.trim().isEmpty()) { // Check for whitespace too
            showError(binding.eventDescriptionInput, "Please Enter Event Description", shake);
            return;
        }

        // 1. Minimum Title Length:
        if (eventTitle.trim().length() < 3) { // minimum 3 characters
            showError(binding.eventTitleInput, "Event Title must be at least 3 characters long.", shake);
            return;
        }

        if (eventDescription.trim().length() > 500) { // maximum 500 characters
            showError(binding.eventDescriptionInput, "Event Description is too long (maximum 500 characters).", shake);
            return;
        }

        if (selectedCategory == null || selectedCategory.isEmpty()) { // Check for null and empty
            showError(binding.categoryDropdown, "Please Select a Category", shake);
            return;
        }

        //check datetime Selected
        if (!isDateTimeSelected()){
            showError(binding.eventDateInput, "Please select date and time", shake);
            showError(binding.eventTimeInput, "Please select date and time", shake);
            return; // Stop execution
        }

        if (ticketPriceStr.isEmpty()) {
            showError(binding.ticketPriceInput, "Ticket price is required", shake);
            return;
        }


        try {
            double ticketPrice = Double.parseDouble(ticketPriceStr);
            if (ticketPrice <= 0) {  // Now includes 0 check
                showError(binding.ticketPriceInput, "Ticket price must be greater than zero", shake);
                return;
            }
        } catch (NumberFormatException e) {
            showError(binding.ticketPriceInput, "Invalid ticket price format", shake);
            return;
        }

        if (availableTicketsStr.isEmpty()) {
            showError(binding.availableTicketsInput, "Available tickets is required", shake);
            return;
        }


        try {
           double availableTickets = Double.parseDouble(availableTicketsStr);
            if (availableTickets <= 0) { // Now includes 0 check
                showError(binding.availableTicketsInput, "Available tickets must be greater than or equal to zero", shake);
                return;
            }
        } catch (NumberFormatException e) {
            showError(binding.availableTicketsInput, "Invalid number of tickets format", shake);
            return;
        }


        if (latitude==null && longitude==null) {
            showError(binding.venueNameInput, "Please Select Location", shake);
            return;
        }

        if (venueName.isEmpty()) {
            showError(binding.venueNameInput, "Please Enter Location", shake);

            return;
        }


        //check image empty or not
        if (croppedImageUri == null) {
            buttonsBack();
            customToast.showToast("Please select Event photo", R.raw.warning);
            return; // Stop execution
        }



        //getpath
        String filePath = getRealPathFromURI(croppedImageUri);

        if (filePath == null) {
            buttonsBack();
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

                CreateEventInFirestore(imageUrl);

            } catch (IOException e) {
                buttonsBack();
                e.printStackTrace();
                Log.e("Profile Update", "Failed to upload to Cloudinary", e);
            }
        }).start();



    }

    public Timestamp getFirestoreTimestamp() {
        Calendar selectedCalendar = getSelectedDateTime(); // Get the selected Calendar
        Date date = selectedCalendar.getTime(); // Convert Calendar to Date
        return new Timestamp(date); // Convert Date to Firestore Timestamp
    }

    private void CreateEventInFirestore(String imageUrl) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {


            String eventTitle = String.valueOf(binding.eventTitleInput.getText());
            String eventDescription = String.valueOf(binding.eventDescriptionInput.getText());
            String ticketPriceStr = String.valueOf(binding.ticketPriceInput.getText());
            String availableTicketsStr = String.valueOf(binding.availableTicketsInput.getText());
            String venueName = String.valueOf(binding.venueNameInput.getText());

            // Create event data
            Map<String, Object> event = new HashMap<>();
            event.put("title", eventTitle);
            event.put("description", eventDescription);
            event.put("category", selectedCategory);
            event.put("image_url", imageUrl);
            event.put("organizer_id", user.getUid());
            event.put("venue", new GeoPoint(latitude, longitude)); // Use GeoPoint for location
            event.put("venue_name", venueName);
            event.put("ticket_price", Double.parseDouble(ticketPriceStr));
            event.put("event_date", getFirestoreTimestamp()); // Store as string. Firestore will convert to Timestamp when added.
            event.put("created_at", Timestamp.now()); // Store as string. Firestore will convert to Timestamp when added.
            event.put("status", "noapproved"); // Default status
            event.put("attendees_count", 0); // Initial count
            event.put("available_tickets",  Integer.parseInt(availableTicketsStr)); // Initial count

            // Save to Firestore
            db.collection("Events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        customToast.showToast("Event successfully Added Wait For Admin Approval", R.raw.success);
                        buttonsBack();
                        clearEventData();
                        sendPurchaseNotification();

                    })
                    .addOnFailureListener(e -> {
                        buttonsBack();
                        customToast.showToast("Failed", R.raw.warning);
                    });

        }
    }

    private void sendPurchaseNotification() {
        String CHANNEL_ID = "Success_notifications";
        String notificationMessage = "You've successfully Created Event PLease Wait For Admin Approval";

        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // 🔹 Create Notification Channel (For Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Success_notifications", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // 🔹 Open App When Notification Clicked
        Intent mainIntent = new Intent(getContext(), OraganizerMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getContext(), 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔹 Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.applogo) // Change to your app's notification icon
                .setContentTitle("✨ Event Created!")
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        String setTitle = "Success";
        // 🔔 Show the Notification
        manager.notify(setTitle.hashCode(), builder.build());

        // 🔹 Save Notification in SQLite
        NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper(getContext());
        dbHelper.saveNotification("✨ Event Created!", notificationMessage);
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


    private void clearEventData() {
        binding.eventTitleInput.setText("");
        binding.eventDescriptionInput.setText("");
        binding.ticketPriceInput.setText("");
        binding.availableTicketsInput.setText("");
        binding.venueNameInput.setText("");

        // Clear Category (AutoCompleteTextView)
        binding.categoryDropdown.setText(""); // Clear the text
        selectedCategory = null; // Clear the underlying data

        // Clear Date and Time
        binding.eventDateInput.setText("");
        binding.eventTimeInput.setText("");


        // Clear Location
        latitude = null;
        longitude = null;
        // You might want to update the displayed location on the UI as well

        // Clear Image
        croppedImageUri = null;
        binding.eventImagePreview.setVisibility(GONE); // Hide the preview


    }

    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500); // Vibrate for 500ms on older devices
            }
        }
    }

}