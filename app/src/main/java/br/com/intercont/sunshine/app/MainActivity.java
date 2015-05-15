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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
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
        //solução do curso para apresentar a localização do mapa, passa o CEP/ZIP Postal Code
        // diretamente para a API do Google e esta se encarrega de buscar o endereço
        if(id == R.id.action_mapuserlocationcourse) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * SOLUÇÃO DO CURSO
     * Solução do curso para carregar o mapa com a região, passa de parâmetro diretamente
     * o CEP armazenado no SharedPreferences na query da Implicit Intent para algum aplicativo de maps
     */
    private void openPreferredLocationInMap(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = preferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        //Uri para chamada do intent, como descrita em https://developer.android.com/guide/components/intents-common.html#Maps
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();

        //Chamada de uma Intent Action View, necessária para mostrar o mapa
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        //Verificamos se é possível chamar essa Intent, quer dizer, se há algum aplicativo no celular que pode abrir mapas
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }else{
            Log.d(LOG_TAG, "Não foi possive chamar " + location + ", não há nenhuma aplicação de mapas instalada");
        }

    }
}
