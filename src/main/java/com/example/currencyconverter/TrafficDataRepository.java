package com.example.currencyconverter;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class TrafficDataRepository {

    private FirebaseFirestore db;
    private CollectionReference trafficDataCollection;

    public TrafficDataRepository() {
        db = FirebaseFirestore.getInstance();
        trafficDataCollection = db.collection("traffic_data");
    }

    public void getTrafficDataForPeriod(Date startDate, Date endDate) {
        // Létrehozzuk a lekérdezést a Firestore adatbázisban
        Query query = trafficDataCollection
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate);
    }
}
