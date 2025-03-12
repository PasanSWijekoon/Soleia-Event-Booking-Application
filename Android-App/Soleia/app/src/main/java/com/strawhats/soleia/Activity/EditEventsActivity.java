package com.strawhats.soleia.Activity;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.Fragment.OrganizerCreateEventFragment;
import com.strawhats.soleia.Models.CustomToast;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.R;
import com.strawhats.soleia.ViewModel.MainViewModel;
import com.strawhats.soleia.databinding.ActivityEditEventsBinding;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditEventsActivity extends AppCompatActivity {

    private ActivityEditEventsBinding binding;
    private EventModel event;
    private Calendar calendar;
    private MainViewModel viewModel;
    private String selectedCategory;

    private TextInputEditText eventDateInput;
    private TextInputEditText eventTimeInput;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    private Uri sourceUri;
    private Uri croppedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int UCROP_REQUEST_CODE = 2;

    CustomToast customToast;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Cloudinary cloudinary;

    private EditText TxtLocation;
    private Button BtnOpenMap;

    private Double latitude;
    private Double longitude;
    Animation shake;
    private ActivityResultLauncher<Intent> mapActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        calendar = Calendar.getInstance(); // Initialize calendar
        customToast = new CustomToast(EditEventsActivity.this);
        // Retrieve event object passed through intent
        event = (EventModel) getIntent().getSerializableExtra("event");

        if (event == null) {
            // Handle the case where the event is not passed correctly
            Toast.makeText(this, "Error: Event data not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return; // Important: Stop further execution
        }

        // Initialize Cloudinary with your credentials
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "mmmm",
                "api_key", "mmmm",
                "api_secret", "mmmmmmmm"
        ));

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the toolbar
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // Simplified finish
        shake = AnimationUtils.loadAnimation(EditEventsActivity.this, R.anim.fade_in);
        // Load the event image using Glide (handle potential null URL)
        if (!TextUtils.isEmpty(event.getImageUrl())) {
            Glide.with(this)
                    .load(event.getImageUrl())
                    .into(binding.eventImagePreview);
        } else {
            // Set a placeholder or handle the missing image URL
           // binding.eventImagePreview.setImageResource(R.drawable.ic_placeholder_image); // Example
        }

        // Populate the input fields with the current event data
        binding.eventTitleInput.setText(event.getTitle());
        binding.eventDescriptionInput.setText(event.getDescription());

        calendar.setTime(event.getEventDate());
        // Category Dropdown Setup
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.loadCatergory().observe(this, categoryModels -> {
            if (categoryModels != null) {
                List<String> categoryNames = new ArrayList<>();
                for (CategoryModel category : categoryModels) {
                    categoryNames.add(category.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                );

                binding.categoryDropdown.setAdapter(adapter);

                // Set the selected category based on the event data
                if (event.getCategory() != null) {
                    binding.categoryDropdown.setText(event.getCategory(), false); // Use setText with false for no filtering
                    selectedCategory = event.getCategory(); //set selected category
                }

                binding.categoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCategory = (String) parent.getItemAtPosition(position);
                    binding.categoryDropdown.setError(null); // Clear any error
                    Log.d("Category Selection", "Selected: " + selectedCategory);
                });

            } else {
                Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show();
                Log.e("Category Loading", "Category data is null");
            }
        });

        // Initialize views
        eventDateInput = binding.eventDateInput;
        eventTimeInput = binding.eventTimeInput;

        // Date and Time Pickers
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (event.getEventDate() != null) {
            eventDateInput.setText(dateFormat.format(event.getEventDate()));
        }
        if (event.getEventDate() != null) {
            eventTimeInput.setText(timeFormat.format(event.getEventDate()));
        }

        binding.eventDateInput.setOnClickListener(v -> showDatePicker(dateFormat));
        binding.eventTimeInput.setOnClickListener(v -> showTimePicker(timeFormat));


        binding.ticketPriceInput.setText(String.valueOf(event.getTicketPrice()));
        binding.availableTicketsInput.setText(String.valueOf(event.getAvailableTickets()));
        binding.venueNameInput.setText(event.getVenueName()); // Use venueName

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
            Intent intent = new Intent(EditEventsActivity.this, MapActivity.class);
            mapActivityLauncher.launch(intent); // Use the launcher
        });

        binding.selectImageButton.setOnClickListener(v -> chooseProfilePicture()); // Set click listener

        // Handle the update event button click
        binding.updateEventButton.setOnClickListener(v -> {
            buttonsGone();
            if (validateInputs()) {
                uploadImageToCloud();
                buttonsBack();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedEvent", event);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void uploadImageToCloud() {
        if (croppedImageUri != null) {
            //getpath
            String filePath = getRealPathFromURI(croppedImageUri);

            if (filePath == null) {
                buttonsBack();
                Log.e("Upload Error", "File path is null. Cannot upload.");
                Toast.makeText(EditEventsActivity.this, "Error: Unable to process the image.", Toast.LENGTH_SHORT).show();
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

                    UpdateInFirestore(imageUrl);

                } catch (IOException e) {
                    buttonsBack();
                    e.printStackTrace();
                    Log.e("Profile Update", "Failed to upload to Cloudinary", e);
                }
            }).start();
        }else {
            UpdateInFirestore("Pass");
        }
    }

    public Timestamp getFirestoreTimestamp() {
        Calendar selectedCalendar = getSelectedDateTime(); // Get the selected Calendar
        Date date = selectedCalendar.getTime(); // Convert Calendar to Date
        return new Timestamp(date); // Convert Date to Firestore Timestamp
    }

    private void UpdateInFirestore(String imageUrl) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {


            String eventTitle = String.valueOf(binding.eventTitleInput.getText());
            String eventDescription = String.valueOf(binding.eventDescriptionInput.getText());
            String ticketPriceStr = String.valueOf(binding.ticketPriceInput.getText());
            String availableTicketsStr = String.valueOf(binding.availableTicketsInput.getText());
            String venueName = String.valueOf(binding.venueNameInput.getText());

            // Create event data
            Map<String, Object> eventdata = new HashMap<>();
            eventdata.put("title", eventTitle);
            eventdata.put("description", eventDescription);
            eventdata.put("category", selectedCategory);
            if (!imageUrl.equals("Pass")){
                eventdata.put("image_url", imageUrl);
            }
            eventdata.put("organizer_id", user.getUid());

            if (latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0) { // Check for null AND default values
                eventdata.put("venue", new GeoPoint(latitude, longitude));
            }
            eventdata.put("venue_name", venueName);
            eventdata.put("ticket_price", Double.parseDouble(ticketPriceStr));
            eventdata.put("event_date", getFirestoreTimestamp()); // Store as string. Firestore will convert to Timestamp when added.
            eventdata.put("created_at", Timestamp.now()); // Store as string. Firestore will convert to Timestamp when added.
            eventdata.put("status", "noapproved"); // Default status
            eventdata.put("available_tickets",  Integer.parseInt(availableTicketsStr)); // Initial count

            db.collection("Events").document(event.getEventId()) // Use event ID
                    .update(eventdata)
                    .addOnSuccessListener(aVoid -> {
                        customToast.showToast("Event successfully Updated Wait For Admin Approval", R.raw.success);
                        sendPurchaseNotification(eventTitle);
                    })
                    .addOnFailureListener(e -> {
                        buttonsBack();
                        customToast.showToast("Failed", R.raw.warning);
                    });


        }
    }

    private void sendPurchaseNotification(String EventTitle) {
        String CHANNEL_ID = "Success_notifications";
        String notificationMessage = "You've successfully Updated "+EventTitle+" PLease Wait For Admin Approval";

        NotificationManager manager = (NotificationManager) EditEventsActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

        // 🔹 Create Notification Channel (For Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Success_notifications", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // 🔹 Open App When Notification Clicked
        Intent mainIntent = new Intent(EditEventsActivity.this, OraganizerMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                EditEventsActivity.this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔹 Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(EditEventsActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.applogo) // Change to your app's notification icon
                .setContentTitle("📝 Event Updated!")
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        String setTitle = "Success";
        // 🔔 Show the Notification
        manager.notify(setTitle.hashCode(), builder.build());

        // 🔹 Save Notification in SQLite
        NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper(EditEventsActivity.this);
        dbHelper.saveNotification("✨ Event Updated!", notificationMessage);
    }


    private String getRealPathFromURI(Uri uri) {
        String filePath = null;

        if (uri.getScheme().equals("content")) {
            Cursor cursor = EditEventsActivity.this.getContentResolver().query(uri, null, null, null, null);
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


    private void showError(EditText field, String message, Animation animation) {
        field.setError(message);
        field.startAnimation(animation);
        customToast.showToast(message, R.raw.warning);
        vibratePhone();

        buttonsBack();

    }

    private void buttonsGone() {
        binding.progressBar.setVisibility(VISIBLE);
        binding.updateEventButton.setVisibility(GONE);
        binding.selectVenueButton.setVisibility(GONE);
        binding.selectImageButton.setVisibility(GONE);
    }

    private void buttonsBack() {
        binding.progressBar.setVisibility(GONE);
        binding.updateEventButton.setVisibility(VISIBLE);
        binding.selectVenueButton.setVisibility(VISIBLE);
        binding.selectImageButton.setVisibility(VISIBLE);
    }


    private void showDatePicker(SimpleDateFormat dateFormat) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDatePickerDialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year1);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                }, year, month, day);
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.setTitle("Select Event Date");
        datePickerDialog.show();
    }


    private void showTimePicker(SimpleDateFormat timeFormat) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.CustomTimePickerDialog,
                (view, hourOfDay, minute1) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute1);
                    updateTimeField();
                }, hour, minute, false);
        timePickerDialog.setTitle("Select Event Time");
        timePickerDialog.show();
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
            Uri destinationUri = Uri.fromFile(new File(EditEventsActivity.this.getCacheDir(), "cropped_profile.jpg"));

            // Start uCrop for cropping
            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(4, 3)
                    .withMaxResultSize(1024, 768) // Set max size for the cropped image
                    .start(EditEventsActivity.this, UCROP_REQUEST_CODE);
        }

        // Handle the cropped image result
        else if (requestCode == UCROP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            croppedImageUri = UCrop.getOutput(data);

            if (croppedImageUri != null) {
                // Display cropped image using Glide
                Log.i("Crop", croppedImageUri.toString());
                Glide.with(EditEventsActivity.this)
                        .load(sourceUri)
                        .transform(new RoundedCorners(16)) // 16 pixels corner radius
                        .into(binding.eventImagePreview);
                binding.eventImagePreview.setVisibility(VISIBLE);



            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Log.e("Crop Error", "Error cropping image", cropError);
            Toast.makeText(EditEventsActivity.this, "Image cropping failed", Toast.LENGTH_SHORT).show();
        }
    }




