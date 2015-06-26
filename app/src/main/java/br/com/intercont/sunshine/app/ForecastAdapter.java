package br.com.intercont.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.intercont.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    // Representa��es de int do tipo de ViewType.
    // Seu maior n�mero NUNCA vai ser maior ou igual � quantidade de Views
    // por conta do �ndice da lista come�ar no primeiro em 0
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    public String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        //Refactor 4C - Foram substitu�dos pelas constantes de Projection do ForecastFragment.
        // Caso os �ndices no DB mudem ou haja altera��o de Schema ou do Join, as constantes
        // devem ser atualizadas manualmente

//        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
//        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
//        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
//        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /**
     * Especifica qual tipo de View deve ser usada pela condição ternária.
     * Se a mesma é a primeira, estará na posição zero, então, corresponte à
     * previsão de Hoje, então carregamos a View list_item_forecast_today,
     * se não, carregamos a lista normal com as demais previsões dos dias.
     * Esta lógica de qual carregar é feita dentro de newView com o valor de
     * retorno daqui.
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position){
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /**
     * Sobrescreve o m�todo getViewTypeCount
     * que especifica quantas Views o Adapter retorna
     * @return Quantidade de Views
     */
    @Override
    public int getViewTypeCount(){
        return 2;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        //Define qual layout será, substitu� o tern�rio para legibilidade e consist�ncia em caso de
        // um retorno n�o esperado pelo cursor, para que a aplica��o n�o quebre
        if(viewType == VIEW_TYPE_TODAY){
            layoutId = R.layout.list_item_forecast_today;
        }else if(viewType == VIEW_TYPE_FUTURE_DAY){
            layoutId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //Criando o objeto viewHolder e recuperando a tag da View criada em newView
        //Este objeto ser� o utilizado de agora em diante para aplicar os valores
        //na view
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        //Define qual layout será, substitu� o tern�rio para legibilidade e consist�ncia em caso de
        // um retorno n�o esperado pelo cursor, para que a aplica��o n�o quebre


        //Read weather icon ID from Cursor
        int weatherID = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        //Use placeholder image for now
//        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
//        iconView.setImageResource(R.mipmap.ic_launcher);

        //Verifica se devo carregar o ícone Art (cores HD), previsão HOJE ou o ícone Icon (cinza) DEMAIS DIAS
        if(viewType == VIEW_TYPE_TODAY){
            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherID));
        }else if(viewType == VIEW_TYPE_FUTURE_DAY){
            viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherID));
        }

        //Read DATE from cursor
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
//        TextView dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
//        dateView.setText(Utility.getFriendlyDayString(context,date));
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context,date));

        //Read WEATHER FORECAST from cursor
        String weather = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
//        TextView weatherView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
//        weatherView.setText(weather);
        viewHolder.weatherView.setText(weather);

        //Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        //Read high temperature from cursor
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
//        TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
//        highView.setText(Utility.formatTemperature(high, isMetric));
        viewHolder.highView.setText(Utility.formatTemperature(context, high, isMetric));

        //Read low temperature from cursor
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
//        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
//        lowView.setText(Utility.formatTemperature(low, isMetric));
        viewHolder.lowView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    /**
     * Cache de todas as view filhas para a lista de itens da forecast
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView weatherView;
        public final TextView highView;
        public final TextView lowView;

        public ViewHolder(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            weatherView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
            highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}


