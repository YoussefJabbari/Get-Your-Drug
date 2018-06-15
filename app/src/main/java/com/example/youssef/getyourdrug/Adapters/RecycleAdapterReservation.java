package com.example.youssef.getyourdrug.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youssef.getyourdrug.LoginActivity;
import com.example.youssef.getyourdrug.R;
import com.example.youssef.getyourdrug.UpdateReservationActivity;
import com.example.youssef.getyourdrug.entities.Reservation;

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
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by youssef on 09/06/2018.
 */

public class RecycleAdapterReservation extends RecyclerView.Adapter<RecycleAdapterReservation.MyViewHolder> {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    private Context context;
    private List<Reservation> itemList;
    private Reservation reservation;

    private Bitmap bmp = null;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView medicament, pharmacie, quantite, dateReservation;

        public MyViewHolder(View view) {
            super(view);

            image = (ImageView) view.findViewById(R.id.image);
            medicament = (TextView) view.findViewById(R.id.medicament);
            pharmacie = (TextView) view.findViewById(R.id.pharmacie);
            quantite = (TextView) view.findViewById(R.id.quantite);
            dateReservation = (TextView) view.findViewById(R.id.dateReservation);

        }
    }

    public RecycleAdapterReservation(Context mainActivityContacts,List<Reservation> reservations) {
        this.itemList = reservations;
        this.context = mainActivityContacts;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        return new MyViewHolder(itemView);
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        reservation = itemList.get(position);

        holder.medicament.setText(reservation.getMedicament());
        holder.pharmacie.setText(reservation.getPharmacie());
        holder.quantite.setText("Quantité : " + reservation.getQuantite());
        holder.dateReservation.setText(reservation.getStrDateReservation());

        try {
            Void res = new MyDownloadTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        holder.image.setImageBitmap(bmp);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = context.getSharedPreferences("DATA", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("reservation_id", itemList.get(position).getId());
                editor.apply();
                Intent intent = new Intent(context, UpdateReservationActivity.class);
                context.startActivity(intent);
                ( (Activity) context).finish();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            int choix = -1;
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                String[] choices = new String[2];
                choices[0] = "Modifier la réservation";
                choices[1] = "Annuler la réservation";
                // set dialog message
                alertDialogBuilder.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        choix = whichButton;
                    }
                });
                alertDialogBuilder.setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (choix == 0)
                        {
                            SharedPreferences preferences = context.getSharedPreferences("DATA", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("reservation_id", itemList.get(position).getId());
                            editor.apply();
                            Intent intent = new Intent(context, UpdateReservationActivity.class);
                            context.startActivity(intent);
                            ( (Activity) context).finish();
                        }
                        else if (choix == 1)
                        {
                            new AsyncAnnulerReservation().execute("" + itemList.get(position).getId());
                        }
                    }
                });

                alertDialogBuilder.setCancelable(true).setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("StaticFieldLeak")
    class MyDownloadTask extends AsyncTask<Void,Void,Void> {


        protected void onPreExecute() {
            //display progress dialog.

        }

        protected Void doInBackground(Void... params) {
            URL url = null;
            try {
                url = new URL("http://" + LoginActivity.IP + reservation.getImage());
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            // dismiss progress dialog and update ui
        }
    }




    @SuppressLint("StaticFieldLeak")
    private class AsyncAnnulerReservation extends AsyncTask<String, String, String>
    {
        //ProgressDialog pdLoading = new ProgressDialog(UtilisateurActivity.this);
        HttpURLConnection conn;
        URL url = null;
        int response_code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            /*pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();*/

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // Enter URL address where your php file resides
                url = new URL("http://" + LoginActivity.IP + "/GetYourDrug/Reservations/deleteReservation.php");
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
                        .appendQueryParameter("id", params[0]);
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

                Toast.makeText(context, "La réservation a été annulée", Toast.LENGTH_LONG).show();
                ((Activity) context).finish();
                context.startActivity( ((Activity) context).getIntent() );

            }else if (result.equalsIgnoreCase("false")){

                Toast.makeText(context, "Annulation refusée", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") ) {

                Toast.makeText(context, "Something went wrong! Exception. "+result, Toast.LENGTH_LONG).show();

            } else if ( result.equalsIgnoreCase("unsuccessful")){
                Toast.makeText(context, "Something went wrong! CNX. "+response_code, Toast.LENGTH_LONG).show();
            }
        }
    }
}



