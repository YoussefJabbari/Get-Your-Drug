package com.example.youssef.getyourdrug;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.youssef.getyourdrug.entities.Utilisateur;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A login screen that offers login via email/password.
 */
public class ReserveActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public String result = null;

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
    public int medicament_id;
    public int pharmacie_id;

    public Utilisateur utilisateur = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);

        setTitle("Réserver Médicament");
        // Set up the login form.
        mMedicamentView = (EditText) findViewById(R.id.medicament);
        mPharmacieView = (EditText) findViewById(R.id.pharmacie);
        mQteView = (EditText) findViewById(R.id.quantite);
        mDateReservationView = (EditText) findViewById(R.id.dateReservation);
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

        mDateAchatView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReserveActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Button mReserveButton = (Button) findViewById(R.id.reserveButton);
        mReserveButton.setText("Réserver");
        mReserveButton.setOnClickListener(new OnClickListener() {
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

        medicament_id = preferences.getInt("medicament_id", 0);
        pharmacie_id  = preferences.getInt("pharmacie_id", 0);

        mMedicamentView.setText(preferences.getString("medicament", "Aucun médicament n'est séléctionné"));
        mPharmacieView.setText(preferences.getString("pharmacie", "Aucune pahrmacie n'est séléctionnée"));
        updateDateLabel(mDateReservationView, Calendar.getInstance().getTime());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void updateDateLabel(EditText edittext, Date date)
    {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        edittext.setText(sdf.format(date));
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
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
            mDateAchatView.setError(getString(R.string.error_field_required));
            focusView = mDateAchatView;
            cancel = true;
        }
        if (TextUtils.isEmpty(dateAchat)) {
            mQteView.setError(getString(R.string.error_field_required));
            focusView = mQteView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new AsyncAddReservation().execute("" + utilisateur.getId(), "" + medicament_id, "" + pharmacie_id, quantite, dateAchat);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

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
    private class AsyncAddReservation extends AsyncTask<String, String, String>
    {
        //ProgressDialog pdLoading = new ProgressDialog(ReserveActivity.this);
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
                url = new URL("http://"+ LoginActivity.IP +"/GetYourDrug/Reservations/addReservation.php");

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
                        .appendQueryParameter("utilisateur", params[0])
                        .appendQueryParameter("medicament", params[1])
                        .appendQueryParameter("pharmacie", params[2])
                        .appendQueryParameter("quantite", params[3])
                        .appendQueryParameter("dateAchat", params[4]);
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

            //pdLoading.dismiss();

            if(result.equalsIgnoreCase("true"))
            {

                Toast.makeText(ReserveActivity.this, "Reservation validée", Toast.LENGTH_LONG).show();
                ReserveActivity.this.onBackPressed();

            }else if (result.equalsIgnoreCase("false")){

                Toast.makeText(ReserveActivity.this, "Les données ne sont pas valides", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") ) {

                Toast.makeText(ReserveActivity.this, "Something went wrong! Exception. " + result, Toast.LENGTH_LONG).show();

            } else if ( result.equalsIgnoreCase("unsuccessful")){
                Toast.makeText(ReserveActivity.this, "Something went wrong! CNX. " + response_code, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ReserveActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}

