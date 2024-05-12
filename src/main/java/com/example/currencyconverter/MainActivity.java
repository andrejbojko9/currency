package com.example.currencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Spinner spinner1, spinner2;
    private EditText editText;
    private TextView result;
    private Button submit;
    private ProgressBar load, submitload;
    private String input = "";
    private String localch1 = null;
    private String localch2 = null;
    private DatabaseReference mDatabase;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final String CHANNEL_ID = "my_channel";
    private TrafficDataRepository trafficDataRepository;
    private CurrencyRatesRepository currencyRatesRepository;

    private ListView listViewTraffic;
    private RecyclerView recyclerViewCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        editText = findViewById(R.id.edittext);
        result = findViewById(R.id.showresult);
        submit = findViewById(R.id.button);
        load = findViewById(R.id.load);
        submitload = findViewById(R.id.submit_load);

        HELLOMETHOD();

        /*if (checkPermission()) {
            // Ha az engedély már megvan, végezze el a szükséges műveleteket
            // Például: hozzáférés a tárolóhoz
            performFileOperation();
        } else {
            // Ha nincs engedély, kérjen engedélyt
            requestPermission();
        }
        createNotificationChannel();*/

        // Értesítés létrehozása és megjelenítése
        createNotification();
        trafficDataRepository = new TrafficDataRepository();

        currencyRatesRepository = new CurrencyRatesRepository();

        listViewTraffic = findViewById(R.id.listViewTraffic);

        recyclerViewCurrency = findViewById(R.id.recyclerViewCurrency);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitload.setVisibility(View.VISIBLE);
                String input = editText.getText().toString();
                String s1 = spinner1.getSelectedItem().toString();
                String s2 = spinner2.getSelectedItem().toString();
                String real1 = s1.substring(s1.length() - 3);
                String real2 = s2.substring(s2.length() - 3);
                if (TextUtils.isEmpty(input)) {
                    Toast.makeText(MainActivity.this, "Enter some value....", Toast.LENGTH_SHORT).show();
                } else {
                    FetchData(input, real1, real2);
                }
            }
        });

    }

    private void HELLOMETHOD() {
        load.setVisibility(View.VISIBLE);

        List<String> list = new ArrayList<>();
        list.add("USD - United States Dollar");
        list.add("EUR - Euro");
        list.add("GBP - British Pound Sterling");
        list.add("JPY - Japanese Yen");
        list.add("AUD - Australian Dollar");
        list.add("HUF - Hungarian Forint");
        list.add("RSD - Serbian Dinar");


        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(arrayAdapter1);

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(arrayAdapter2);

        load.setVisibility(View.GONE);
    }

    private void FetchData(String input, String real1, String real2) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://data.fixer.io/api/convert?access_key=YOUR_ACCESS_KEY&from="+real1+"&to="+real2+"&amount="+input;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String resultString = jsonObject.getString("result");
                            result.setText(resultString);
                            submitload.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            submitload.setVisibility(View.GONE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                submitload.setVisibility(View.GONE);
            }
        });

        queue.add(stringRequest);
    }

    private void writeDataToFirebase(String data) {
        // A Firebase adatbázisban létrehozunk egy új "currency" csomópontot és beállítjuk az értéket
        mDatabase.child("currency").setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Data has been written successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to write data to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void readDataFromFirebase() {
        // Firebase adatbázisban található "currency" csomópont értékének lekérése
        mDatabase.child("currency").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currencyValue = dataSnapshot.getValue(String.class);
                    // Az adatot itt kezelheted
                    // Például: result.setText(currencyValue);
                } else {
                    Toast.makeText(MainActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to read data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void writeCurrencyDataToFirebase(String currencyCode, String currencyValue) {
        // A Firebase adatbázisban létrehozunk egy új csomópontot a megadott valuta kód alapján és beállítjuk az értéket
        mDatabase.child("currencies").child(currencyCode).setValue(currencyValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Currency data has been written successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to write currency data to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void readCurrencyDataFromFirebase(String currencyCode) {
        // Firebase adatbázisban található csomópont értékének lekérése a megadott valuta kód alapján
        mDatabase.child("currencies").child(currencyCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currencyValue = dataSnapshot.getValue(String.class);
                    // Az adatot itt kezelheted
                    // Például: result.setText(currencyValue);
                } else {
                    Toast.makeText(MainActivity.this, "Currency data does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to read currency data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_auth) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*private boolean checkPermission() {
        // Ellenőrizze, hogy van-e engedély a tárolóhoz való hozzáféréshez
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // Engedély kérés
    private void requestPermission() {
        // Engedélyt kér a tárolóhoz való hozzáféréshez
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }*/

    // Engedélykérés eredményének kezelése
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Ha az engedélyt megadták, végezze el a szükséges műveleteket
                // Például: hozzáférés a tárolóhoz
                performFileOperation();
            } else {
                // Ha az engedélyt nem adták meg, tájékoztassa a felhasználót
                Toast.makeText(this, "Engedély megtagadva!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Tárolóhoz való hozzáférés szükséges műveletek elvégzése
    private void performFileOperation() {
        // Itt végezze el a tárolóhoz való hozzáféréshez szükséges műveleteket
        // Például: képek olvasása vagy írása
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Értesítés létrehozása és megjelenítése
    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Értesítés ikon
                .setContentTitle("My Notification") // Értesítés címe
                .setContentText("This is a notification from my app.") // Értesítés szövege
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // Értesítés prioritása

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build()); // Értesítés megjelenítése
    }
    public void onClickAuthenticate(View view) {

        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }



}