package br.com.intercont.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> mForecastAdapter;
    private ListView listView;
    private double coordLatitude;
    private double coordLongitude;

    private static final String PREF_LOCATION = "location";
    private static final String PREF_LOCATION_DEFAULT = "13206714";

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
            updateData();
            return true;
        }

        if(id == R.id.action_mapuserlocationigor) {
            showMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * updateData - Método para atualizar os dados da MainActivity
     * Funcionamento:
     * - Uso o PreferenceManager, pegando o getDefaultSharedPreferences, no contexto corrente,
     * trago a String do arquivo strings.xml da chave que quero e o valor por Default se esta não
     * existir e passo na chamada
     *
     */
    private void updateData(){
        FetchWeatherTask weatherTask  = new FetchWeatherTask();
        //1º - Obtenho o arquivo Preferences default
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //2º - Recupero o valor de location, passando o valor KEY e o valor DEFAULT, trazendo do strings.xml
        String location = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        //3º - Passo o valor da String para o parâmetro de FetchWeatherTask
        weatherTask.execute(location);
    }

    /**
     * onStart - Always executed when the Activity is started
     */
    @Override
    public void onStart(){
        super.onStart();
        updateData();
    }

    /**
     * showMap (MINHA SOLUÇÃO) - Este método foi a minha solução para apresentar a localização do usuário no mapa
     * O mesmo faz uso das coordenadas que a API retorna e carrega o mapa com uso delas
     */
    public void showMap() {
        //geo:latitude,longitude
        String geoUri = "geo:"+coordLatitude+","+coordLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(geoUri));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }else{
            Log.d(LOG_TAG, "Nao foi possivel chamar " + geoUri + ", não há nenhuma aplicação de mapas instalada");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Adaptador para popular a ListView por uma fonte de dados, que no caso, é o ArrayList Mocado
        //ele requer uma série de parâmetros, como se pode ver pelos diferentes construtores dele
        mForecastAdapter = new ArrayAdapter<String>(
                //o contexto corrente, ou seja, o pai deste fragment, a Activity
                getActivity(),
                //referência ao layout gráfico como um todoo, por isso o nome do arquivo xml
                R.layout.list_item_forecast,
                //referência ao elemento do TextView, dentro do arquivo de layout de antes
                R.id.list_item_forecast_textview,
                //fonte de dados, neste caso o ArrayList acima - UPDATE: como estou trazendo agora
                // direto da API, passo um ArrayList vazio no lugar do ArrayList mocado
                new ArrayList<String>());

        //Trazendo o ListView pra cá, precisa trazer do rootView já que é onde está o ListView,
        //o rootView inflou o fragment_main aí encima e tem todos seus elementos
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //e set o Adapter
        listView.setAdapter(mForecastAdapter);
        //setando um listener para o clique em um elemento do ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(position));
                startActivity(intent);

                //TOAST de Teste
                //SOLUÇÃO DO CURSO
//                String forecast = mForecastAdapter.getItem(position);
//                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                //SOLUÇÃO DO CURSO
                //MINHA SOLUÇÃO
//                Toast toast = Toast.makeText(getActivity(), adapterView.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT);
//                toast.show();
                //MINHA SOLUÇÃO
                //FIM - TOAST de Teste
            }
        });
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
        protected String[] doInBackground(String... params) {
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
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(LANG_PARAM, lang);

                String urlQuery = builder.build().toString();
                Log.v("Query do Builder", urlQuery);

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
                Log.v(LOG_TAG, "String JSON da Previsao: " + forecastJsonStr);

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
         * Como o objetivo do aplicativo é apenas trazer os dados em Celsius e
         converter os valores de Celsius para Fahrenheit no aplicativo,
         deixarei o parâmetro da chamada como metric, intacta, e realizo as
         conversões aqui, como mostrado no curso. O objetivo de converter os
         valores aqui e somente trazer valores métricos é porque vamos
         armazenar estes valores em um Banco de Dados e não queremos dados com
         unidades misturadas, então, para mostrar ao usuário de acordo com a pref
         dele, convertemos aqui trazendo sua preferência de SharedPreferences
         */
        private String formatHighLows(double high, double low) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(getString(R.string.pref_unit_key),getString(R.string.pref_unit_default));

            //verificamos se a unidade de medida de preferência é imperial, se for, convertemos, amém
            if(unitType.equals(getString(R.string.pref_unit_imperial))){
                //cálculo para conversão dos valores para Fahrenheit
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            }else if (!unitType.equals(getString(R.string.pref_unit_metric))){
                //caso não seja nem métrica nem imperial, loga que bicho é esse
                Log.d(LOG_TAG,"Unit type not found: " + unitType);
            }

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
            final String OWM_LOCATION = "city";
            final String OWM_LOCATION_COORD = "coord";
            final String OWM_LOCATION_COORD_LAT = "lat";
            final String OWM_LOCATION_COORD_LON = "lon";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            //MINHA SOLUÇÃO
            //Obtém a latitude e a longitude para mostrar no mapa o lugar de preferência do usuário
            JSONObject forecastCity = forecastJson.getJSONObject(OWM_LOCATION);
            JSONObject forecastCoord = forecastCity.getJSONObject(OWM_LOCATION_COORD);
            coordLatitude = forecastCoord.getDouble(OWM_LOCATION_COORD_LAT);
            coordLongitude = forecastCoord.getDouble(OWM_LOCATION_COORD_LON);
            //MINHA SOLUÇÃO

            //Forecast Array
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
                mForecastAdapter.notifyDataSetChanged();
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
