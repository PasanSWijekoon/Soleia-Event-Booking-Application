package com.strawhats.soleia.Domain;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Date;

public class TicketModel implements Serializable {

    private String paymentId;
    private String eventId;
    private String userId;
    private int ticketCount;
    private String status;
    private int ticketPrice;
    private Date timestamp;

    private String imageUrl;

    private String title;

    private LatLng venue;  // Replace GeoPoint with LatLng
    private String venueName;

    private Date eventDate;      // Firestore: event_date


    public TicketModel() {
    }

    public TicketModel(String paymentId, String title, Date eventDate, String venueName, double amount, int ticketCount, String status, GeoPoint venue) {
        this.paymentId = paymentId;
        this.title = title;
        this.eventDate = eventDate;
        this.venueName = venueName;
        this.ticketPrice = (int) amount; // Convert double to int
        this.ticketCount = ticketCount;
        this.status = status;
        this.venue = new LatLng(venue.getLatitude(), venue.getLongitude()); // Convert GeoPoint to LatLng
    }



    public Date  getEventDate() {
        return eventDate;
    }


    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate.toDate();
    }



    public void setVenue(GeoPoint venue) {
        this.venue = new LatLng(venue.getLatitude(), venue.getLongitude());
    }


    public LatLng getVenue() {
        return venue;
    }


    public String getVenueName() {
        return venueName;
    }


    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public class LatLng implements Serializable {
        private double latitude;
        private double longitude;

        public LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    @PropertyName("eventId")
    public String getEventId() {
        return eventId;
    }
    @PropertyName("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }
    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }
    @PropertyName("ticket_count")
    public int getTicketCount() {
        return ticketCount;
    }
    @PropertyName("ticket_count")
    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }
    @PropertyName("status")
    public String getStatus() {
        return status;
    }
    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }
    @PropertyName("amount")
    public int getTicketPrice() {
        return ticketPrice;
    }
    @PropertyName("amount")
    public void setTicketPrice(int ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    @PropertyName("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp.toDate();
    }



}
