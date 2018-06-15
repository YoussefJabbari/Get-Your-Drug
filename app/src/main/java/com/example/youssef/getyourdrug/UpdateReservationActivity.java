package com.example.youssef.getyourdrug;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.youssef.getyourdrug.entities.Affectation;
import com.example.youssef.getyourdrug.entities.Reservation;
import com.example.youssef.getyourdrug.entities.Utilisateur;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class UpdateReservationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public String result = null;
    public String adresse = null;

    // UI references.
    private EditText mMedicamentView;
    private EditText mPharmacieView;
    private EditText mQteView;
    private EditText mDateReservationView;
    private EditText mDateAchatView;
    private View mProgressView;
    private View mLoginFormView;
    Calendar myCalendar = Calendar.getInstance();

    //DATA
    public int reservation_id;

    public Reservation reservation = null;

    public Affectation affectation = null;

    public Utilisateur utilisateur = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);

        setTitle("Modifier Réservation");
        // Set up the login form.
        mMedicamentView = (EditText) findViewById(R.id.medicament);
        mPharmacieView = (EditText) findViewById(R.id.pharmacie);
        mDateReservationView = (EditText) findViewById(R.id.dateReservation);
        mQteView = (EditText) findViewById(R.id.quantite);
        mDateAchatView = (EditText) findViewById(R.id.dateAchat);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel(mDateAchatView, myCalendar.getTime());
            }

        };

        mDateAchatView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(UpdateReservationActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Button mReserveButton = (Button) findViewById(R.id.reserveButton);
        mReserveButton.setText("Modifier");
        mReserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptReserve();
            }
        });

        SharedPreferences preferences = getSharedPreferences("DATA", MODE_PRIVATE);
        utilisateur = new Utilisateur(preferences.getInt("id", 0), preferences.getString("cin", ""),
                preferences.getString("nom", ""), preferences.getString("prenom", ""),
                preferences.getString("email", ""), preferences.getString("password", ""),
                preferences.getString("tel", ""), preferences.getString("adresse" ,""));
        reservation_id = preferences.getInt("reservation_id", 0);

        adresse = "http://"+ LoginActivity.IP +"/GetYourDrug/Reservations/getReservation.php";
        try{
            String retrieve = new RetrieveData().execute("reservation", adresse, "" + reservation_id).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        mMedicamentView.setText(reservation.getMedicament());
        mPharmacieView.setText(reservation.getPharmacie());
        mDateReservationView.setText(reservation.getStrDateReservation());
        mQteView.setText(""+reservation.getQuantite());
        mDateAchatView.setText(reservation.getStrDateAchat());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        adresse = "http://"+ LoginActivity.IP +"/GetYourDrug/Affectations/getAffectation.php";
        try {
            String s = new RetrieveData().execute("affectation", adresse, "" + reservation.getAffectation_id()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void updateDateLabel(EditText edittext, Date date)
    {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        edittext.setText(sdf.format(date));
    }

    private void attemptReserve() {

        // Reset errors.
        mQteView.setError(null);
        mDateAchatView.setError(null);

        // Store values at the time of the login attempt.
        String quantite  = mQteView.getText().toString();
        String dateAchat = mDateAchatView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(quantite)) {
            mQteView.setError(getString(R.string.error_field_required));
            focusView = mQteView;
            cancel = true;
        }
        else if (Integer.parseInt(quantite) > affectation.getQuantite()) {
            mQteView.setError(getString(R.string.error_over_stock));
            focusView = mQteView;
            cancel = true;
        }
        if (TextUtils.isEmpty(dateAchat)) {
            mDateAchatView.setError(getString(R.string.error_field_required));
            focusView = mDateAchatView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new AsyncUpdateReservation().execute(""+reservation.getId(), quantite, dateAchat);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UpdateReservationActivity.this, UtilisateurActivity.class);
        startActivity(intent);
        UpdateReservationActivity.this.finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @SuppressLint("StaticFieldLeak")
    private class RetrieveData extends AsyncTask<String, String, String>
    {

        ProgressDialog pdLoading = new ProgressDialog(UpdateReservationActivity.this);
        HttpURLConnection conn;
        URL url = null;
        int response_code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
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
                JSONObject jo = new JSONObject(result);

                switch (params[0]) {
                    case "reservation" :
                        String strReservation;
                        String strDate;
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        strReservation = jo.getString("dateReservation");
                        strDate        = jo.getString("dateAchat");

                        reservation = new Reservation(jo.getInt("id"), jo.getInt("utilisateur_id"),
                                jo.getInt("affectation_id"), jo.getInt("quantite"),
                                sdf.parse(strReservation), sdf.parse(strDate),
                                jo.getString("pharmacie"), jo.getString("medicament"), jo.getString("image"));
                        break;
                    case "affectation":
                        affectation = new Affectation(jo.getInt("id"), jo.getInt("medicament_id"),
                                jo.getInt("pharmacie_id"), jo.getInt("quantite"));
                        break;
                }

                return "done";

            } catch (JSONException e) {
                e.printStackTrace();
                return "exception 4";
            } catch (ParseException e) {
                e.printStackTrace();
                return "exception 5";
            }
            finally {
                conn.disconnect();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            pdLoading.dismiss();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncUpdateReservation extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(UpdateReservationActivity.this);
        HttpURLConnection conn;
        URL url = null;
        int response_code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://"+ LoginActivity.IP +"/GetYourDrug/Reservations/updateReservation.php");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception";
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
                        .appendQueryParameter("id", params[0])
                        .appendQueryParameter("quantite", params[1])
                        .appendQueryParameter("dateAchat", params[2]);
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
                return "exception";
            }

            try {

                response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }

        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if(result.equalsIgnoreCase("true"))
            {

                Toast.makeText(UpdateReservationActivity.this, "Modification validée", Toast.LENGTH_LONG).show();
                UpdateReservationActivity.this.onBackPressed();

            }else if (result.equalsIgnoreCase("false")){

                Toast.makeText(UpdateReservationActivity.this, "Les données ne sont pas valides", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") ) {

                Toast.makeText(UpdateReservationActivity.this, "Something went wrong! Exception. " + result, Toast.LENGTH_LONG).show();

            } else if ( result.equalsIgnoreCase("unsuccessful")){
                Toast.makeText(UpdateReservationActivity.this, "Something went wrong! CNX. " + response_code, Toast.LENGTH_LONG).show();
            }
        }
    }

}
