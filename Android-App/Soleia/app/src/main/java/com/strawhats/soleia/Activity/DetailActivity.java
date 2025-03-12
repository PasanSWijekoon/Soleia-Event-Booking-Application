package com.strawhats.soleia.Activity;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.strawhats.soleia.Adapters.FeaturedEventsAdapter;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.Domain.TicketModel;
import com.strawhats.soleia.Models.EventReminderReceiver;
import com.strawhats.soleia.Models.EventReminderWorker;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.R;
import com.strawhats.soleia.databinding.ActivityDetailBinding;
import com.google.android.gms.maps.model.LatLng;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityDetailBinding binding;
    private EventModel event;
    private static final int REQUEST_CALL_PERMISSION = 1;

    private GoogleMap googleMap;

    private static final int PAYHERE_REQUEST = 11001;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String organizerPhoneNumber = "";  // Variable to hold the organizer's phone number
    private String organizerName = "";  // Variable to hold the organizer's phone number

    private int ticketCount = 0;
    private final int maxTickets = 4;

    // Assuming you have an event and ticket count
    private double totalPrice;

    private  String username;
    private  String useremail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve the EventModel passed through the Intent
        event = (EventModel) getIntent().getSerializableExtra("event");
        fetchOrganizerData();


        // Set up the Toolbar
        setSupportActionBar(binding.toolbar);

        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set up the CollapsingToolbarLayout
        binding.collapsingToolbar.setTitle(event.getTitle()); // Set the event title
        binding.collapsingToolbar.setExpandedTitleTextSize(30);



        // Load event image using Glide
        Glide.with(this)
                .load(event.getImageUrl()) // Assuming event has an image URL
                .into(binding.eventImageView);



        // Set up the Call, Message, and Directions buttons
        setUpCallAndMessageButtons();


        // Initialize Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("DetailActivity", "Error: Map Fragment is NULL!");
        }

        // Set up directions button
        MaterialButton directionsButton = findViewById(R.id.directionsButton);
        directionsButton.setOnClickListener(v -> openGoogleMapsDirections(event.getVenue()));


        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        listenForEventUpdates();

        MaterialButton decreaseButton = binding.decreaseButton;
        MaterialButton increaseButton = binding.increaseButton;
        TextView ticketCountText = binding.ticketCountText;
        TextView totalPriceText = binding.totalPriceText;
        updateTicketUI();

        // Set up click listeners for buttons
        decreaseButton.setOnClickListener(v -> {
            if (ticketCount > 0) {
                ticketCount--;
                updateTicketUI();
            }
        });

        increaseButton.setOnClickListener(v -> {
            if (ticketCount < maxTickets && ticketCount < event.getAvailableTickets()) {
                ticketCount++;
                updateTicketUI();
            }
        });

        // Set up PayHere Payment Button
        MaterialButton bookTicketButton = binding.bookTicketButton;
        bookTicketButton.setOnClickListener(v -> {
            if (ticketCount > 0) {
                startPaymentProcess();
            } else {
                Toast.makeText(DetailActivity.this, "Please select at least one ticket.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void startPaymentProcess() {

        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("first_name");
                String lastName = documentSnapshot.getString("last_name");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone_number");

                username = firstName +" "+lastName;
                useremail = email;

                InitRequest req = new InitRequest();
                req.setMerchantId("1227479");       // Merchant ID
                req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
                req.setAmount(totalPrice);             // Final Amount to be charged
                req.setOrderId(UUID.randomUUID().toString());        // Unique Reference ID
                req.setItemsDescription(event.getTitle());  // Item description title

                req.getCustomer().setFirstName(firstName);
                req.getCustomer().setLastName(lastName);
                req.getCustomer().setEmail("wijekoonpasan055@gmail.com");
                req.getCustomer().setPhone(phone);
                req.getCustomer().getAddress().setAddress("No.1, Galle Road");
                req.getCustomer().getAddress().setCity("Colombo");
                req.getCustomer().getAddress().setCountry("Sri Lanka");


                Intent intent = new Intent(this, PHMainActivity.class);
                intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
                PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
                startActivityForResult(intent, PAYHERE_REQUEST); //unique request ID e.g. "11001"

            }
        }).addOnFailureListener(e -> {
            Toast.makeText(DetailActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            String msg;
            if (response == null) {
                msg = "Result: No response";
            } else {
                if (resultCode == Activity.RESULT_OK) {
                    if (response.isSuccess()) {

                        sendPurchaseNotification(event.getTitle(), ticketCount);
                        //scheduleEventReminder(event.getTitle(), event.getEventDate());
                        //checkPendingWork(); // ✅ Call this to see scheduled jobs
                        scheduleAlarmEventReminder(event.getTitle(), event.getEventDate());

                        sendemail(event.getTitle());

                        // Payment successful, save payment details
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();

                        // Prepare payment data to save
                        Map<String, Object> paymentData = new HashMap<>();
                        paymentData.put("userId", userId);
                        paymentData.put("eventId", event.getEventId());
                        paymentData.put("amount", totalPrice);
                        paymentData.put("status", "unused");
                        paymentData.put("ticket_count", ticketCount);
                        paymentData.put("timestamp", FieldValue.serverTimestamp());

                        // Save payment data in Firestore
                        db.collection("payments")
                                .add(paymentData)
                                .addOnSuccessListener(aVoid -> {
                                    updateEventData();
                                    String addedid = aVoid.getId();
                                    Log.d(TAG, "Payment details saved successfully");


                                    // Now fetch the saved ticket details using addedid
                                    fetchTicketDetailsAndOpen(addedid);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DetailActivity.this, "Failed to save payment details", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error saving payment details: ", e);
                                });



                        msg = "Payment Successful: " + response.getData().toString();
                    } else {
                        msg = "Payment Failed: " + response.toString();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    msg = "User canceled the payment request";
                } else {
                    msg = "Unexpected result code: " + resultCode;
                }
            }

            // Log the result message with INFO level
            Log.i(TAG, msg);

        }
    }

    private void updateEventData() {
        DocumentReference eventRef = db.collection("Events").document(event.getEventId());

        // Decrease the available tickets and increase the attendance count
        eventRef.update(
                "available_tickets", event.getAvailableTickets() - ticketCount,
                "attendees_count", event.getAttendeesCount() + ticketCount
        ).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Event data updated successfully");

            // Optionally, you can show a success message to the user
            Toast.makeText(DetailActivity.this, "Payment successful! Tickets booked.", Toast.LENGTH_SHORT).show();

            // Optionally, redirect the user to a success page or back to the event list
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(DetailActivity.this, "Failed to update event data", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error updating event data: ", e);
        });
    }

    private void fetchTicketDetailsAndOpen(String paymentId) {
        db.collection("payments").document(paymentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extract ticket data
                        String userId = documentSnapshot.getString("userId");
                        String eventId = documentSnapshot.getString("eventId");
                        double amount = documentSnapshot.getDouble("amount");
                        String status = documentSnapshot.getString("status");
                        long ticketCount = documentSnapshot.getLong("ticket_count");

                        // Fetch Event Details
                        db.collection("Events").document(eventId).get()
                                .addOnSuccessListener(eventSnapshot -> {
                                    if (eventSnapshot.exists()) {
                                        String title = eventSnapshot.getString("title");
                                        Date eventDate = eventSnapshot.getDate("event_date");
                                        String venueName = eventSnapshot.getString("venue_name");

                                        // Fetch geolocation data as GeoPoint
                                        GeoPoint geoPoint = eventSnapshot.getGeoPoint("venue");

                                        // Create TicketModel object with GeoPoint
                                        TicketModel ticketModel = new TicketModel(paymentId, title, eventDate, venueName, amount, (int) ticketCount, status, geoPoint);

                                        // Open SingleTicketActivity with the ticket details
                                        Intent intent = new Intent(DetailActivity.this, SingleTicketActivity.class);
                                        intent.putExtra("ticket", ticketModel);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(DetailActivity.this, "Event not found!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(DetailActivity.this, "Ticket not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailActivity.this, "Failed to fetch ticket details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching ticket details: ", e);
                });
    }

    private void sendPurchaseNotification(String eventTitle, int ticketCount) {
        String CHANNEL_ID = "purchase_notifications";
        String notificationMessage = "You've successfully purchased " + ticketCount + " ticket(s) for " + eventTitle + "!";

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 🔹 Create Notification Channel (For Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Purchase Notifications", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // 🔹 Open App When Notification Clicked
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔹 Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.applogo) // Change to your app's notification icon
                .setContentTitle("🎟️ Ticket Purchased!")
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // 🔔 Show the Notification
        manager.notify(eventTitle.hashCode(), builder.build());

        // 🔹 Save Notification in SQLite
        NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper(this);
        dbHelper.saveNotification("🎟️ Ticket Purchased!", notificationMessage);
    }
    private void scheduleEventReminder(String eventTitle, Date eventDate) {
//        long delay = eventDate.getTime() - System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1); // 1 day before
        long delay = TimeUnit.SECONDS.toMillis(10); // 🔹 Set delay to 10 seconds for testing
        if (delay > 0) {
            Data data = new Data.Builder()
                    .putString("eventTitle", eventTitle)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(EventReminderWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);
            // 🔹 Debug: Print Scheduled Time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String scheduledTime = sdf.format(new Date(System.currentTimeMillis() + delay));
            Log.d("EventReminder", "🔔 Notification for event '" + eventTitle + "' scheduled at: " + scheduledTime);
        } else {
            Log.e("EventReminder", "⛔ Invalid notification time! The event is too soon or already passed.");

        }
    }

    private void scheduleAlarmEventReminder(String eventTitle, Date eventDate) {
       // long reminderTime = eventDate.getTime() - TimeUnit.DAYS.toMillis(1); // 🔹 1 day before the event

        long reminderTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30); // 🔹 10-second delay for testing

        //long reminderTime = eventDate.getTime() - TimeUnit.DAYS.toMillis(1); // 🔔 1 day before event

        if (reminderTime < System.currentTimeMillis()) {
            Log.e("AlarmManager", "⛔ Event is too soon. Cannot schedule a reminder.");
            return;
        }

        Intent intent = new Intent(this, EventReminderReceiver.class);
        intent.putExtra("eventTitle", eventTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, eventTitle.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            Log.d("AlarmManager", "🔔 Reminder set for: " + new Date(reminderTime));
        }
    }

    private void checkPendingWork() {
        WorkManager.getInstance(this).getWorkInfosByTagLiveData("event_reminder").observe(this, workInfos -> {
            for (WorkInfo workInfo : workInfos) {
                Log.d("WorkManager", "🔹 Scheduled Work: " + workInfo.getId() + " | State: " + workInfo.getState());
            }
        });
    }



    private void openGoogleMapsDirections(EventModel.LatLng venue) {
        if (venue == null) return;

        // Create a URI for Google Maps directions
        Uri uri = Uri.parse("google.navigation:q=" + venue.getLatitude() + "," + venue.getLongitude());

        // Create intent to open Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if Google Maps is installed
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // If Google Maps is not installed, open in browser
            Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                    venue.getLatitude() + "," + venue.getLongitude());
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    // Method to update ticket UI elements
    private void updateTicketUI() {
        TextView ticketCountText = binding.ticketCountText;
        TextView totalPriceText = binding.totalPriceText;
        MaterialButton decreaseButton = binding.decreaseButton;
        MaterialButton increaseButton = binding.increaseButton;
        MaterialButton bookTicketButton = binding.bookTicketButton;

        // Update ticket count display
        ticketCountText.setText(String.valueOf(ticketCount));

        // Calculate and update total price
        totalPrice = ticketCount * event.getTicketPrice();
        totalPriceText.setText("Total: Rs " + String.format("%.2f", totalPrice));

        // Enable or disable buttons based on the ticket count
        decreaseButton.setEnabled(ticketCount > 0);
        increaseButton.setEnabled(ticketCount < maxTickets && ticketCount < event.getAvailableTickets());

        // Enable or disable the book ticket button
        bookTicketButton.setEnabled(ticketCount > 0);
    }



    // Fetch the organizer's Data from Firestore
    private void fetchOrganizerData() {
        String organizerId = event.getOrganizerId();
        if (organizerId != null && !organizerId.isEmpty()) {
            // Log to check the organizerId
            Log.d("DetailActivity", "Fetching details for organizerId: " + organizerId);

            // Firestore query to get the organizer details using the document ID
            FirebaseFirestore.getInstance()
                    .collection("users")  // The collection is "Organizer"
                    .document(organizerId)     // Document ID is the organizerId
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot organizerDoc = task.getResult();
                                if (organizerDoc != null && organizerDoc.exists()) {
                                    // Log the entire document to check all fields
                                    Log.d("DetailActivity", "Organizer data retrieved: " + organizerDoc.getData());

                                    // Fetch first name, last name, and phone number
                                    String firstName = organizerDoc.getString("first_name");
                                    String lastName = organizerDoc.getString("last_name");
                                    organizerPhoneNumber = organizerDoc.getString("phone_number");

                                    // Log the fetched fields
                                    Log.d("DetailActivity", "First Name: " + firstName);
                                    Log.d("DetailActivity", "Last Name: " + lastName);
                                    Log.d("DetailActivity", "Phone Number: " + organizerPhoneNumber);

                                    // Combine first name and last name
                                    organizerName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                                    // Populate the event data into the views
                                    setEventData();

                                } else {
                                    Log.d("DetailActivity", "Organizer not found.");
                                    Toast.makeText(DetailActivity.this, "Organizer not found.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("DetailActivity", "Error fetching organizer data: " + task.getException().getMessage());
                                Toast.makeText(DetailActivity.this, "Error fetching organizer data.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Log.e("DetailActivity", "Organizer ID is null or empty.");
        }
    }

    private  void sendemail(String eventname){

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("to_email", useremail);
            jsonObject.addProperty("event_title", eventname);
            jsonObject.addProperty("recipient_name", username);

            String jsonString = gson.toJson(jsonObject);

            RequestBody body = RequestBody.create(
                    jsonString,
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("http://192.168.1.6/soleiaAdmin/sendEmailProcess.php") // Replace with your PHP URL
                    .post(body)
                    .build();

            try {

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("HTTP_SUCCESS", "Response: " + responseStr);


                    JsonObject responseObject = gson.fromJson(responseStr, JsonObject.class);
                    String status = responseObject.get("status").getAsString();
                    String serverMessage = responseObject.get("message").getAsString();

                    Log.d("SERVER_RESPONSE", "Status: " + status + ", Message: " + serverMessage);
                } else {
                    Log.e("HTTP_ERROR", "Server Error: " + response.code());
                }
            } catch (Exception e) {
                Log.e("HTTP_ERROR", "Request Failed: " + e.getMessage());
            }
        }).start();
    }


    // Populate event data into the respective views
    private void setEventData() {
        binding.eventTitleText.setText(event.getTitle());
        binding.categoryChip.setText(event.getCategory());
        Date eventDate = event.getEventDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(eventDate);
        binding.eventDateTimeText.setText(formattedDate);
        binding.venueNameText.setText(event.getVenueName());

        // For ticket price (double -> formatted string)
        double ticketPrice = event.getTicketPrice();
        String formattedPrice = String.format(Locale.getDefault(), "Rs %.2f", ticketPrice);
        binding.ticketPriceText.setText(formattedPrice);
        // Set the organizer's full name to the TextView or whatever UI element you use to display it
        binding.organizerNameText.setText(organizerName);
        binding.availableTicketsText.setText(event.getAvailableTickets() + " tickets available");
        binding.descriptionText.setText(event.getDescription());
    }



    // Set up the Call and Message buttons
    private void setUpCallAndMessageButtons() {
        // Call Button: Dial the organizer's phone number
        binding.callButton.setOnClickListener(v -> {
            makePhoneCall();
        });

        // Message Button: Send SMS to the organizer
        binding.messageButton.setOnClickListener(v -> {
            if (event.getOrganizerId() != null && !event.getOrganizerId().isEmpty()) {
                Intent messageIntent = new Intent(Intent.ACTION_SENDTO);
                messageIntent.setData(Uri.parse("smsto:" + organizerPhoneNumber));
                messageIntent.putExtra("sms_body", "Regarding your event: " + event.getTitle());
                startActivity(messageIntent);
            } else {
                Toast.makeText(this, "Organizer's phone number is not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Create the menu for the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_detail_menu, menu); // Inflate the menu
        return true;
    }

    // Handle the menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_call) {
            // Trigger the call button click
            binding.callButton.performClick();
            return true;
        } else if (item.getItemId() == R.id.action_message) {
            // Trigger the message button click
            binding.messageButton.performClick();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            // Handle the back button click (toolbar back arrow)
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void listenForEventUpdates() {
        // Assuming 'event' is the EventModel object containing the event data
        DocumentReference eventRef = db.collection("Events").document(event.getEventId());

        // Attach a snapshot listener to the event document
        eventRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                // Check if the snapshot is valid
                if (snapshot != null && snapshot.exists()) {
//                    // Retrieve the updated attendees count
                    long updatedticketcount = snapshot.getLong("available_tickets");

                    // Update the event object (if needed) and update the UI
                    event.setAvailableTickets((int) updatedticketcount);

                    totalPrice = 0 ;
                    ticketCount = 0;
                    binding.availableTicketsText.setText(event.getAvailableTickets() + " tickets available");
                    updateTicketUI();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }



    private void makePhoneCall() {

        // Check for permission to make calls
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with the call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + organizerPhoneNumber));
            startActivity(callIntent);
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, make the call
                makePhoneCall();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission Denied! Cannot make calls.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Handle back button click (toolbar back arrow)
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        if (event != null && event.getVenue() != null) {
            // Get venue location from event model
            LatLng venueLocation = new LatLng(
                    event.getVenue().getLatitude(),
                    event.getVenue().getLongitude()
            );

            // Add marker for venue
            googleMap.addMarker(new MarkerOptions()
                    .position(venueLocation)
                    .title(event.getVenueName()));

            // Move camera to venue location
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 15f));

            // Enable zoom controls
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }
}