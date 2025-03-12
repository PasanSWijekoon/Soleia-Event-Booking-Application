package com.strawhats.soleia.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.Domain.TicketModel;
import com.strawhats.soleia.Repository.MainRepository;

import java.util.List;

public class MainViewModel extends ViewModel {

    private final MainRepository repository;

    public MainViewModel() {
        this.repository = new MainRepository();
    }

    public LiveData<List<CategoryModel>> loadCatergory(){return repository.loadCatergory();}

    public LiveData<List<EventModel>> loadEvents(){return repository.loadEvents();}

    public LiveData<List<TicketModel>> loadUserTickets(String userId) {
        return repository.loadUserTickets(userId);
    }

    public LiveData<List<EventModel>> loadEventsByOrganizer(String organizerId) {
        return repository.loadEventsByOrganizer(organizerId);
    }

}
