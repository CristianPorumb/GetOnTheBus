package com.project.cristian.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;



public class Http extends AsyncTask<String,String,ArrayList<BusStopCoordinate>> {
    private ProgressDialog pd;
    private Activity act;
    ArrayList<BusStopCoordinate> busStopCoordinates;

    public Http(Activity act) {
        this.act=act;
    }

    public double CalculationByDistance(double start_lat, double start_lon, double stop_lat, double stop_lon) {
        int Radius = 6371;// radius of earth in Km
        double dLat = toRadians(stop_lat - start_lat);
        double dLon = toRadians(stop_lon - start_lon);
        double a = sin(dLat / 2) * sin(dLat / 2)
                + cos(toRadians(start_lat))
                * cos(toRadians(stop_lat)) * sin(dLon / 2)
                * sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        DecimalFormat newFormat = new DecimalFormat("####");
        double meter = Radius * c % 1000;
        return meter;
    }

    public static long getDistanceMeters(double lat1, double lng1, double lat2, double lng2) {

        double l1 = toRadians(lat1);
        double l2 = toRadians(lat2);
        double g1 = toRadians(lng1);
        double g2 = toRadians(lng2);

        double dist = acos(sin(l1) * sin(l2) + cos(l1) * cos(l2) * cos(g1 - g2));
        if(dist < 0) {
            dist = dist + Math.PI;
        }

        return Math.round(dist * 6378100);
    }

    public ArrayList<BusStopCoordinate> getBusStopCoordinates(){
        return busStopCoordinates;
    }
    protected void onPreExecute(){
        super.onPreExecute();
        pd = new ProgressDialog(act);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();
    }
    @Override
    protected ArrayList<BusStopCoordinate> doInBackground(String... params) {
        HttpURLConnection connection = null;
        busStopCoordinates = new ArrayList<BusStopCoordinate>();
        try{
            URL url = new URL(params[0]);
            Log.d("URL: ","> "+params[0]);

            connection = (HttpURLConnection) url.openConnection();
            if ( connection != null) {
                String sb = null;
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");

                sb = params[1];
                byte[] postData      = sb.toString().getBytes( Charset.forName("UTF-8") );
                int    postDataLength = postData.length;
                connection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));

                OutputStreamWriter ow = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                PrintWriter pw = new PrintWriter(ow);
                pw.write(sb.toString());
                pw.flush();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    Log.d("String", "> " + inputLine);
                    Log.d("String X: ", "> " + inputLine.substring(inputLine.indexOf("@X=") + 3, inputLine.indexOf("@X=") + 11));
                    Log.d("String Y: ", "> " + inputLine.substring(inputLine.indexOf("@Y=") + 3, inputLine.indexOf("@Y=") + 12));
                    double x = Double.parseDouble(inputLine.substring(inputLine.indexOf("@X=") + 3, inputLine.indexOf("@X=") + 11).replace(",", "."));
                    double y = Double.parseDouble(inputLine.substring(inputLine.indexOf("@Y=") + 3, inputLine.indexOf("@Y=") + 12).replace(",", "."));
                    String name = inputLine.substring(inputLine.indexOf("id=A=1@O=") + 9, inputLine.indexOf("@X="));

                    BusStopCoordinate busStop = new BusStopCoordinate(name, x, y, inputLine, getDistanceMeters(Double.valueOf(params[2]), Double.valueOf(params[3]), x, y));
                    busStopCoordinates.add(busStop);
                }
                in.close();
            }
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return busStopCoordinates;
    }
    @Override
    protected void onPostExecute(ArrayList<BusStopCoordinate> result) {
        super.onPostExecute(result);

        if(pd.isShowing()){
            pd.dismiss();
        }

    }
}
