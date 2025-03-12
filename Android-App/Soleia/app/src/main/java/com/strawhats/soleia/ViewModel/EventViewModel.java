package com.strawhats.soleia.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.Repository.EventRepository;

import java.util.List;

public class EventViewModel extends ViewModel {

    private final EventRepository eventRepository;

    public EventViewModel() {
        this.eventRepository = new EventRepository();
    }

    public LiveData<List<CategoryModel>> loadCatergory(){return eventRepository.loadCatergory();}

    // Modified method to pass list of categories instead of a single category
    public LiveData<List<EventModel>> getFilteredEventsNearby(double latitude, double longitude, double radiusInKm,
                                                              List<String> categories, String searchQuery) {
        return eventRepository.getFilteredEventsNearby(latitude, longitude, radiusInKm, categories, searchQuery);
    }
}


