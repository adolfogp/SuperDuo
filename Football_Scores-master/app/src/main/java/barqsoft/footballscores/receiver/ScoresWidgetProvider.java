/*
 * Copyright 2016 Jesús Adolfo García Pasquel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package barqsoft.footballscores.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.ScoresWidgetService;

/**
 * {@link android.content.BroadcastReceiver} that handles the widget's life
 * cycle events.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class ScoresWidgetProvider extends AppWidgetProvider {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String TAG = ScoresWidgetProvider.class.getSimpleName();

    /**
     * Identifies the {@link android.content.Intent}'s action that launches the
     * application.
     */
    public static final String LAUNCH_APP_ACTION =
            "barqsoft.footballscores.receiver.ScoresWidgetProvider.LAUNCH_APP_ACTION";

    /**
     * Identifies the index of the item in the widget that triggers the action.
     */
    public static final String EXTRA_ITEM_POSITION =
            "barqsoft.footballscores.receiver.ScoresWidgetProvider.EXTRA_ITEM_POSITION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(LAUNCH_APP_ACTION)) {
            Intent newActivityIntent = new Intent(context, MainActivity.class);
            newActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newActivityIntent);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.v(TAG, "Received widget update request. Contacting service.");
        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            // Create the intent that will request the service to create the views.
            Intent intent = new Intent(context, ScoresWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetIds[i]);
            // When intents are compared, the extras are ignored, so we need to
            // embed the extras into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            rv.setRemoteAdapter(R.id.stack_view, intent);
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            // We set pending intent template for the collection and individual
            // items set a fillInIntent.
            Intent launchAppIntent = new Intent(context, ScoresWidgetProvider.class);
            launchAppIntent.setAction(ScoresWidgetProvider.LAUNCH_APP_ACTION);
            launchAppIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent launchAppPendingIntent = PendingIntent.getBroadcast(
                    context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stack_view, launchAppPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
