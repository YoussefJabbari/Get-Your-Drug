package com.example.youssef.getyourdrug;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youssef.getyourdrug.entities.Medicament;
import com.example.youssef.getyourdrug.entities.Pharmacie;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RechercheActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    String result = null;

    public List<Pharmacie> pharmacies = new ArrayList<>();
    public List<Medicament> medicaments = new ArrayList<>();

    Medicament medicament = null;

    private static final String TAG = "RechercheActivity";

    private static final String FINAL_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private Boolean mLocationPermissionsGranted = false;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng myLocation = null;

    //widgets
    private EditText mInputRecherche;
    private ImageView mMyLocation;
    private ImageView menu;
    private ListView mListView;
    private Button reserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recherche);

        menu = (ImageView) findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RechercheActivity.this, UtilisateurActivity.class);
                startActivity(intent);
                RechercheActivity.this.finish();
            }
        });

        mInputRecherche = (EditText) findViewById(R.id.input_recherche);
        mMyLocation = (ImageView) findViewById(R.id.myLocation);
        mListView = (ListView) findViewById(R.id.medicaments_search);
        reserver = (Button) findViewById(R.id.reserver);

        getLocationPermission();
    }

    @Override
    public void onBackPressed() {
        if (mListView.getVisibility() == View.VISIBLE) {
            mListView.setVisibility(View.INVISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    private void init() {
        mInputRecherche.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE ) {

                    //Réinitialiser la map
                    hideKeyboard();
                    mMap.clear();
                    reserver.setVisibility(View.INVISIBLE);
                    //Lancer la recherche
                    rechercherMedicaments();

                }
                return false;
            }
        });

        mMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });
    }

    private void rechercherMedicaments () {

        String recherche = mInputRecherche.getText().toString();

        //Retourner les médicament
        String adresse = "http://"+ LoginActivity.IP +"/GetYourDrug/Medicaments/searchMedicament.php";
        try {
            String resultat = new RetrieveData().execute("medicaments", adresse, recherche).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        mListView.setVisibility(View.VISIBLE);
        ArrayAdapter<Medicament> adapter = new ArrayAdapter<Medicament>(this, android.R.layout.simple_list_item_2, android.R.id.text1, medicaments) {
            @NonNull
            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setTextSize(18);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text2.setTextColor(Color.GRAY);

                text1.setText(medicaments.get(position).getNom());
                text2.setText("" + medicaments.get(position).getPrix() + "MAD");
                return view;
            }
        };
        mListView.setAdapter(adapter);

        if (medicaments.size() == 0) {
            this.onBackPressed();
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                medicament = new Medicament(medicaments.get(i));
                getPharmacies("" + medicaments.get(i).getId());
                RechercheActivity.this.onBackPressed();
            }
        });
    }

    private void getPharmacies (final String medicament_id) {

        String adresse = "http://"+ LoginActivity.IP +"/GetYourDrug/Pharmacies/getPharmaciesMedicament.php";

        //Retourner les pharmacies
        try {
            String resultat = new RetrieveData().execute("pharmacies", adresse, medicament_id).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        for (Pharmacie pharmacie : pharmacies) {
            LatLng latLng = new LatLng(pharmacie.getLatitude(), pharmacie.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(pharmacie.getNom()));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                marker.showInfoWindow();
                reserver.setVisibility(View.INVISIBLE);

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(final Marker marker) {

                        //Lancer la page de réservation
                        reserver.setVisibility(View.VISIBLE);
                        reserver.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Pharmacie pharmacie = Pharmacie.findByLatLng(marker.getPosition(), pharmacies);
                                if (pharmacie != null)
                                {
                                    SharedPreferences preferences = getSharedPreferences("DATA", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();

                                    editor.putInt("medicament_id", medicament.getId());
                                    editor.putInt("pharmacie_id", pharmacie.getId());
                                    editor.putString("medicament", medicament.getNom());
                                    editor.putString("pharmacie", pharmacie.getNom());
                                    editor.apply();

                                    Intent intent = new Intent(RechercheActivity.this, ReserveActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(RechercheActivity.this, "Un problème est survenu!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                return false;
            }
        });

    }

    private void LancerMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(RechercheActivity.this);
    }



    /* --------------------------------------------------------------------- */

    private void hideKeyboard () {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getLocationPermission () {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINAL_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                LancerMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation () {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                if (location != null) {
                    location.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location currentLocation = (Location) task.getResult();
                                myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f);
                            } else {
                                Toast.makeText(RechercheActivity.this, "Impossible de trouver votre location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(RechercheActivity.this, "Location NULL", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE : {
                if (grantResults.length > 0 ){
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    LancerMap();
                }
            }
        }
    }



    @SuppressLint("StaticFieldLeak")
    private class RetrieveData extends AsyncTask<String, String, String>
    {

        //ProgressDialog pdLoading = new ProgressDialog(RechercheActivity.this);
        HttpURLConnection conn;
        URL url = null;
        int response_code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            //pdLoading.setMessage("\tLoading...");
            //pdLoading.setCancelable(false);
            //pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL(params[1]);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Exception 1";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", params[2]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                e1.printStackTrace();
                return "Exception 2";
            }

            try {

                response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder objets = new StringBuilder();
                    String line = reader.readLine();

                    //Réinitialiser
                    medicaments.clear();
                    pharmacies.clear();

                    if (line.equalsIgnoreCase("exist"))
                    {
                        while ((line = reader.readLine()) != null) {
                            objets.append(line);
                        }
                        result = objets.toString();
                    }
                    else
                    {
                        objets.append(line);
                        return (objets.toString());
                    }

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "Exception 3";
            }
            //PARSE JSON DATA
            try
            {
                JSONArray ja = new JSONArray(result);
                JSONObject jo;

                for (int i=0 ; i<ja.length() ; i++)
                {
                    jo = ja.getJSONObject(i);

                    switch (params[0]) {
                        case "medicaments" :
                            medicaments.add(new Medicament(jo.getInt("id"), jo.getString("nom"),
                                    jo.getDouble("prix"), jo.getString("image")));
                            break;

                        case "pharmacies" :
                            pharmacies.add(new Pharmacie(jo.getInt("id"), jo.getString("nom"),
                                    jo.getString("adresse"), jo.getString("ville"),
                                    jo.getDouble("latitude"), jo.getDouble("longitude")));
                            break;
                    }

                }
                return "done";

            } catch (JSONException e) {
                e.printStackTrace();
                return "exception 4";
            }
            finally {
                conn.disconnect();
            }

        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            //pdLoading.dismiss();
            Toast.makeText(RechercheActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }
}
