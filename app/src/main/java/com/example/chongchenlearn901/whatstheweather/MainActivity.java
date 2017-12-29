package com.example.chongchenlearn901.whatstheweather;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final String weathApi = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=d9ec9c93d8370dfc7621ef09b6258b47";

    private EditText etCity;
    private TextView tvWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.etCity = findViewById(R.id.etCity);
        this.tvWeather = findViewById(R.id.tvWeather);
        Button btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(searchListener);

    }

    private View.OnClickListener searchListener = (v) -> {
        String city = this.etCity.getText().toString();
        if(TextUtils.isEmpty(city)){
            this.tvWeather.setText("");
        }else{
            (new WeatherApiAsyncTask((s) -> {
                if(TextUtils.isEmpty(s)){
                    Toast.makeText(getApplicationContext(), "Could not find the weather.", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("weather");
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject weatherObj = jsonArray.getJSONObject(i);
                        sb.append(String.format("%s: %s\n", weatherObj.getString("main"), weatherObj.getString("description")));
                    }
                    this.tvWeather.setText(sb.toString());
                } catch (Exception e) {
                    Log.e(TAG, "click search had an error: ", e);
                }

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            })).execute(String.format(weathApi, city));
        }
    };

    private interface WeatherApiAsyncTaskCallback {
        void callback(String s);
    }

    private class WeatherApiAsyncTask extends AsyncTask<String, Void, String>{
        private static final String TAG = "WeatherApiAsyncTask";

        private WeatherApiAsyncTaskCallback callback;

        WeatherApiAsyncTask(WeatherApiAsyncTaskCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void onPostExecute(String s) {
            this.callback.callback(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            if(strings == null || strings.length == 0){
                return null;
            }
            HttpURLConnection connection = null;
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());

                char[] data = new char[1024];
                for(int i = reader.read(data); i != -1; i = reader.read(data)){
                    sb.append(data, 0, i);
                }

            } catch (Exception e) {
                // can not use toast here, let onPostExecute control it!
                //Toast.makeText(getApplicationContext(), "Could not find the weather.", Toast.LENGTH_SHORT).show();

                Log.e(TAG, "doInBackground: ", e);
                return null;
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
            return sb.toString();
        }
    }
}
