
package com.bel.android.dspmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bel.android.dspmanager.activity.WM8994;
import com.bel.android.dspmanager.service.HeadsetService;

/**
 * This receiver starts our {@link HeadsetService} after system boot. Since
 * Android 2.3, we will always need a persistent process, because we are forced
 * to keep track of all open audio sessions.
 *
 * @author alankila
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, HeadsetService.class));
        if (WM8994.isSupported(context)) {
            WM8994.restore(context);
        }
    }
}
