package com.example.youssef.getyourdrug.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.youssef.getyourdrug.Adapters.RecycleAdapterReservation;
import com.example.youssef.getyourdrug.LoginActivity;
import com.example.youssef.getyourdrug.R;
import com.example.youssef.getyourdrug.entities.Reservation;
import com.example.youssef.getyourdrug.entities.Utilisateur;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class BlankFragment extends Fragment {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public Utilisateur utilisateur = null;

    View view;

    protected String adresse;
    String result = null;

    public List<Reservation> reservations = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecycleAdapterReservation mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.content_utilisateur, container, false);

        //Get Infos
        SharedPreferences preferences = getActivity().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        utilisateur = new Utilisateur(preferences.getInt("id", 0), preferences.getString("cin", ""),
                preferences.getString("nom", ""), preferences.getString("prenom", ""),
                preferences.getString("email", ""), preferences.getString("password", ""),
                preferences.getString("tel", ""), preferences.getString("adresse" ,""));

        adresse = "http://"+ LoginActivity.IP +"/GetYourDrug/Reservations/getReservations.php";
        try {
            String retrieve = new RetrieveData().execute(adresse, ""+utilisateur.getId()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_property);

        mAdapter = new RecycleAdapterReservation(getActivity(), reservations);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        return view;
    }


    @SuppressLint("StaticFieldLeak")
    private class RetrieveData extends AsyncTask<String, String, String>
    {

        //ProgressDialog pdLoading = new ProgressDialog(UtilisateurActivity.this);
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
                url = new URL(params[0]);

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
                        .appendQueryParameter("id", params[1]);
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
                JSONArray ja = new JSONArray(result);
                JSONObject jo;
                //reservations.clear();
                String strReservation;
                String strDate;
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                for (int i=0 ; i<ja.length() ; i++)
                {
                    jo = ja.getJSONObject(i);

                    strReservation = jo.getString("dateReservation");
                    strDate        = jo.getString("dateAchat");

                    reservations.add(new Reservation(jo.getInt("id"), jo.getInt("utilisateur_id"),
                            jo.getInt("affectation_id"), jo.getInt("quantite"),
                            sdf.parse(strReservation), sdf.parse(strDate),
                            jo.getString("pharmacie"), jo.getString("medicament"), jo.getString("image")));

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

            //this method will be running on UI thread

            //pdLoading.dismiss();
            //Toast.makeText(BlankFragment.this.getContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}
