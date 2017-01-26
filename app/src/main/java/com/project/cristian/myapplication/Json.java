package com.project.cristian.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class Json extends AsyncTask<String,String,String> {
    private ProgressDialog pd;
    private Activity act;

    public Json(Activity act) {
        this.act=act;
    }
    protected void onPreExecute(){
        super.onPreExecute();


        pd = new ProgressDialog(act);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();
    }
    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try{
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line ="";

            while((line = reader.readLine()) !=null){
                buffer.append(line+"\n");
                Log.d("Response: ","> "+line);
            }
            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(connection != null){
                connection.disconnect();
            }
            try{
                if(reader !=null){
                    reader.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(pd.isShowing()){
            pd.dismiss();
        }

    }
}