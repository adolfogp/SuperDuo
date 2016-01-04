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

package barqsoft.footballscores.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresAdapter;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.receiver.ScoresWidgetProvider;

/**
 * Updates the widget.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class ScoresWidgetService extends RemoteViewsService {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static String TAG = ScoresWidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.v(TAG, "Widget requested View factory from service.");
        return new ScoresRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    /**
     * Creates the views for the items in the collection view widget.
     */
    private static class ScoresRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        /**
         * ID of the widget associated with this instance.
         */
        private int mAppWidgetId;

        /**
         * {@link Cursor} containing the entries to be shown in the collection
         * view widget.
         */
        private Cursor mCursor;

        /**
         * {@link Context} used to query the {@code ContentProvider}.
         */
        private Context mContext;

        /**
         * Creates a new instance that uses the specified context to query the
         * {@code ContentProvider} and the {@link Intent} used to request the
         * {@link android.widget.RemoteViewsService.RemoteViewsFactory} from
         * the {@link RemoteViewsService}.
         *
         * @param context used to query the {@code ContentProvider}.
         * @param intent the {@link Intent} used to request the
         *     {@link android.widget.RemoteViewsService.RemoteViewsFactory} from
         *     the {@link RemoteViewsService}.
         */
        public ScoresRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {
            mCursor = mContext.getContentResolver().query(
                    DatabaseContract.scores_table.buildScoreWithDate(),
                    null,
                    null,
                    new String[] {new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, // today
                    null);
        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
                    R.layout.widget_item);
            mCursor.moveToPosition(position);
            String homeTeamName = mCursor.getString(ScoresAdapter.COL_HOME);
            String awayTeamName = mCursor.getString(ScoresAdapter.COL_AWAY);
            remoteViews.setTextViewText(R.id.home_name, homeTeamName);
            remoteViews.setTextViewText(R.id.away_name, awayTeamName);
            remoteViews.setImageViewResource(R.id.home_crest
                    , Utilies.getTeamCrestByTeamName(homeTeamName));
            remoteViews.setImageViewResource(R.id.away_crest
                    , Utilies.getTeamCrestByTeamName(awayTeamName));
            remoteViews.setTextViewText(R.id.data_textview
                    , mCursor.getString(ScoresAdapter.COL_MATCHTIME));
            remoteViews.setTextViewText(R.id.score_textview
                    , Utilies.getScores(mCursor.getInt(ScoresAdapter.COL_HOME_GOALS)
                    , mCursor.getInt(ScoresAdapter.COL_AWAY_GOALS)));

            // Next, we set a fill-intent which will be used to fill-in the pending
            // intent template which is set on the collection view in ScoresWidgetProvider.
            Bundle extras = new Bundle();
            extras.putInt(ScoresWidgetProvider.EXTRA_ITEM_POSITION, position);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            remoteViews.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

            return remoteViews;
        }

        public RemoteViews getLoadingView() {
            return null;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public long getItemId(int position) {
            return position;
        }

        public boolean hasStableIds() {
            return true;
        }

        public void onDataSetChanged() {
        }
    }
}
