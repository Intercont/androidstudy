package br.com.intercont.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by intercont on 19/04/15.
 */
public class ForecastFragment extends Fragment {

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
        ArrayAdapter<String> mForecastAdapter = new ArrayAdapter<String>(
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
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //e set o Adapter
        listView.setAdapter(mForecastAdapter);
        return rootView;
    }

    /**
     * FetchWeatherTask
     * Subclasse para execuçao da consulta na API dos dados em Background
     */
    private class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        //TAG Usada para identificaçao do nome da classe no Logger, facilita para a identificaçao de erros e estao em sincronia caso refatore a classe
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... postalcode) {
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
                        .appendQueryParameter(LANG_PARAM,lang);

                String urlQuery = builder.build().toString();
                Log.v("Query do Builder",urlQuery);

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

            return null;
        }
    }
}
