package br.com.intercont.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class DetailActivity extends ActionBarActivity implements ForecastFragment.CallbackDetails {

//    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null){
            // Create the detail fragment and add it to the activity
            // using a fragment transaction

            Bundle args = new Bundle();
            //adicionando ao Bundle os dados recuperados pelo getData que foram
            // adicionados ao Intent desde onde foi clicado
            args.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container,
                            new DetailActivityFragment())
                    .add(R.id.weather_detail_container,
                            fragment)
                    .commit();
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        /**
        //MINHA FORMA - FUNCIONA MAS NAO E BOA PRATICA E NO REFACTOR FUNCIONA MELHOR
        //LEVADO PRA DENTRO DO DetailActivityFragment
        //Definindo a Intent default do ShareActionProvider
        MenuItem shareItem = menu.findItem(R.id.action_share);
        //cast para compatibilidade da v4 para a v7
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        //passamos a Intent a ser executada na opcao de Share, no caso, o ACTION_SEND no metodo auxiliar
        mShareActionProvider.setShareIntent(getDefaultIntent());

        return super.onCreateOptionsMenu(menu);
        //MINHA FORMA
        */

        return true;
    }

    /**
     * Substituido pelo Refactor do Share
     * Get forecast text from selected day
     * @return Text showed in DetailActivity
     */
//    @Deprecated
//    private String getExtraText(){
//        Intent intent = this.getIntent();
//        TextView text = (TextView) findViewById(R.id.detailforecast);
//        String shareText = (String) text.getText() + " #SunshineApp";
//        return shareText;
//    }

    /**
     * Set Share Intent to the loaded ShareActionProvider
     * @return intent
     *
     * Substituido pelo Refactor do Share. Esta foi a forma como eu fiz funcionar antes de assistir a resposta.
     * O refactor leva todo este processo pra dentro do DetailActivityFragment
     */
//    @Deprecated
//    private Intent getDefaultIntent(){
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        String text = getExtraText();
//        intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_TEXT,text);
//        return intent;
//    }

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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {

    }
}
