package com.strawhats.soleia.Domain;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;

public class EventModel implements Serializable {

    private String eventId;           // Firestore: Document ID
    private int attendeesCount;       // Firestore: attendees_count
    private int availableTickets;     // Firestore: available_tickets
    private String category;          // Firestore: catergory
    private Date createdAt;      // Firestore: created_at
    private String description;       // Firestore: description
    private Date eventDate;      // Firestore: event_date
    private String imageUrl;          // Firestore: image_url
    private String organizerId;       // Firestore: organizer_id
    private String status;            // Firestore: status
    private double ticketPrice;       // Firestore: ticket_price
    private String title;             // Firestore: title
    private LatLng venue;  // Replace GeoPoint with LatLng
    private String venueName;         // Firestore: venue_name

    // Default constructor
    public EventModel() {}

    // Constructor with parameters
    public EventModel(String eventId, int attendeesCount, int availableTickets, String category,
                      Date createdAt, String description, Date eventDate, String imageUrl,
                      String organizerId, String status, double ticketPrice, String title,
                      LatLng venue, String venueName) {
        this.eventId = eventId;
        this.attendeesCount = attendeesCount;
        this.availableTickets = availableTickets;
        this.category = category;
        this.createdAt = createdAt;
        this.description = description;
        this.eventDate = eventDate;
        this.imageUrl = imageUrl;
        this.organizerId = organizerId;
        this.status = status;
        this.ticketPrice = ticketPrice;
        this.title = title;
        this.venue = venue;
        this.venueName = venueName;
    }

    // Getter and Setter methods for each field

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @PropertyName("attendees_count")
    public int getAttendeesCount() {
        return attendeesCount;
    }

    @PropertyName("attendees_count")
    public void setAttendeesCount(int attendeesCount) {
        this.attendeesCount = attendeesCount;
    }

    @PropertyName("available_tickets")
    public int getAvailableTickets() {
        return availableTickets;
    }

    @PropertyName("available_tickets")
    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    @PropertyName("category")
    public String getCategory() {
        return category;
    }

    @PropertyName("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @PropertyName("created_at")
    public Date  getCreatedAt() {
        return createdAt;
    }

    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt.toDate();
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("event_date")
    public Date  getEventDate() {
        return eventDate;
    }

    @PropertyName("event_date")
    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate.toDate();
    }

    @PropertyName("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("organizer_id")
    public String getOrganizerId() {
        return organizerId;
    }

    @PropertyName("organizer_id")
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("ticket_price")
    public double getTicketPrice() {
        return ticketPrice;
    }

    @PropertyName("ticket_price")
    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    @PropertyName("title")
    public String getTitle() {
        return title;
    }

    @PropertyName("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("venue")
    public void setVenue(GeoPoint venue) {
        this.venue = new LatLng(venue.getLatitude(), venue.getLongitude());
    }

    @PropertyName("venue")
    public LatLng getVenue() {
        return venue;
    }



    @PropertyName("venue_name")
    public String getVenueName() {
        return venueName;
    }

    @PropertyName("venue_name")
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
}



