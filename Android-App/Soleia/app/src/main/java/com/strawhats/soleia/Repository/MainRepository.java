package com.strawhats.soleia.Repository;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.Domain.TicketModel;

import java.util.ArrayList;
import java.util.List;

public class MainRepository {

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public LiveData<List<CategoryModel>> loadCatergory(){

        final MutableLiveData<List<CategoryModel>> listData = new MutableLiveData<>();
        firebaseFirestore.collection("Category")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CategoryModel> lists = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    CategoryModel item = document.toObject(CategoryModel.class); // Correct conversion
                                    if (item != null) {
                                        lists.add(item);
                                    } else {
                                        Log.w("Firestore", "Failed to convert document: " + document.getId() + ", Data: " + document.getData()); // Log the failure and document data
                                    }
                                } catch (Exception e) {
                                    Log.e("Firestore", "Error converting document: " + document.getId(), e); // Log any exceptions
                                }
                            }
                            listData.setValue(lists);
                        } else {
                            Log.e("Firestore", "Error getting documents: ", task.getException()); // Log the exception
                            listData.setValue(null); // Or handle the error as needed
                        }
                    }
                });

        return listData;
    }
    public LiveData<List<EventModel>> loadEvents() {
        final MutableLiveData<List<EventModel>> listData = new MutableLiveData<>();

        firebaseFirestore.collection("Events")
                .whereEqualTo("status", "approved") // Filter for approved events
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<EventModel> eventList = new ArrayList<>();

                            // Loop through each document in the result
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Convert Firestore document to EventModel object
                                    EventModel event = document.toObject(EventModel.class);

                                    if (event != null) {
                                        event.setEventId(document.getId());
                                        eventList.add(event); // Add event to list
                                    } else {
                                        Log.w("Firestore", "Failed to convert document: " + document.getId());
                                    }
                                } catch (Exception e) {
                                    Log.e("Firestore", "Error converting document: " + document.getId(), e);
                                }
                            }
                            listData.setValue(eventList); // Set the filtered list to LiveData
                        } else {
                            Log.e("Firestore", "Error getting documents: ", task.getException());
                            listData.setValue(null); // Handle error as needed
                        }
                    }
                });

        return listData;
    }

    public LiveData<List<TicketModel>> loadUserTickets(String userId) {
        final MutableLiveData<List<TicketModel>> ticketsLiveData = new MutableLiveData<>();
        List<TicketModel> ticketList = new ArrayList<>();

        // Query Firestore to get all tickets for the logged-in user
        firebaseFirestore.collection("payments")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() == null || task.getResult().isEmpty()) {
                            // ✅ No tickets found → Set LiveData to empty list
                            Log.d("Firestore", "No tickets found for user: " + userId);
                            ticketsLiveData.setValue(new ArrayList<>());  // 🔥 Fix: Set empty list to trigger UI update
                            return;
                        }

                        List<Task<DocumentSnapshot>> eventTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                TicketModel ticket = document.toObject(TicketModel.class);

                                if (ticket != null) {
                                    ticket.setPaymentId(document.getId());
                                    Log.d("Firestore", "Fetching event for Ticket: " + ticket.getEventId());

                                    // Fetch Event details (Title, Image, Venue)
                                    Task<DocumentSnapshot> eventTask = firebaseFirestore.collection("Events")
                                            .document(ticket.getEventId())
                                            .get()
                                            .addOnCompleteListener(eventTaskResult -> {
                                                if (eventTaskResult.isSuccessful() && eventTaskResult.getResult() != null) {
                                                    DocumentSnapshot eventDoc = eventTaskResult.getResult();

                                                    if (eventDoc.exists()) {
                                                        // Fetch event title & image
                                                        ticket.setTitle(eventDoc.getString("title"));
                                                        ticket.setImageUrl(eventDoc.getString("image_url"));
                                                        ticket.setEventDate(eventDoc.getTimestamp("event_date"));

                                                        // Fetch venue name
                                                        ticket.setVenueName(eventDoc.getString("venue_name"));

                                                        // Fetch venue coordinates (GeoPoint) and convert to LatLng
                                                        if (eventDoc.contains("venue")) {
                                                            GeoPoint geoPoint = eventDoc.getGeoPoint("venue");
                                                            if (geoPoint != null) {
                                                                ticket.setVenue(geoPoint);
                                                            }
                                                        }

                                                        Log.d("Firestore", "Event Loaded: " + ticket.getTitle() + " | Venue: " + ticket.getVenueName());
                                                    }
                                                } else {
                                                    Log.w("Firestore", "Event not found for Ticket: " + ticket.getEventId());
                                                }

                                                // Add ticket to list after fetching event details
                                                ticketList.add(ticket);

                                                // ✅ Only update LiveData after all event fetches are completed
                                                if (ticketList.size() == task.getResult().size()) {
                                                    ticketsLiveData.setValue(ticketList);
                                                }
                                            });

                                    eventTasks.add(eventTask);
                                }
                            } catch (Exception e) {
                                Log.e("Firestore", "Error converting document: " + document.getId(), e);
                            }
                        }
                    } else {
                        Log.e("Firestore", "Error getting ticket documents: ", task.getException());
                        ticketsLiveData.setValue(new ArrayList<>());  // 🔥 Fix: Set empty list on failure
                    }
                });

        return ticketsLiveData;




    }
    public LiveData<List<EventModel>> loadEventsByOrganizer(String organizerId) {
        final MutableLiveData<List<EventModel>> listData = new MutableLiveData<>();

        firebaseFirestore.collection("Events")
                .whereEqualTo("organizer_id", organizerId)  // Firestore query filter
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<EventModel> eventList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                EventModel event = document.toObject(EventModel.class);
                                event.setEventId(document.getId()); // Set Firestore document ID
                                eventList.add(event);
                            } catch (Exception e) {
                                Log.e("Firestore", "Error converting document: " + document.getId(), e);
                            }
                        }

                        // If no events are found, return null
                        if (eventList.isEmpty()) {
                            listData.setValue(null);
                        } else {
                            listData.setValue(eventList);
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                        listData.setValue(null);
                    }
                });

        return listData;
    }



}
