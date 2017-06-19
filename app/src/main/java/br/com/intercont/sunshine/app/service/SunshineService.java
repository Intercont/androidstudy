package br.com.intercont.sunshine.app.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by intercont on 19/11/15.
 */
public class SunshineService extends IntentService {

    public static final String LOCATION_QUERY_EXTRA = "lqe";

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    static public class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent){
            Intent sunshineServiceIntent = new Intent(context, SunshineService.class);
            sunshineServiceIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                    intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA));
            context.startService(sunshineServiceIntent);
        }
    }
}
