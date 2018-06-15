package com.example.youssef.getyourdrug;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * A login screen that offers login via email/password.
 */
public class SignupActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    // UI references.
    private EditText mCINView;
    private EditText mNomView;
    private EditText mPrenomView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mTelView;
    private EditText mAdresseView;
    private TextView logo;
    private TextView mSignUp;
    private TextView mSignInAccount;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Set up the login form.
        mCINView = (EditText) findViewById(R.id.cin);
        mNomView = (EditText) findViewById(R.id.nom);
        mPrenomView = (EditText) findViewById(R.id.prenom);
        mEmailView = (EditText) findViewById(R.id.email);
        mTelView = (EditText) findViewById(R.id.tel);
        mAdresseView = (EditText) findViewById(R.id.adresse);
        mPasswordView = (EditText) findViewById(R.id.password);

        mSignUp = (TextView) findViewById(R.id.sign_up);
        mSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        mSignInAccount = (TextView) findViewById(R.id.signin_acount);
        mSignInAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SignupActivity.this.onBackPressed();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {

        // Reset errors.
        mEmailView.setError(null);
        mCINView.setError(null);
        mNomView.setError(null);
        mPrenomView.setError(null);
        mTelView.setError(null);
        mAdresseView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String cin = mCINView.getText().toString();
        String nom = mNomView.getText().toString();
        String prenom = mPrenomView.getText().toString();
        String tel = mTelView.getText().toString();
        String adresse = mAdresseView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (TextUtils.isEmpty(cin)) {
            mCINView.setError(getString(R.string.error_field_required));
            focusView = mCINView;
            cancel = true;
        }
        if (TextUtils.isEmpty(nom)) {
            mNomView.setError(getString(R.string.error_field_required));
            focusView = mNomView;
            cancel = true;
        }
        if (TextUtils.isEmpty(prenom)) {
            mPrenomView.setError(getString(R.string.error_field_required));
            focusView = mPrenomView;
            cancel = true;
        }
        if (TextUtils.isEmpty(tel)) {
            mTelView.setError(getString(R.string.error_field_required));
            focusView = mTelView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            new AsyncSignup().execute(cin, nom, prenom, email, password, tel, adresse);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
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


    private class AsyncSignup extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(SignupActivity.this);
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
                url = new URL("http://" + LoginActivity.IP + "/GetYourDrug/signup.php");
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
                        .appendQueryParameter("cin", params[0])
                        .appendQueryParameter("nom",params[1])
                        .appendQueryParameter("prenom", params[2])
                        .appendQueryParameter("email", params[3])
                        .appendQueryParameter("password", params[4])
                        .appendQueryParameter("tel", params[5])
                        .appendQueryParameter("adresse", params[6]);
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

                Toast.makeText(SignupActivity.this, "Ajout validé", Toast.LENGTH_LONG).show();
                SignupActivity.this.onBackPressed();

            }else if (result.equalsIgnoreCase("false")){

                Toast.makeText(SignupActivity.this, "Les données ne sont pas valides", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") ) {

                Toast.makeText(SignupActivity.this, "Something went wrong! Exception. "+result, Toast.LENGTH_LONG).show();

            } else if ( result.equalsIgnoreCase("unsuccessful")){
                Toast.makeText(SignupActivity.this, "Something went wrong! CNX. "+response_code, Toast.LENGTH_LONG).show();
            }
        }
    }

}

