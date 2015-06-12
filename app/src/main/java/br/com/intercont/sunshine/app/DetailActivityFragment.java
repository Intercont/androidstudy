package br.com.intercont.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.intercont.sunshine.app.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 * Recebeu o Intent para Share apos o refactor realizado para adaptar meu codigo ao ensinado na
 * aula Share Intent, Lesson 3
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private View rootView;
    private String mForecast;
    private ShareActionProvider mShareActionProvider;

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    //CursorLoader Loader ID
    private static final int DETAIL_FRAGMENT_LOADER_ID = 1;

    //Projection (colunas da tabela) dos dados que vou requisitar e usar na View de Details
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    //Constantes correspondentes à projeção acima e DEVEM ser atualizadas caso a projeção mude
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;


    public DetailActivityFragment() {
        //sinaliza que o Fragment tem um Menu a ser inserido
        // para que chame o onCreateOptionMenu la no DetailActivity
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //obtendo o Intent da chamada ao se clicado na ListView
//        Intent intent = getActivity().getIntent();
        //altero o return para que atribua a um rootView e seja retornado somente ao final, assim como no ForecastFragment
        rootView =  inflater.inflate(R.layout.fragment_detail, container, false);
        //verifico se o intent nao esta vazio ou nulo e se tem algum Extra
//        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
        //Refactor Lição 4C - Trabalhando com Content Providers
//        if(intent != null){
        //recebo o texto selecionado do ListView e enviado pelo Extra
        //UPDATE - Atualizado no Refactor para alimentar uma variavel Global, assim a String e usada no Share
//            String selectedItemText = intent.getStringExtra(Intent.EXTRA_TEXT);
        //Seto o texto no TextView que coloquei ID que estava com o Hello World quando criei a Activity
//            ((TextView)rootView.findViewById(R.id.detailforecast)).setText(mForecastStr);
        //alimentando o TextView
//        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        //Infla o menu. Adiciona itens na Action Bar se esta existe
        menuInflater.inflate(R.menu.detailfragment, menu);

        //Recupero o menuitem de Share para o DetailActivity
        MenuItem shareItem = menu.findItem(R.id.action_share);

        //Obtenho o ShareActionProvider pelo MenuItemCompat e seto o MenuItem do Share nele pra que
        // seja habilitado no botao o os Providers para o Share, pelo getActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        //Anexo uma intent a este ShareActionProvider, fazendo uso do metodo createShareForecastIntent
        //que criei acima. Este e atualizado a qualquer hora, quando o usuario seleciona um novo
        // conjunto de dados para compartilhar

//        if(mShareActionProvider != null){
        //Refactor Lição 4C - Details Activity - Share Option - Valido se tenho algum valor
        // alimentado pelo CursorLoader da consulta do clique na ListView
        if(mForecast != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }else{
            //Log.d(LOG_TAG, "ShareActionProvider esta nulo");
            Log.d(LOG_TAG, "Valor de String da consulta do clique esta nulo");
        }
    }

    /**
     * Create a Intent for the Share Action in the DetailActivity
     * @return intent with params
     */
    private Intent createShareForecastIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        //em substituicao a FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET que foi Deprecated mas tem o mesmo valor
        //Flag utilizada para garantir que ao finalizar o compartilhamento e retroceder, a mesma retorne
        //ao aplicativo Sunshine que esta compartilhando, e nao ao aplicativo com o qual foi compartilhado
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(
                DETAIL_FRAGMENT_LOADER_ID,
                null,
                this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "Dentro de onCreateLoader");
        Intent intent = getActivity().getIntent();
        Log.d(LOG_TAG,"intent.getData(): " + intent.getData());
        if(intent == null){
            return null;
        }

        //Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                FORECAST_COLUMNS, //projection somente das colunas a serem usadas nesta Activity
                null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "Dentro de onLoadFinished");
        //se não há dados, return vazio
        if(!data.moveToFirst()){
            return;
        }

        //Vamos reconstruir a String com os dados do Cursor, usando as constantes
        // de projeção para requisitar os dados
        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP),isMetric);

        //Formatando a String com format e as variáveis
        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        //alimentando o TextView
        TextView detailTextView = (TextView) getView().findViewById(R.id.detailforecast);
        detailTextView.setText(mForecast);
        Log.d(LOG_TAG,"mForecast: " + mForecast);

        //If onCreateOptionsMenu has already happened, we need to update the share intent
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    public static <T> void initLoader(final int loaderId, final Bundle args, final LoaderManager.LoaderCallbacks<T> callbacks,
//                                      final LoaderManager loaderManager) {
//        final Loader<T> loader = loaderManager.getLoader(loaderId);
//        if (loader != null && loader.isReset()) {
//            loaderManager.restartLoader(loaderId, args, callbacks);
//        } else {
//            loaderManager.initLoader(loaderId, args, callbacks);
//        }
//    }
}
