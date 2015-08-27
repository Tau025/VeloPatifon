package tt.velopatifon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Tau on 26.08.2015.
 * BroadcastReceiver будет перезапускаться при перезагрузку девайса
 */
public class BootBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, PlayService.class));
    }
}
