package com.project.cristian.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;



public class GetHttp extends AsyncTask<String,String,String> {
    private ProgressDialog pd;
    private Activity act;

    public GetHttp(Activity act) {
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
        StringBuilder returnValue = new StringBuilder(200);
        try{
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            if ( connection != null) {
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");

                byte[] postData      = params[1].getBytes( Charset.forName("UTF-8") );
                int    postDataLength = postData.length;
                connection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));

                OutputStreamWriter ow = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                PrintWriter pw = new PrintWriter(ow);
                pw.write(params[1]);
                pw.flush();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                String inputLine;

                while((inputLine = in.readLine()) !=null){
                    returnValue.append(inputLine+"\n");
                }
            }
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnValue.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(pd.isShowing()){
            pd.dismiss();
        }

    }
}