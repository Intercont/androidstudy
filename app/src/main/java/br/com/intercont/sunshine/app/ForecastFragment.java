package br.com.intercont.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by intercont on 19/04/15.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;
    private ListView listView;

    public ForecastFragment() {
    }

    //este override do onCreate sera executado antes do proprio onCreate da View abaixo, antes do UI ser inflado
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Especificando que tenho Opçoes de Menu a serem inclusas no menu principal
        setHasOptionsMenu(true);
    }

    //inflando o item do forecastfragment das opçoes apos setar o setHasOptionsMenu como true no fim de onCreate
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    //este cara 'e de forma geral padrao e necessario
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask  = new FetchWeatherTask();
            weatherTask.execute("13206714");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weekForecast = new ArrayList<String>();

        weekForecast.add("Hoje, Sol 27º");
        weekForecast.add("Segunda, Sol 25º");
        weekForecast.add("Terça, Sol 29º");
        weekForecast.add("Quarta, Nublado 24º");
        weekForecast.add("Quinta, Chuva 22º");
        weekForecast.add("Sexta, Sol 25º");
        weekForecast.add("Sabado, Sol 25º");
        weekForecast.add("Domingo, Sol 25º");
        weekForecast.add("Segunda, Sol 25º");
        weekForecast.add("Terça, Sol 25º");
        weekForecast.add("Quarta, Sol 25º");
        weekForecast.add("Quinta, Sol 25º");


        //criando um array de strings
//            String[] forecastArray = {
//                "Hoje, Sol 27º",
//                "Segunda, Sol 25º",
//                "Terça, Sol 29º",
//                "Quarta, Nublado 24º",
//                "Quinta, Chuva 22º",
//                "Sexta, Sol 25º",
//            };
//            //convertendo-o para um ArrayList
//            List<String> weekForecast = new ArrayList<String>(
//                    Arrays.asList(forecastArray));

        //Adaptador para popular a ListView por uma fonte de dados, que no caso, é o ArrayList Mocado
        //ele requer uma série de parâmetros, como se pode ver pelos diferentes construtores dele
        mForecastAdapter = new ArrayAdapter<String>(
                //o contexto corrente, ou seja, o pai deste fragment, a Activity
                getActivity(),
                //referência ao layout gráfico como um todoo, por isso o nome do arquivo xml
                R.layout.list_item_forecast,
                //referência ao elemento do TextView, dentro do arquivo de layout de antes
                R.id.list_item_forecast_textview,
                //fonte de dados, neste caso o ArrayList acima
                weekForecast);

        //Trazendo o ListView pra cá, precisa trazer do rootView já que é onde está o ListView,
        //o rootView inflou o fragment_main aí encima e tem todos seus elementos
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //e set o Adapter
        listView.setAdapter(mForecastAdapter);
        return rootView;
    }

    /**
     * FetchWeatherTask
     * Subclasse para execuçao da consulta na API dos dados em Background
     */
    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        //TAG Usada para identificaçao do nome da classe no Logger, facilita para a identificaçao de erros e estao em sincronia caso refatore a classe
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... postalcode) {
            //Trazendo dados reais da API do OpenWeather, comentários reais mantidos do Github
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            //Parameters that could change in the OpenWeather Request
            String format = "json";
            String units = "metric";
            String lang = "pt";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                /*URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=Indaiatuba&mode=json&units=metric&cnt=7&lang=pt");*/
                /*http://api.openweathermap.org/data/2.5/forecast/daily?q=13086000&mode=json&units=metric&cnt=7&lang=pt*/
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?lat=35&lon=139&cnt=7&mode=json");

                //Vou colocar em constantes os blocos da URI para que não fiquem diretamente na construção, desta forma ao mudar em
                //um lugar, se atualiza em todos
                final String SCHEME = "http";
                final String AUTHORITY = "api.openweathermap.org";
                final String TYPE = "data";
                final String VERSION = "2.5";
                final String SERVICE = "forecast";
                final String FREQUENCY = "daily";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String LANG_PARAM = "lang";

                //Criando a URL com o URIBuilder
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME)
                        .authority(AUTHORITY)
                        .appendPath(TYPE)
                        .appendPath(VERSION)
                        .appendPath(SERVICE)
                        .appendPath(FREQUENCY)
                        .appendQueryParameter(QUERY_PARAM, postalcode[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(LANG_PARAM, lang);

                String urlQuery = builder.build().toString();
//                Log.v("Query do Builder", urlQuery);

                URL url = new URL(urlQuery);
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

                //logando o retorno do backend da API do Tempo
//                Log.v(LOG_TAG, "String JSON da Previsao: " + forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
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

            String[] forecast = new String[]{};

            try {
                forecast = getWeatherDataFromJson(forecastJsonStr,numDays);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error getting Data from JSON", e);
            }

            return forecast;
        }

         /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
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

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

//            for (String s : resultStrs) {
//                Log.v(LOG_TAG, "Forecast entry: " + s);
//            }

            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            //SOLUÇÃO DO CURSO - LIMPA O OBJETO E O REALIMENTA COM UMA ITERAÇÃO - solução mais limpa
            //COM ESTE MÉTODO, NÃO HÁ NECESSIDADE DE MOVER A listView PARA SER GLOBAL
            if(strings != null){
                mForecastAdapter.clear();
                for(String dayForecastStr : strings){
                    mForecastAdapter.add(dayForecastStr);
                }
            }
            //SOLUÇÃO DO CURSO

            //MINHA SOLUÇÃO - RECRIO O OBJETO E NOTIFICO DA MUDANÇA
            //alimentar o Adapter aqui dentro com o array de dados que vem do doInBackground
//            mForecastAdapter = new ArrayAdapter<String>(
//                    //o contexto corrente, ou seja, o pai deste fragment, a Activity
//                    getActivity(),
//                    //referência ao layout gráfico como um todoo, por isso o nome do arquivo xml
//                    R.layout.list_item_forecast,
//                    //referência ao elemento do TextView, dentro do arquivo de layout de antes
//                    R.id.list_item_forecast_textview,
//                    //fonte de dados, neste caso o ArrayList acima
//                    strings);
//
//            mForecastAdapter.notifyDataSetChanged();
//            listView.setAdapter(mForecastAdapter);
            //MINHA SOLUÇÃO

        }
    }
}
