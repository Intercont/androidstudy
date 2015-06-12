package br.com.intercont.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FORECASTFRAGMENT_TAG = "FFTAG";

    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Alimentando a mLocation
        mLocation = Utility.getPreferredLocation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment(),FORECASTFRAGMENT_TAG)
                    .commit();
        }
        //Log para validacao das fases do ciclo de vida de uma Activity apenas
        Log.d(LOG_TAG, "onCreate");


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        //solucao do curso para apresentar a localizacao do mapa, passa o CEP/ZIP Postal Code
        // diretamente para a API do Google e esta se encarrega de buscar o endereco
        if(id == R.id.action_mapuserlocationcourse) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * SOLUCAO DO CURSO
     * Solucao do curso para carregar o mapa com a regiao, passa de parametro diretamente
     * o CEP armazenado no SharedPreferences na query da Implicit Intent para algum aplicativo de maps
     */
    private void openPreferredLocationInMap(){
        //REFACTOR 4C
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String location = preferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        //REFACTOR 4C
        String location = Utility.getPreferredLocation(this);
                //Uri para chamada do intent, como descrita em https://developer.android.com/guide/components/intents-common.html#Maps
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();

        //Chamada de uma Intent Action View, necessaria para mostrar o mapa
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        //Verificamos se e possivel chamar essa Intent, quer dizer, se ha algum aplicativo no celular que pode abrir mapas
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }else{
            Log.d(LOG_TAG, "Nao foi possivel chamar " + location + ", nao ha nenhuma aplicacao de mapas instalada");
        }

    }

    /**
     * Log para validacao das fases do ciclo de vida de uma Activity apenas
     */
    @Override
    protected void onStart(){
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        //Verificação se o location está diferente do atual, se sim, requisito novos dados
        String location = Utility.getPreferredLocation(this);
        if(location != null && !location.equals(mLocation)){
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentByTag(FORECASTFRAGMENT_TAG);
            if(null != ff) {
                ff.onLocationChange();
            }
            mLocation = location;
        }

        Log.d(LOG_TAG, "onResume");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

}
