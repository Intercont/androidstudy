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

    //formato utilizado para armazenar datas no banco de dados.
    // Tamb�m utilizado para converter estas strings de volta � objetos
    // de data para compara��o/processo
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * M�todo Helper para converter a representa��o de data do banco de dados em algo
     * a ser mostrado para os usu�rios.
     * @param context
     * @param dateInMillis
     * @return uma representa��o "user-friendly" da data
     */
    public static String getFriendlyDayString(Context context, long dateInMillis){
        //Para a string de previs�o do tempo, utilizo a seguinte l�gica:
        //Para hoje em ingl�s: "Today, June 16" e em portugu�s (TODO): Hoje, 16 de Junho (agora mesmo vai vir o m�s em ingl�s)
        //Para amanh� em ingl�s: "Tomorrow" e em portugu�s (TODO): Amanh�
        //Para os pr�ximos 5 dias: "Wednesday" (somente o nome do dia). TODO para portugu�s ao internacionalizar
        //Para os demais dias ap�s: "Tue Jun 16" e TODO em portugu�s BR: "Ter Jun 16"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis(); //tempo corrente e milisegundos
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        //Se a data que estamos constru�ndo � a data de hoje,
        // o formato � "Hoje, Junho 16" ou em ingl�s, "Today, June 16"
        if(julianDay == currentJulianDay){
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(
                    context.getString(
                            formatId,
                            today,
                            getFormattedMonthDay(context, dateInMillis)));
        }else if(julianDay < currentJulianDay + 7){
            //Se a data de entrada � menor que uma semana no futuro,
            // apenas retorne o nome do dia fornecido pelo m�todo auxiliar
            return getDayName(context,dateInMillis);
        }else{
            //do contr�rio, uso o formato "Ter Jun 16" ou in english, "Tue Jun 16"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Dado um dia, retorno somente o nome deste dia para usar neste dia
     * Exemplos: Hoje, Amanh�, Quarta, Quinta
     * Exemplos em Ingl�s (original): Today, Tomorrow, Wednesday
     * @param context
     * @param dateInMillis
     * @return nome do dia selecionado em String
     */
    public static String getDayName(Context context, long dateInMillis){
        //Se o dia � Hoje, retorne a vers�o localizada de "Hoje" no lugar do nome do dia

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        //Retorno somente a string do NOME DO DIA
        if(julianDay == currentJulianDay){
            return context.getString(R.string.today);
        }else if (julianDay == currentJulianDay + 1){
            return  context.getString(R.string.tomorrow);
        }else{
            Time time = new Time();
            time.setToNow();
            //do contr�rio, se n�o � Hoje nem Amanh�, o formato � apenas o dia da semana (ex: Quarta/Wednesday)
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converte a data do formato do Banco de Dados para o formato "M�s dia", por ex. "June 17"
     * @param context Contexto para uso do recurso de localiza��o
     * @param dateInMillis A String formatada para o Banco de Dados, � esperada que esteja no
     *                     formato especificado em Utility.DATE_FORMAT
     * @return O dia em String no formato "June 17"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis){
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
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
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

    public static String getFormattedWind(Context context, float windSpeed, float degrees){
        int windFormat;
        if(Utility.isMetric(context)){
            windFormat = R.string.format_wind_kmh;
        }else{
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        //A partir da dire��o do vento em graus, determine a dire��o do compasso como uma
        // string (ex.: NW, N, NE, E, SE, SW, W).
        //TODO Internacionalizar este if/else para pt-BR, alimentando os valores desde strings.xml
        String direction = "WTF";
        if(degrees >= 337.5 || degrees < 22.5){
            direction = "N";
        }else if (degrees >= 22.5 || degrees < 67.5){
            direction = "NE";
        }else if (degrees >= 67.5 || degrees < 112.5){
            direction = "E";
        }else if (degrees >= 112.5 || degrees < 157.5){
            direction = "SE";
        }else if (degrees >= 157.5 || degrees < 202.5){
            direction = "S";
        }else if (degrees >= 202.5 || degrees < 247.5){
            direction = "SW";
        }else if (degrees >= 247.5 || degrees < 292.5){
            direction = "W";
        }else if (degrees >= 292.5 || degrees < 22.5){
            direction = "NW";
        }

        //Construo a String de retorno
        return String.format(context.getString(windFormat),windSpeed, direction);
    }
}