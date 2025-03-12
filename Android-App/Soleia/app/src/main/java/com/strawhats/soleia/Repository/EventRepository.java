package com.strawhats.soleia.Repository;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Domain.EventModel;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<EventModel>> getFilteredEventsNearby(double latitude, double longitude, double radiusInKm,
                                                              List<String> categories, String searchQuery) {
        MutableLiveData<List<EventModel>> liveData = new MutableLiveData<>();

        // Set default GeoPoint if latitude and longitude are invalid
        GeoPoint center = (latitude != 0 && longitude != 0) ? new GeoPoint(latitude, longitude) : null;

        // Start Firestore query
        Query query = db.collection("Events")
                .whereEqualTo("status", "approved");

        // Apply multiple category filter (if provided)
        if (categories != null && !categories.isEmpty()) {
            query = query.whereIn("category", categories); // Filter by categories
        }

        // Apply search query filter (if provided)
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("title", searchQuery)
                    .whereLessThanOrEqualTo("title", searchQuery + "\uf8ff"); // Text search
        }

        // Retrieve events from Firestore
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventModel> eventList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        EventModel event = document.toObject(EventModel.class);
                        if (event != null) {
                            event.setEventId(document.getId()); // Set document ID

                            // Apply distance filter if coordinates are provided
                            if (center != null) {
                                double distance = calculateDistance(center, new GeoPoint(event.getVenue().getLatitude(), event.getVenue().getLongitude()));
                                if (distance <= radiusInKm) {
                                    eventList.add(event); // Within radius
                                }
                            } else {
                                eventList.add(event); // No location filter applied
                            }
                        }
                    }
                    liveData.setValue(eventList); // Return filtered list
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error: " + e.getMessage());
                    liveData.setValue(null); // Handle errors
                });

        return liveData;
    }


    private double calculateDistance(GeoPoint p1, GeoPoint p2) {
        float[] results = new float[1];
        Location.distanceBetween(p1.getLatitude(), p1.getLongitude(),
                p2.getLatitude(), p2.getLongitude(), results);
        return results[0] / 1000; // Convert distance from meters to kilometers
    }

    public LiveData<List<CategoryModel>> loadCatergory(){

        final MutableLiveData<List<CategoryModel>> listData = new MutableLiveData<>();
        db.collection("Category")
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
}
