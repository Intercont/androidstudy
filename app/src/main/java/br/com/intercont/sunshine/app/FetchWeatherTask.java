/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.intercont.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import br.com.intercont.sunshine.app.data.WeatherContract;
import br.com.intercont.sunshine.app.data.WeatherContract.WeatherEntry;
import br.com.intercont.sunshine.app.data.WeatherDbHelper;

public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private final Context mContext;

    //MINHA SOLU��O - Mapas, coordenadas para o Maps
    public static double coordLatitude;
    public static double coordLongitude;

    private boolean DEBUG = true;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        WeatherDbHelper mOpenHelper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor retCursor;
        long _id;

            //SOLU��O DO CURSO - Consulto no banco usando o ContentResolver, para fazer uma query,
            // usando a URI do Location no Contract, que j� passa todos os par�metros para a consulta
            retCursor = mContext.getContentResolver().query(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    new String[]{WeatherContract.LocationEntry._ID},
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?", //o curso usou somente LocationSetting de par�metro de busca j� que pela URI do ContentProvider somente ser� poss�vel buscar usando esse par�metro
                    new String[]{locationSetting},
                    null
            );

            //se j� tiver um valor
            if (retCursor.moveToFirst()){
                _id = retCursor.getLong(retCursor.getColumnIndex(WeatherContract.LocationEntry._ID));
            }else{
                //se n�o, insiro o valor
                ContentValues values = new ContentValues();
                values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
                values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
                values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
                values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

                //SOLU��O DO CURSO - Insiro fazendo uso do ContentProvider com a URI
                Uri insertedUri = mContext.getContentResolver().insert(
                        WeatherContract.LocationEntry.CONTENT_URI,
                        values
                );
                //SOLU��O DO CURSO - A URI cont�m um ID para a row. Extraio a locationId da Uri
                _id = ContentUris.parseId(insertedUri);
            }
        retCursor.close();
        return _id;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    //REFACTOR LI��O 4C - muda o retorno do getWeatherDataFromJson
    private void getWeatherDataFromJson(String forecastJsonStr,
                                            String locationSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        //MINHA SOLU��O - Mostrar a localiza��o no Mapa
        final String OWM_LOCATION = "city";
        final String OWM_LOCATION_COORD = "coord";
        final String OWM_LOCATION_COORD_LAT = "lat";
        final String OWM_LOCATION_COORD_LON = "lon";
        //MINHA SOLU��O - Mostrar a localiza��o no Mapa

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            //MINHA SOLU��O - Mostrar a localiza��o no Mapa com as coordenadas de retorno da API
            //Obt�m a latitude e a longitude para mostrar no mapa o lugar de prefer�ncia do usu�rio
            JSONObject forecastCity = forecastJson.getJSONObject(OWM_LOCATION);
            JSONObject forecastCoord = forecastCity.getJSONObject(OWM_LOCATION_COORD);
            coordLatitude = forecastCoord.getDouble(OWM_LOCATION_COORD_LAT);
            coordLongitude = forecastCoord.getDouble(OWM_LOCATION_COORD_LON);

            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked f or, which means that we need to know the GMT offset to translatethis data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for(int i = 0; i < weatherArray.length(); i++) {
                // These are the values that will be collected.
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // Description is in a child array called "weather", which is 1 element long.
                // That element also contains a weather code.
                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);
            }

            //Refactor Licao 4C - Insere um contador para substituir o codigo de contagem
            // de registros inseridos
            int inserted = 0;

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] resultCV = new ContentValues[cVVector.size()];
                cVVector.toArray(resultCV);
                inserted = mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, resultCV);

            }
            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) { //REFACTOR 4C - mudou tipo de retorno

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String locationQuery = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        String lang = "pt"; //minha custom
        String api_key = "f01a986ef54fb617a966dcc9ba6df97d";
        int numDays = 14;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //http://api.openweathermap.org/data/2.5/forecast/daily?q=Indaiatuba&units=metric&cnt=14&lang=pt&appid=f01a986ef54fb617a966dcc9ba6df97d
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String LANG_PARAM = "lang"; //minha custom
            final String API_KEY = "appid"; //minha custom


            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(LANG_PARAM, lang)
                    .appendQueryParameter(API_KEY, api_key)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.d(LOG_TAG, "URL da Requisição: " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();

            //REFACTOR Licao 4C
            getWeatherDataFromJson(forecastJsonStr, locationQuery);

            //logando o retorno do backend da API do Tempo
            Log.d(LOG_TAG, "String JSON da Previsao: " + forecastJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        }catch (JSONException e){ //Refactor 4C - Tratamento da excess�o da chamada de getWeatherDataFromJson
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }
}