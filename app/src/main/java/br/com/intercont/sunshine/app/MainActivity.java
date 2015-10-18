package br.com.intercont.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements ForecastFragment.CallbackDetails{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Alimentando a mLocation
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);
        setContentView(R.layout.activity_main);

        //Verifico se há a presença de weather_detail_container no layout do activity_main
        //Caso tenha, estou em um tablet, com o layout de sw600dp carregado,
        // caso não, estou em um smartphone com o layout default carregado
        if (findViewById(R.id.weather_detail_container) != null){
            //The detail container view will be present only in the large-screen layouts
            //(res/layout-sw600dp. If this view is present, the the activityshould be
            //in two-pane mode.
            mTwoPane = true;

            //In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a fragment transaction
            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container,
                                new DetailActivityFragment(),
                                DETAILFRAGMENT_TAG)
                        .commit();
            }
        }else{
            mTwoPane = false;
            //seta a elevação da ActionBar para zero, eliminando assim a sombra default que a mesma faz
            if(getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0);
            }
        }

        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);

        //define o tipo do item para o topo do ListView de Forecasts.
        // Se for tablet (mTwoPane = true), false, assim o primeiro da lista aparece normal
        // Se for smartphone (mTwoPane = false), true, assim o primeiro da lista aparece grandao
        forecastFragment.setmForecastAdapterUseTodayLayout(!mTwoPane);

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
        //Verifica��o se o location est� diferente do atual, se sim, requisito novos dados
        String location = Utility.getPreferredLocation(this);
        if(location != null && !location.equals(mLocation)){
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if(null != ff) {
                ff.onLocationChange();
            }
            //onResume para o DetailActivityFragment que agora é carregado dinâmicamente, buscamos ele pela TAG
            DetailActivityFragment detailActivityFragment = (DetailActivityFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if (null != detailActivityFragment) {
                detailActivityFragment.onLocationChanged(location);
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

    @Override
    public void onItemSelected(Uri dateUri) {
        //se estamos em um layout com 2 painéis (tablet)
        if(mTwoPane){
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction
            //Argumentos da URI que mudou ao se clicar em um item na lista
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, dateUri);
            //criação dinâmica do fragment para ser carregado em um Master/Detail Navigation Flow
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);
            //substituição da activity atual pela dinâmicamente gerada com os novos args selecionados
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();

        }else{//senão smarthphone, carregamento convencional de outra activity
            Intent intent = new Intent(this,DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }


    }

}