//    private void updateEventData() {
//        event.setTitle(binding.eventTitleInput.getText().toString().trim());
//        event.setDescription(binding.eventDescriptionInput.getText().toString().trim());
//        event.setCategory(binding.categoryDropdown.getText().toString().trim());
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//        try {
//            Date eventDateTime = dateFormat.parse(binding.eventDateInput.getText().toString() + " " + binding.eventTimeInput.getText().toString());
//            event.setEventDate(eventDateTime);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Invalid date or time format", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            double price = Double.parseDouble(binding.ticketPriceInput.getText().toString().trim());
//            event.setTicketPrice(price);
//        } catch (NumberFormatException e) {
//            binding.ticketPriceInput.setError("Invalid ticket price");
//            return;
//        }
//
//        try {
//            int availableTickets = Integer.parseInt(binding.availableTicketsInput.getText().toString().trim());
//            event.setAvailableTickets(availableTickets);
//        } catch (NumberFormatException e) {
//            binding.availableTicketsInput.setError("Invalid number of tickets");
//            return;
//        }
//
//        event.setVenueName(binding.venueNameInput.getText().toString().trim()); // Use venueName
//        // ... other fields if necessary
//    }
//

    private boolean validateInputs() {
        boolean valid = true;

        if (TextUtils.isEmpty(binding.eventTitleInput.getText().toString().trim())) {
            showError(binding.eventTitleInput, "Please Enter Event Title", shake);
            valid = false;
        }
        if (TextUtils.isEmpty(binding.eventDescriptionInput.getText().toString().trim())) {
            showError(binding.eventDescriptionInput, "Please Enter Event Description", shake);
            valid = false;
        }

        if (TextUtils.isEmpty(binding.eventDateInput.getText().toString().trim())) {
            binding.eventDateInput.setError("Event date is required.");
            valid = false;
        }

        if (selectedCategory == null || selectedCategory.isEmpty()) { // Check for null and empty
            showError(binding.categoryDropdown, "Please Select a Category", shake);
            valid = false;
        }


        if (TextUtils.isEmpty(binding.eventDateInput.getText().toString().trim())) {
            showError(binding.eventDateInput, "Please select date ", shake);
            valid = false;
        }

        if (TextUtils.isEmpty(binding.eventTimeInput.getText().toString().trim())) {
            showError(binding.eventTimeInput, "Please select time", shake);
            valid = false;
        }

        if (TextUtils.isEmpty(binding.ticketPriceInput.getText().toString().trim())) {
            showError(binding.ticketPriceInput, "Ticket price is required", shake);
            valid = false;
        }

        try {
            double ticketPrice = Double.parseDouble(binding.ticketPriceInput.getText().toString().trim());
            if (ticketPrice <= 0) {  // Now includes 0 check
                showError(binding.ticketPriceInput, "Ticket price must be greater than zero", shake);
                valid = false;
            }
        } catch (NumberFormatException e) {
            showError(binding.ticketPriceInput, "Invalid ticket price format", shake);
            valid = false;
        }

        if (TextUtils.isEmpty(binding.availableTicketsInput.getText().toString().trim())) {
            showError(binding.availableTicketsInput, "Available tickets is required", shake);
            valid = false;
        }

        try {
            double availableTickets = Double.parseDouble(binding.availableTicketsInput.getText().toString().trim());
            if (availableTickets <= 0) { // Now includes 0 check
                showError(binding.availableTicketsInput, "Available tickets must be greater than or equal to zero", shake);
                valid = false;
            }
        } catch (NumberFormatException e) {
            showError(binding.availableTicketsInput, "Invalid number of tickets format", shake);
            valid = false;
        }

        if (TextUtils.isEmpty(binding.venueNameInput.getText().toString().trim())) {
            showError(binding.venueNameInput, "Please Enter Location", shake);
            valid = false;
        }

        return valid;
    }

    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) EditEventsActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500); // Vibrate for 500ms on older devices
            }
        }
    }
}