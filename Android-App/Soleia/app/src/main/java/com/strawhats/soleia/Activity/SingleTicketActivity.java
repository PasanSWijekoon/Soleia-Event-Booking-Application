package com.strawhats.soleia.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.strawhats.soleia.Domain.TicketModel;
import com.strawhats.soleia.R;
import com.strawhats.soleia.databinding.ActivitySingleTicketBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SingleTicketActivity extends AppCompatActivity {

    ActivitySingleTicketBinding binding;
    private TicketModel ticketModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingleTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ticketModel = (TicketModel) getIntent().getSerializableExtra("ticket");

        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Handle back navigation
        toolbar.setNavigationOnClickListener(v -> {
            Intent i = new Intent(SingleTicketActivity.this, MainActivity.class);
            startActivity(i);
        });

        MaterialButton directionsButton = binding.directionsButton;
        directionsButton.setOnClickListener(v -> openDirections());

        setTicketData();
        displayQRCode();
    }

    private void setTicketData() {
        binding.titleTextView.setText(ticketModel.getTitle());
        binding.paymentIdTextView.setText("Payment ID: " + ticketModel.getPaymentId());

        Date eventDate = ticketModel.getEventDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(eventDate);
        binding.eventDateTextView.setText("Date: " + formattedDate);
        binding.venueTextView.setText("Venue: " + ticketModel.getVenueName());

        double ticketPrice = ticketModel.getTicketPrice();
        String formattedPrice = String.format(Locale.getDefault(), "Rs %.2f", ticketPrice);
        binding.priceTextView.setText("Price: " + formattedPrice);

        binding.ticketCountTextView.setText("Tickets: " + ticketModel.getTicketCount());
        binding.statusChip.setText(ticketModel.getStatus());
    }

    private void displayQRCode() {
        String paymentId = ticketModel.getPaymentId();
        ImageView qrCodeImageView = binding.qrCodeImageView;

        // Check if QR code exists
        Bitmap qrBitmap = loadQRCodeFromInternalStorage(paymentId);

        if (qrBitmap == null) {
            // Generate new QR code if not found
            qrBitmap = generateQRCode(paymentId, 500);
            if (qrBitmap != null) {
                saveQRCodeToInternalStorage(qrBitmap, paymentId);
            }
        }

        // Display QR Code
        if (qrBitmap != null) {
            qrCodeImageView.setImageBitmap(qrBitmap);
        }
    }

    private Bitmap generateQRCode(String text, int size) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            BitMatrix bitMatrix = barcodeEncoder.encode(text, BarcodeFormat.QR_CODE, size, size);
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveQRCodeToInternalStorage(Bitmap qrBitmap, String paymentId) {
        File qrDir = new File(getFilesDir(), "QRCodeImages");
        if (!qrDir.exists()) {
            qrDir.mkdirs(); // Create directory if it doesn't exist
        }

        File qrFile = new File(qrDir, paymentId + ".png");
        if (qrFile.exists()) return; // If already exists, don't save again

        try (FileOutputStream out = new FileOutputStream(qrFile)) {
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadQRCodeFromInternalStorage(String paymentId) {
        File qrDir = new File(getFilesDir(), "QRCodeImages");
        File qrFile = new File(qrDir, paymentId + ".png");
        if (qrFile.exists()) {
            return BitmapFactory.decodeFile(qrFile.getAbsolutePath());
        }
        return null;
    }

    private void openDirections() {
        TicketModel.LatLng venue = ticketModel.getVenue();
        if (venue != null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + venue.getLatitude() + "," + venue.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to browser if Google Maps isn't installed
                Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                        venue.getLatitude() + "," + venue.getLongitude());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ticket_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareTicket();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareTicket() {
        File qrFile = getQRCodeFile(ticketModel.getPaymentId());

        if (qrFile.exists()) {
            Uri qrUri = FileProvider.getUriForFile(this, "com.strawhats.soleia.fileprovider", qrFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");

            shareIntent.putExtra(Intent.EXTRA_STREAM, qrUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Ticket");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out my ticket for " + ticketModel.getTitle() +
                    "\nPayment ID: " + ticketModel.getPaymentId());

            // Grant temporary read permission
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } else {
            // Handle case where QR code does not exist
            Toast.makeText(this, "QR Code not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private File getQRCodeFile(String paymentId) {
        File qrDir = new File(getFilesDir(), "QRCodeImages");
        return new File(qrDir, paymentId + ".png");
    }
}
