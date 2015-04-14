package br.com.intercont.sunshine.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
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
    }
}
