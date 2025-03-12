package com.strawhats.soleia.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.strawhats.soleia.R;
import java.util.List;

public class ScanTicketActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private DecoratedBarcodeView barcodeScannerView;
    private FirebaseFirestore db;
    private TextView tvEventTitle, tvEventVenue, tvEventDate, tvPaymentId, tvTicketCount, tvStatus, tvUserName, tvUserEmail, tvUserPhone;
    private View ticketInfoLayout;
    private MaterialButton btnScan, btnScanNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ticket);

        barcodeScannerView = findViewById(R.id.barcodeScannerView);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventVenue = findViewById(R.id.tvEventVenue);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvPaymentId = findViewById(R.id.tvPaymentId);
        tvTicketCount = findViewById(R.id.tvTicketCount);
        tvStatus = findViewById(R.id.tvStatus);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        ticketInfoLayout = findViewById(R.id.ticketInfoLayout);
        btnScan = findViewById(R.id.btnScan);
        btnScanNext = findViewById(R.id.btnScanNext);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Handle back navigation
        toolbar.setNavigationOnClickListener(v -> {
            Intent i = new Intent(ScanTicketActivity.this, OraganizerMainActivity.class);
            startActivity(i);
        });

        db = FirebaseFirestore.getInstance();
        checkCameraPermission();

        btnScan.setOnClickListener(v -> barcodeScannerView.resume());
        btnScanNext.setOnClickListener(v -> {
            barcodeScannerView.resume();
            ticketInfoLayout.setVisibility(View.GONE);
        });

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String scannedPaymentId = result.getText();
                if (scannedPaymentId != null) {
                    barcodeScannerView.pause();
                    fetchTicketDetails(scannedPaymentId);
                }
            }

            @Override
            public void possibleResultPoints(List resultPoints) {}
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            barcodeScannerView.resume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                barcodeScannerView.resume();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchTicketDetails(String paymentId) {
        db.collection("payments").document(paymentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userId = documentSnapshot.getString("userId");
                        String eventId = documentSnapshot.getString("eventId");
                        int ticketCount = documentSnapshot.getLong("ticket_count").intValue();
                        String status = documentSnapshot.getString("status");

                        tvPaymentId.setText("Payment ID: " + paymentId);
                        tvTicketCount.setText("Tickets: " + ticketCount);
                        tvStatus.setText("Status: " + status);

                        ticketInfoLayout.setVisibility(View.VISIBLE);

                        fetchEventDetails(eventId);
                        fetchUserDetails(userId);
                        // ✅ Update the ticket status in Firestore
                        if (!status.equalsIgnoreCase("Confirmed") && !status.equalsIgnoreCase("Completed")) {
                            showUpdateStatusDialog(paymentId); // Call the new method to show the dialog
                        }


                    } else {
                        Toast.makeText(this, "Invalid Ticket!", Toast.LENGTH_SHORT).show();
                        barcodeScannerView.resume();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching ticket details", e);
                    Toast.makeText(this, "Failed to fetch ticket details!", Toast.LENGTH_SHORT).show();
                });
    }

    private void showUpdateStatusDialog(final String paymentId) { // Make paymentId final for use in listener
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ticket Status");
        builder.setMessage("The ticket status is not Confirmed or Completed. Do you want to update it?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                updateTicketStatus(paymentId);
            }
        });

        // Handle dialog dismissal (clicking outside or back button)
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Check if the dialog was dismissed without clicking "OK"
                // You might want to re-check the status or take other actions here.
                // For example, if you want to show the dialog again:
                if (!wasOkClicked) { // Use a flag to track if "OK" was clicked

                    showUpdateStatusDialog(paymentId); // Show the dialog again
                }
                wasOkClicked = false; // Reset the flag
            }
        });

        dialog.show();
        wasOkClicked = false; // Initialize the flag before showing the dialog
    }

    private boolean wasOkClicked = false; // Flag to track if "OK" was clicked
    private void updateTicketStatus(String paymentId) {
        wasOkClicked = true;
        db.collection("payments").document(paymentId)
                .update("status", "Confirmed")
                .addOnSuccessListener(aVoid -> {

                    tvStatus.setText("Status: Confirmed"); // ✅ Update UI
                    Toast.makeText(this, "Ticket status updated to Confirmed!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating ticket status", e);
                    Toast.makeText(this, "Failed to update ticket status!", Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchEventDetails(String eventId) {
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvEventTitle.setText("Event: " + documentSnapshot.getString("title"));
                        tvEventVenue.setText("Venue: " + documentSnapshot.getString("venue_name"));
                        tvEventDate.setText("Date: " + documentSnapshot.getDate("event_date"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching event details", e);
                });
    }

    private void fetchUserDetails(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("first_name");
                        String lastName = documentSnapshot.getString("last_name");

                        tvUserName.setText("User: " + firstName+" "+lastName );
                        tvUserEmail.setText("Email: " + documentSnapshot.getString("email"));
                        tvUserPhone.setText("Phone: " + documentSnapshot.getString("phone_number"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user details", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}
