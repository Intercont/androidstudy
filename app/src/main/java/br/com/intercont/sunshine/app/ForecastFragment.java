package br.com.intercont.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import br.com.intercont.sunshine.app.data.WeatherContract;

/**
 * Created by intercont on 19/04/15.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private final String LIST_POS = "LIST_POS";

    private ForecastAdapter mForecastAdapter;
    private ListView listView;
    private Bundle savedInstanceState;

    private MainActivity mCallback;
    private int positionOnList;

    private static final String PREF_LOCATION = "location";
    private static final String PREF_LOCATION_DEFAULT = "13206714";

    //CursorLoader Loader ID
    private static final int FORECAST_FRAGMENT_LOADER_ID = 0;

    //Projection - 4C
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    //FIM - Projection - 4C

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface CallbackDetails {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }

    //este override do onCreate sera executado antes do proprio onCreate da View abaixo, antes do UI ser inflado
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        positionOnList = 0;
        //Especificando que tenho Opçoes de Menu a serem inclusas no menu principal
        setHasOptionsMenu(true);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(
                FORECAST_FRAGMENT_LOADER_ID,
                null,
                this);
    }

    //inflando o item do forecastfragment das opçoes apos setar o setHasOptionsMenu como true no fim de onCreate
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

//    //Loader Callbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Lição 4C Refactor - requisitando dados do DB para o Cursor
        //getPreferredLocation - Busca a location, de acordo com o SharedPreferences setando em Settings
        String locationSetting = Utility.getPreferredLocation(getActivity());

        //Sort order: ASCending, by date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis() //pega a hora atual do dispositivo em milisegundos
        );

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,null,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        listView.smoothScrollToPosition(positionOnList);
        //TODO MARCAR DE AZUL ITEM SELECCIONADO listView.setStateListAnimator(R.drawable);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
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
//        FetchWeatherTask weatherTask  = new FetchWeatherTask(getActivity(),mForecastAdapter);
//        //1º - Obtenho o arquivo Preferences default
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        //2º - Recupero o valor de location, passando o valor KEY e o valor DEFAULT, trazendo do strings.xml
//        String location = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        //3º - Passo o valor da String para o parâmetro de FetchWeatherTask

        //REFACTOR LIÇÃO 4C
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
        mForecastAdapter.notifyDataSetChanged();
    }

    /**
     * onStart - Always executed when the Activity is started
     * UPDATE - Removido no final da Lição 4C
     */
//    @Override
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
//        String geoUri = "geo:"+coordLatitude+","+coordLongitude;
        String geoUri = "geo:"+ br.com.intercont.sunshine.app.FetchWeatherTask.coordLatitude+","+
                br.com.intercont.sunshine.app.FetchWeatherTask.coordLongitude + "?z=19";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(geoUri));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }else{
            Log.d(LOG_TAG, "Nao foi possivel chamar " + geoUri + ", não há nenhuma aplicação de mapas instalada");
        }
    }

    public void onLocationChange(){
        updateData();
        getLoaderManager().restartLoader(FORECAST_FRAGMENT_LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(LIST_POS, positionOnList);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Lição 4C - O mForecastAdapter (ou seja, o CursorAdapter) irá pegar os dados do nosso cursor pelo Loader
        // e popular a ListView
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        if(savedInstanceState != null){
            positionOnList = savedInstanceState.getInt(LIST_POS);
        }

        //Trazendo o ListView pra cá, precisa trazer do rootView já que é onde está o ListView,
        //o rootView inflou o fragment_main aí encima e tem todos seus elementos
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //e set o Adapter
        listView.setAdapter(mForecastAdapter);

        //Lição 4C - Novo clique para abrir os detalhes da previsão
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //CursorAdapter retorna um cursor da posição correta clicada na Lista para getItem() ou null se não achar essa posição
                positionOnList = position;
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                //se encontrou valor da chamada acima
                if(cursor != null){
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    //Com 2 painéis diferentes (Tablet e Smartphone), ao ter algum item clicado,
                    // vou chamar o Callback do onItemSelected do pai no lugar de criar aqui a Activity
                    ((CallbackDetails) getActivity())//Cast da Interface e getActivity para ter acesso à Activity acima deste Fragment (se MainActivity para 2 painéis ou DetailActivityFragment
                            .onItemSelected(WeatherContract.WeatherEntry //no método da interface, construo uma nova Uri
                                    .buildWeatherLocationWithDate
                                            (locationSetting, //com este locationSetting que o recebi acima
                                                    cursor.getLong(COL_WEATHER_DATE))); //e busco no Cursor a data, fazendo uso dos índices de Projection criados acima
                }
            }
        });

        return rootView;
    }
}
