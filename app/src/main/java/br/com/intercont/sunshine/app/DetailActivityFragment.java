package br.com.intercont.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
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
            String selectedItemText = intent.getStringExtra(Intent.EXTRA_TEXT);
            //Seto o texto no TextView que coloquei ID que estava com o Hello World quando criei a Activity
            ((TextView)rootView.findViewById(R.id.detailforecast)).setText(selectedItemText);
        }
        return rootView;
    }
}
