package br.com.intercont.sunshine.app;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by intercont on 29/04/15.
 */
public class WeatherDataParser {

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        JSONObject jsonObject = new JSONObject(weatherJsonStr);
        JSONArray forecast = jsonObject.getJSONArray("list");
        JSONObject day = forecast.getJSONObject(dayIndex);
        JSONObject temps = day.getJSONObject("temp");
        double maxTemp = temps.getDouble("max");
        return maxTemp;
    }

}