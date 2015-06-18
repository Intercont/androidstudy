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
import android.widget.ImageView;
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
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };
    //Constantes correspondentes � proje��o acima e DEVEM ser atualizadas caso a proje��o mude
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_DEGREES = 8;


    public DetailActivityFragment() {
        //sinaliza que o Fragment tem um Menu a ser inserido
        // para que chame o onCreateOptionMenu la no DetailActivity
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_detail, container, false);
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

        //Refactor Li��o 4C - Details Activity - Share Option - Valido se tenho algum valor
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v(LOG_TAG, "Dentro de onLoadFinished");
        //se n�o h� dados, return vazio
        if(!cursor.moveToFirst()){
            return;
        }

        //Vamos reconstruir a String com os dados do Cursor, usando as constantes
        // de proje��o para requisitar os dados
//        String dateString = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE));
        String weatherDescription = cursor.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP),isMetric);
        String humidity = String.format(getString(R.string.format_humidity), cursor.getDouble(COL_WEATHER_HUMIDITY));
        String wind = Utility.getFormattedWind(getActivity(), cursor.getFloat(COL_WEATHER_WIND_SPEED), cursor.getFloat(COL_WEATHER_DEGREES));
        String pressure = String.format(getString(R.string.format_pressure), cursor.getDouble(COL_WEATHER_PRESSURE));


        //Formatando a String com format e as vari�veis
//        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        //TODO alimentando os TextViews
        TextView detailDayTextView = (TextView) getView().findViewById(R.id.detail_day_textview);
        detailDayTextView.setText(Utility.getDayName(getActivity(), cursor.getLong(COL_WEATHER_DATE)));

        TextView detailDateTextView = (TextView) getView().findViewById(R.id.detail_date_textview);
        detailDateTextView.setText(Utility.getFormattedMonthDay(getActivity(), cursor.getLong(COL_WEATHER_DATE)));

        TextView detailHighTextView = (TextView) getView().findViewById(R.id.detail_high_textview);
        detailHighTextView.setText(high);

        TextView detailLowTextView = (TextView) getView().findViewById(R.id.detail_low_textview);
        detailLowTextView.setText(low);

        ImageView imageView = (ImageView) getView().findViewById(R.id.detail_icon);
        imageView.setImageResource(R.mipmap.ic_launcher);

        TextView detailForecastTextView = (TextView) getView().findViewById(R.id.detail_forecast_textview);
        detailForecastTextView.setText(weatherDescription);

        TextView detailHumidityTextView = (TextView) getView().findViewById(R.id.detail_humidity_textview);
        detailHumidityTextView.setText(humidity);

        TextView detailWindTextView = (TextView) getView().findViewById(R.id.detail_wind_textview);
        detailWindTextView.setText(wind);

        TextView detailPressureTextView = (TextView) getView().findViewById(R.id.detail_pressure_textview);
        detailPressureTextView.setText(pressure);








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
