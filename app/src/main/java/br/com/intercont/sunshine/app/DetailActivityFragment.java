package br.com.intercont.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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


/**
 * A placeholder fragment containing a simple view.
 * Recebeu o Intent para Share após o refactor realizado para adaptar meu código ao ensinado na
 * aula Share Intent, Lesson 3
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;

    public DetailActivityFragment() {
        //sinaliza que o Fragment tem um Menu a ser inserido
        // para que chame o onCreateOptionMenu lá no DetailActivity
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //obtendo o Intent da chamada ao se clicado na ListView
        Intent intent = getActivity().getIntent();
        //altero o return para que atribua a um rootView e seja retornado somente ao final, assim como no ForecastFragment
        View rootView =  inflater.inflate(R.layout.fragment_detail, container, false);
        //verifico se o intent não está vazio ou nulo e se tem algum Extra
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            //recebo o texto selecionado do ListView e enviado pelo Extra
            //UPDATE - Atualizado no Refactor para alimentar uma variável Global, assim a String é usada no Share
//            String selectedItemText = intent.getStringExtra(Intent.EXTRA_TEXT);
            mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            //Seto o texto no TextView que coloquei ID que estava com o Hello World quando criei a Activity
            ((TextView)rootView.findViewById(R.id.detailforecast)).setText(mForecastStr);
        }
        return rootView;
    }

    /**
     * Create a Intent for the Share Action in the DetailActivity
     * @return intent with params
     */
    private Intent createShareForecastIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        //em substituição à FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET que foi Deprecated mas tem o mesmo valor
        //Flag utilizada para garantir que ao finalizar o compartilhamento e retroceder, a mesma retorne
        //ao aplicativo Sunshine que está compartilhando, e não ao aplicativo com o qual foi compartilhado
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return intent;
    }

    /**
     * Incluído no Refactor de Share
     * Após a criação do detailfragment.xml, e setar setHasOptionsMenu(true); no construtor
     * desta classe, crio um onCreateOptionMenu independente e separado da classe Pai DetailActivity
     * e inicializo e crio o ShareActionProvider daqui
     * @param menu
     * @param menuInflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        //Infla o menu. Adiciona itens na Action Bar se esta existe
        menuInflater.inflate(R.menu.detailfragment, menu);

        //Recupero o menuitem de Share para o DetailActivity
        MenuItem shareItem = menu.findItem(R.id.action_share);

        //Obtenho o ShareActionProvider pelo MenuItemCompat e seto o MenuItem do Share nele pra que
        // seja habilitado no botão o os Providers para o Share, pelo getActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        //Anexo uma intent à este ShareActionProvider, fazendo uso do método createShareForecastIntent
        //que criei acima. Este é atualizado a qualquer hora, quando o usuário seleciona um novo
        // conjunto de dados para compartilhar
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }else{
            Log.d(LOG_TAG, "ShareActionProvider está nulo");
        }
    }
}
