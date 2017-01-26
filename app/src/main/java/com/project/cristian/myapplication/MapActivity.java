package com.project.cristian.myapplication;

import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    Marker mCurrLocationMarker;
    Marker mBusMarker;
    LocationRequest mLocationRequest;
    String current_lat = null;
    String current_lon = null;

    public static final String TAG = MapActivity.class.getSimpleName();

    private String marker_name = null;
    private String marker_date = null;
    private String marker_time = null;
    private String marker_direction = null;

    private RadioButton closeRadio, twoRadio, fiveRadio;
    private static Marker mEraseMarker;

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.marker_description, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            //show description of bus station
            TextView titleTxt = (TextView)myContentsView.findViewById(R.id.titleTxt);
            TextView nameTxt = (TextView)myContentsView.findViewById(R.id.nameTxt);
            TextView timeTxt = (TextView)myContentsView.findViewById(R.id.timeTxt);
            TextView dateTxt = (TextView)myContentsView.findViewById(R.id.dateTxt);
            TextView directionTxt = (TextView)myContentsView.findViewById(R.id.directionTxt);
            titleTxt.setText(marker.getTitle() + ": " + marker.getSnippet());
            nameTxt.setText(marker_name);
            timeTxt.setText(marker_time);
            dateTxt.setText(marker_date);
            directionTxt.setText(marker_direction);
            return myContentsView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        AndroidNetworking.initialize(getApplicationContext());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        closeRadio = (RadioButton)findViewById(R.id.closeBtn);
        twoRadio = (RadioButton)findViewById(R.id.twoButton);
        fiveRadio = (RadioButton)findViewById(R.id.fiveButton);
        closeRadio.setChecked(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }else{
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(mCurrLocationMarker != null){
            mCurrLocationMarker.remove();
        }

        //LatLng userposition = new LatLng(49.610700, 6.112550);
        LatLng userposition = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markeroptions = new MarkerOptions();
        markeroptions.position(userposition);
        markeroptions.title("You are here!");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markeroptions);
        current_lat = String.valueOf(location.getLatitude());
        current_lon = String.valueOf(location.getLongitude());
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userposition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        //stop location updates
        if(mGoogleApiClient !=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public void HTTPTest(View view) throws ExecutionException, InterruptedException {
        mMap.clear();
        String radius = "";
        if ( closeRadio.isChecked() ){                  //check close radio button clicked
            radius = "200";                            // set radius 200m
        }else if ( twoRadio.isChecked() ){
            radius = "500";
        }else if ( fiveRadio.isChecked()) {
            radius = "1000";
        }
        Http jt = new Http(this);
        String y = current_lat.replaceAll("[.]","");
        y= y.substring(0, y.length()-1);
        String x = current_lon.replaceAll("[.]","");
        x =x.substring(0, x.length()-1);
        //LatLng userposition = new LatLng(49.610700, 6.112550);
        LatLng userposition = new LatLng(Double.valueOf(current_lat), Double.valueOf(current_lon));
        MarkerOptions markeroptions = new MarkerOptions();
        markeroptions.position(userposition);
        markeroptions.title("You are here!");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markeroptions);
        //x = "6112550";
        //y = "49610700";
        //jt.execute("http://travelplanner.mobiliteit.lu/hafas/query.exe/dot", "performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist="+radius+"&look_x="+x+"&look_y="+y+"", "6.112550", "49.610700");
        jt.execute("http://travelplanner.mobiliteit.lu/hafas/query.exe/dot", "performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist="+radius+"&look_x="+x+"&look_y="+y+"", current_lat, current_lon);
        ArrayList<BusStopCoordinate> busStopCoordinates = jt.get();
        if ( closeRadio.isChecked() && busStopCoordinates.size() != 0){
            //find close bus station in busStopCoordinates
            BusStopCoordinate closeBusStop = busStopCoordinates.get(0);
            for ( int i=1; i< busStopCoordinates.size(); i++){
                if ( busStopCoordinates.get(i).getDistanceFromOriginal() < closeBusStop.getDistanceFromOriginal() ){
                    closeBusStop = busStopCoordinates.get(i);
                }
            }
            busStopCoordinates.clear();
            busStopCoordinates.add(closeBusStop);
        }
        showBusStopsOnMap(busStopCoordinates);
    }

    public void LoadVeloh(View view) throws ExecutionException, InterruptedException {
        Json jt = new Json(this);
        jt.execute("https://developer.jcdecaux.com/rest/vls/stations/Luxembourg.json","");

        String content = jt.get();

        try {
            JSONArray bikeStations = new JSONArray(content);
            for(int i = 0 ; i < bikeStations.length() ; i++){
                String address = bikeStations.getJSONObject(i).getString("address");
                double latitude = Double.parseDouble(bikeStations.getJSONObject(i).getString("latitude"));
                double longitude = Double.parseDouble(bikeStations.getJSONObject(i).getString("longitude"));
                String name = bikeStations.getJSONObject(i).getString("name");
                String number = bikeStations.getJSONObject(i).getString("number");

                Log.d("Bike Station: ", name+"\t"+latitude+"\t"+longitude);

                LatLng bikeStation = new LatLng(latitude,longitude);
                MarkerOptions markeroptions = new MarkerOptions();
                markeroptions.position(bikeStation);
                markeroptions.title("Veloh Station: "+name);
                markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mBusMarker = mMap.addMarker(markeroptions);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void drawRoute(View view) throws ExecutionException, InterruptedException {
        //map clear and add  current location marker again
        mMap.clear();
        //LatLng userposition = new LatLng(49.610700, 6.112550);
        LatLng userposition = new LatLng(Double.valueOf(current_lat), Double.valueOf(current_lon) );
        MarkerOptions markeroptions = new MarkerOptions();
        markeroptions.position(userposition);
        markeroptions.title("You are here!");
        mCurrLocationMarker = mMap.addMarker(markeroptions);

        if ( mEraseMarker != null) {
            //Draw route on map
            //drawRouteOnMap("49.610700", "6.112550", String.valueOf(mEraseMarker.getPosition().latitude), String.valueOf(mEraseMarker.getPosition().longitude), false);
            drawRouteOnMap(current_lat, current_lon, String.valueOf(mEraseMarker.getPosition().latitude), String.valueOf(mEraseMarker.getPosition().longitude), false);
        }
        if ( mEraseMarker != null &&  mEraseMarker.getTitle().equals("Bus Station")){        //Redraw bus station markers bcz map is cleared - same with when bus station button is clicked
            String radius = "";
            if ( closeRadio.isChecked() ){
                radius = "200";
            }else if ( twoRadio.isChecked() ){
                radius = "500";
            }else if ( fiveRadio.isChecked()) {
                radius = "1000";
            }
            Http jt = new Http(this);
            String y = current_lat.replaceAll("[.]","");
            y= y.substring(0, y.length()-1);
            String x = current_lon.replaceAll("[.]","");
            x =x.substring(0, x.length()-1);
            //x = "6112550";
            //y = "49610700";
            //jt.execute("http://travelplanner.mobiliteit.lu/hafas/query.exe/dot", "performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist="+radius+"&look_x="+x+"&look_y="+y+"", "6.112550", "49.610700");
            jt.execute("http://travelplanner.mobiliteit.lu/hafas/query.exe/dot", "performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist="+radius+"&look_x="+x+"&look_y="+y+"", current_lat, current_lon);
            ArrayList<BusStopCoordinate> busStopCoordinates = jt.get();
            if ( closeRadio.isChecked() && busStopCoordinates.size() != 0){
                BusStopCoordinate closeBusStop = busStopCoordinates.get(0);
                for ( int i=1; i< busStopCoordinates.size(); i++){
                    if ( busStopCoordinates.get(i).getDistanceFromOriginal() < closeBusStop.getDistanceFromOriginal() ){
                        closeBusStop = busStopCoordinates.get(i);
                    }
                }
                busStopCoordinates.clear();
                busStopCoordinates.add(closeBusStop);
            }
            showBusStopsOnMap(busStopCoordinates);
        }else{if ( mEraseMarker != null &&  mEraseMarker.getTitle().equals("Veloh Station")) {                                                          //Redraw veloh markers bcz map is cleared - same with when veloh button is clicked
            Json jt = new Json(this);
            jt.execute("https://developer.jcdecaux.com/rest/vls/stations/Luxembourg.json", "");
            String content = jt.get();
            try {
                JSONArray bikeStations = new JSONArray(content);
                for (int i = 0; i < bikeStations.length(); i++) {
                    String address = bikeStations.getJSONObject(i).getString("address");
                    double latitude = Double.parseDouble(bikeStations.getJSONObject(i).getString("latitude"));
                    double longitude = Double.parseDouble(bikeStations.getJSONObject(i).getString("longitude"));
                    String name = bikeStations.getJSONObject(i).getString("name");
                    String number = bikeStations.getJSONObject(i).getString("number");

                    Log.d("Bike Station: ", name + "\t" + latitude + "\t" + longitude);

                    LatLng bikeStation = new LatLng(latitude, longitude);
                    MarkerOptions markeroption = new MarkerOptions();
                    markeroption.position(bikeStation);
                    markeroption.title("Veloh Station: " + name);
                    markeroption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mBusMarker = mMap.addMarker(markeroption);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        }

    }

    public void showBusStopsOnMap(ArrayList<BusStopCoordinate> busStopCoordinates){     // draw  bus station marker on map
        for(int i =0;i<busStopCoordinates.size();i++){
            Log.d("Busstops",busStopCoordinates.get(i).getX()+ " - "+busStopCoordinates.get(i).getY());
            LatLng busStop = new LatLng(busStopCoordinates.get(i).getY(),busStopCoordinates.get(i).getX());
            MarkerOptions markeroptions = new MarkerOptions();
            markeroptions.position(busStop);
            markeroptions.title("Bus Station");
            markeroptions.snippet (busStopCoordinates.get(i).getName());
            markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mMap.setOnMarkerClickListener(this);

            mBusMarker = mMap.addMarker(markeroptions);
            mBusMarker.setTag(busStopCoordinates.get(i).getHttp());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mEraseMarker = marker;
        if(marker.getTitle().equals("Bus Station")){        //Show detail information about bus station
            mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
            String url = marker.getTag().toString();
            GetHttp jt = new GetHttp(this);
            jt.execute("http://travelplanner.mobiliteit.lu/restproxy/departureBoard", "accessId=cdt&format=json&"+url.replace(" ","%20"));
            Log.d("Logs", "http://travelplanner.mobiliteit.lu/restproxy/departureBoard?accessId=cdt&format=json&"+url.replace(" ","%20"));
            try {
                JSONObject res = new JSONObject(jt.get());
                JSONArray departure = res.getJSONArray("Departure");
                JSONObject obj = departure.getJSONObject(0);
                Log.d("Logs", "Obj: " + obj);
                marker_name = "Name: " + obj.getString("name");
                if ( obj.has("rtTime")) {
                    marker_time = "Time: " + obj.getString("rtTime");
                }else if ( obj.has("Time"))
                    marker_time = "Time: " + obj.getString("time");
                if ( obj.has("rtDate")) {
                    marker_date = "Date: " + obj.getString("rtDate");
                }
                else if ( obj.has("date")){
                    marker_date = "Date: " + obj.getString("date");
                }else
                    marker_date = "Date: " + obj.getString("date");
                marker_direction = "!Direction: " + obj.getString("direction");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }else{
            mMap.setInfoWindowAdapter(null);
        }
        return false;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int addstopcount = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (addstopcount < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(addstopcount++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(addstopcount++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    // draw route on Map by using Google Web Play service - call when draw route button clicked
    public void drawRouteOnMap(final String start_lat, final String start_lon, final String dest_lat, final String dest_lon, final Boolean eraseflag){
        AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json")
                .addQueryParameter("sensor", "false")
                .addQueryParameter("units", "metric")
                .addQueryParameter("mode", "walking")
                .addQueryParameter("key", "AIzaSyA7WHVeZpIn71mqFQcRzsVsaKzDz88PlpY")
                .addQueryParameter("origin", start_lat+","+start_lon)
                .addQueryParameter("destination", dest_lat+","+dest_lon)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray routeArray = response.getJSONArray("routes");
                            JSONObject routes = routeArray.getJSONObject(0);
                            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                            String encodedString = overviewPolylines.getString("points");
                            List<LatLng> list = decodePoly(encodedString);
                            if ( eraseflag == true){
                                Polyline line = mMap.addPolyline(new PolylineOptions()
                                        .addAll(list)
                                        .width(14)
                                        .color(Color.parseColor("#FFFFFF"))
                                        .geodesic(true)
                                );
                            }else {
                                Polyline line = mMap.addPolyline(new PolylineOptions()
                                        .addAll(list)
                                        .width(12)
                                        .color(Color.parseColor("#05b1fb"))
                                        .geodesic(true)
                                );
                            }
                        }
                        catch (JSONException e) {
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                    }
                });
    }
}

//testing