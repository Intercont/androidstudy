/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.intercont.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

/*    Formato utilizado para armazenar datas no banco de dados.
     Tambem utilizado para converter estas strings de volta a objetos
     de data para comparacao/processo*/
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Metodo Helper para converter a representacao de data do banco de dados em algo
     * a ser mostrado para os usuarios.
     *
     * @param context
     * @param dateInMillis
     * @return uma representacao "user-friendly" da data
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        /*Para a string de previsao do tempo, utilizo a seguinte logica:
        Para hoje em ingles: "Today, June 16" e em portugues: "Hoje, Junho 16"
        Para amanha em ingles: "Tomorrow" e em portugues Amanha
        Para os proximos 5 dias: "Wednesday" (somente o nome do dia) e em português "Quarta"
        Para os demais dias apos 5 dias: "Tue Jun 16" e em portugues BR: "Ter Jun 16"*/

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        /*Se a data que estamos construindo e a data de hoje,
         o formato e "Hoje, Junho 16" ou em ingles, "Today, June 16"*/
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;

            return String.format(
                    context.getString(
                            formatId,
                            today,
                            getFormattedMonthDay(context, dateInMillis)));
        } else if (julianDay < currentJulianDay + 7) {
            /*Se a data de entrada e menor que uma semana no futuro,
             apenas retorne o nome do dia fornecido pelo metodo auxiliar*/
            return getDayName(context, dateInMillis);
        } else {
            //do contr�rio, uso o formato "Ter Jun 16" ou in english, "Tue Jun 16"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Dado um dia, retorno somente o nome deste dia para usar neste dia
     * Exemplos: Hoje, Amanha, Quarta, Quinta
     * Exemplos em Ingles (original): Today, Tomorrow, Wednesday
     *
     * @param context
     * @param dateInMillis
     * @return nome do dia selecionado em String
     */
    public static String getDayName(Context context, long dateInMillis) {
        //Se o dia e Hoje, retorne a versao localizada de "Hoje" no lugar do nome do dia
        Time t = new Time();
        t.setToNow();

        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            //do contrario, se nao for Hoje nem Amanha, o formato e apenas o dia da semana (ex: Quarta/Wednesday)
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converte a data do formato armazenado no Banco de Dados para o formato "Mes dia", por ex. "June 17"
     *
     * @param context      Contexto para uso do recurso de localizacao
     * @param dateInMillis A String formatada para o Banco de Dados, e esperada que esteja no
     *                     formato especificado em Utility.DATE_FORMAT
     * @return O dia em String no formato "June 17"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_metric))
                .equals(context.getString(R.string.pref_unit_metric));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
//        return String.format("%.0f", temp);
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        //A partir da direcao do vento em graus, determine a direcao do compasso como uma
        // string (ex.: NW, N, NE, E, SE, SW, W).
        String direction = "WTF";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }

        DetailActivity.windDirection = direction;

        //Construo a String de retorno
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    /**
     * Método auxiliar para informar o id de recurso do ícone de acordo com o id de condição do tempo
     * retornado pela chamada ao OpenWeatherMap.
     *
     * @param weatherId de resposta da API do OpeWeatherMap
     * @return o id da imagem correspondente que vem de R(esources). -1 se não for nada encontrado
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        //Baseado no código de dados encontrado em
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) { //TODO
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId >= 761 && weatherId <= 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Método auxiliar para informar o id de recurso da Arte (Vista do Dia ou Detalhes) de acordo
     * com o id de condição do tempo retornado pela chamada ao OpenWeatherMap.
     *
     * @param weatherId de resposta da API do OpeWeatherMap
     * @return o id da imagem correspondente que vem de R(esources). -1 se não for nada encontrado
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        //Baseado no código de dados encontrado em
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) { //TODO
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId >= 761 && weatherId <= 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }
}