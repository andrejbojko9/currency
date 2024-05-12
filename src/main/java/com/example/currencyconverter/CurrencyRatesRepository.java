package com.example.currencyconverter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
public class CurrencyRatesRepository {
    private FirebaseFirestore db;
    private CollectionReference exchangeRatesCollection;

    public CurrencyRatesRepository() {
        db = FirebaseFirestore.getInstance();
        exchangeRatesCollection = db.collection("exchange_rates");
    }

    public void getCurrencyRatesForPeriod(Date startDate, Date endDate) {
            // Létrehozzuk a lekérdezést a Firestore adatbázisban
        Query query = exchangeRatesCollection
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate);
    }
}

