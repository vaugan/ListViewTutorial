package com.mamlambo.tutorial.tutwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.mamlambo.tutorial.tutlist.R;
import com.mamlambo.tutorial.tutlist.TutListActivity;
import com.mamlambo.tutorial.tutlist.data.TutListDatabase;
import com.mamlambo.tutorial.tutlist.data.TutListProvider;

public class TutWidgetProvider extends AppWidgetProvider {

	public static final String DEBUG_TAG = "TutWidgetProvider";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		// Retrieve latest tutorial from the database
		try {
			updateWidgetContent(context, appWidgetManager);
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Failed", e);
		}
	}

	public static void updateWidgetContent(Context context,
			AppWidgetManager appWidgetManager) {
		String strLatestTitle = "None Available";
		
		String [] projection = {TutListDatabase.COL_TITLE};
		Uri content = TutListProvider.CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(content, projection, null, null, TutListDatabase.COL_DATE + " desc LIMIT 1");
		if (cursor.moveToFirst()) {
			strLatestTitle = cursor.getString(0);
		}
		cursor.close();
		
		// Update the app widget controls
		RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.tutlist_appwidget_layout);
		remoteView.setTextViewText(R.id.title, strLatestTitle);

		// add click handling
		Intent launchAppIntent = new Intent(context, TutListActivity.class);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(context, 0, launchAppIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
		remoteView.setOnClickPendingIntent(R.id.full_widget, launchAppPendingIntent);

		// get the Android component name 
		ComponentName tutListWidget = new ComponentName(context, TutWidgetProvider.class);
		appWidgetManager.updateAppWidget(tutListWidget, remoteView);
	}
	
}
