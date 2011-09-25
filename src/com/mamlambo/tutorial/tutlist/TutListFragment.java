/*
 * Copyright (c) 2011, Lauren Darcey and Shane Conder
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of 
 *   conditions and the following disclaimer.
 *   
 * * Redistributions in binary form must reproduce the above copyright notice, this list 
 *   of conditions and the following disclaimer in the documentation and/or other 
 *   materials provided with the distribution.
 *   
 * * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific prior 
 *   written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * <ORGANIZATION> = Mamlambo
 */
package com.mamlambo.tutorial.tutlist;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.mamlambo.tutorial.tutlist.data.TutListDatabase;
import com.mamlambo.tutorial.tutlist.data.TutListProvider;
import com.mamlambo.tutorial.tutlist.data.TutListSharedPrefs;
import com.mamlambo.tutorial.tutlist.service.TutListDownloaderService;

public class TutListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LAST_POSITION_KEY = "lastPosition";
    private static final String LAST_ITEM_CLICKED_KEY = "lastItemClicked";
    private static final String CUR_TUT_URL_KEY = "curTutUrl";

    public static final String DEBUG_TAG = "TutListFragment";

    private OnTutSelectedListener tutSelectedListener;
    private static final int TUTORIAL_LIST_LOADER = 0x01;

    private SimpleCursorAdapter adapter;

    private long lastItemClicked = -1;
    private String curTutUrl = null;
    private int selectedPosition = -1;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position == selectedPosition) {
            // not changing selection, do nothing
            return;
        }

        // get Url
        String projection[] = { TutListDatabase.COL_URL };
        Uri viewedTut = Uri.withAppendedPath(TutListProvider.CONTENT_URI,
                String.valueOf(id));
        Cursor tutorialCursor = getActivity().getContentResolver().query(
                viewedTut, projection, null, null, null);
        if (tutorialCursor.moveToFirst()) {
            curTutUrl = tutorialCursor.getString(0);
            tutSelectedListener.onTutSelected(curTutUrl);
        }
        tutorialCursor.close();

        // mark the last item as read
        if (lastItemClicked != -1) {
            TutListProvider.markItemRead(getActivity().getApplicationContext(),
                    lastItemClicked);
            Log.d(DEBUG_TAG, "Marking " + lastItemClicked
                    + " as read. Now showing " + id + ".");
        }
        lastItemClicked = id;

        // v11+ highlights
        selectedPosition = position;
        l.setItemChecked(position, true);
    }

    private static final String[] UI_BINDING_FROM = {
            TutListDatabase.COL_TITLE, TutListDatabase.COL_DATE,
            TutListDatabase.COL_READ };
    private static final int[] UI_BINDING_TO = { R.id.title, R.id.date,
            R.id.title };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TUTORIAL_LIST_LOADER, null, this);

        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.list_item,
                null, UI_BINDING_FROM, UI_BINDING_TO,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        adapter.setViewBinder(new TutorialViewBinder());
        setListAdapter(adapter);
        setHasOptionsMenu(true);
        setEmptyText(getResources().getText(R.string.empty_list_label));
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (savedInstanceState != null) {
            lastItemClicked = savedInstanceState.getLong(LAST_ITEM_CLICKED_KEY,
                    -1);
            selectedPosition = savedInstanceState.getInt(LAST_POSITION_KEY, -1);
            if (selectedPosition != -1) {
                setSelection(selectedPosition);
                getListView().smoothScrollToPosition(selectedPosition);
                getListView().setItemChecked(selectedPosition, true);
            }

            curTutUrl = savedInstanceState.getString(CUR_TUT_URL_KEY);
            if (curTutUrl != null) {
                tutSelectedListener.onTutSelected(curTutUrl);
            }
        }
    }

    private boolean showReadFlag;

    @Override
    public void onPause() {
        showReadFlag = TutListSharedPrefs.getOnlyUnreadFlag(getActivity());
        Log.d(DEBUG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume");
        if (showReadFlag != TutListSharedPrefs.getOnlyUnreadFlag(getActivity())) {
            getLoaderManager().restartLoader(TUTORIAL_LIST_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(LAST_ITEM_CLICKED_KEY, lastItemClicked);
        outState.putString(CUR_TUT_URL_KEY, curTutUrl);
        outState.putInt(LAST_POSITION_KEY, selectedPosition);
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        super.onDestroy();
    }

    public interface OnTutSelectedListener {
        public void onTutSelected(String tutUrl);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            tutSelectedListener = (OnTutSelectedListener) activity;
        } catch (ClassCastException e) {
            Log.e(DEBUG_TAG, "Bad class", e);
            throw new ClassCastException(activity.toString()
                    + " must implement OnTutSelectedListener");
        }
    }

    // options menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);

        // refresh menu item
        Intent refreshIntent = new Intent(
                getActivity().getApplicationContext(),
                TutListDownloaderService.class);
        refreshIntent.setData(Uri.parse(getString(R.string.default_url)));

        MenuItem refresh = menu.findItem(R.id.refresh_option_item);
        refresh.setIntent(refreshIntent);

        // pref menu item
        Intent prefsIntent = new Intent(getActivity().getApplicationContext(),
                TutListPreferencesActivity.class);

        MenuItem preferences = menu.findItem(R.id.settings_option_item);
        preferences.setIntent(prefsIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh_option_item:
            getActivity().startService(item.getIntent());
            break;
        case R.id.settings_option_item:
            getActivity().startActivity(item.getIntent());
            break;
        case R.id.mark_all_read_item:
            TutListProvider.markAllItemsRead(getActivity()
                    .getApplicationContext());
            break;
        }
        return true;
    }

    // custom viewbinder
    private class TutorialViewBinder implements SimpleCursorAdapter.ViewBinder {

        public boolean setViewValue(View view, Cursor cursor, int index) {
            if (index == cursor.getColumnIndex(TutListDatabase.COL_READ)) {
                boolean read = cursor.getInt(index) > 0 ? true : false;
                TextView title = (TextView) view;
                if (!read) {
                    title.setTypeface(Typeface.DEFAULT_BOLD, 0);
                } else {
                    title.setTypeface(Typeface.DEFAULT);
                }
                return true;
            } else if (index == cursor.getColumnIndex(TutListDatabase.COL_DATE)) {
                // get a locale based string for the date
                DateFormat formatter = android.text.format.DateFormat
                        .getDateFormat(getActivity().getApplicationContext());
                long date = cursor.getLong(index);
                Date dateObj = new Date(date * 1000);
                ((TextView) view).setText(formatter.format(dateObj));
                return true;
            } else {
                return false;
            }
        }
    }

    // LoaderManager.LoaderCallbacks<Cursor> methods

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { TutListDatabase.ID, TutListDatabase.COL_TITLE,
                TutListDatabase.COL_DATE, TutListDatabase.COL_READ };

        Uri content = TutListProvider.CONTENT_URI;
        String selection = null;
        if (TutListSharedPrefs.getOnlyUnreadFlag(getActivity())) {
            selection = TutListDatabase.COL_READ + "='0'";
        }
        CursorLoader cursorLoader = new CursorLoader(getActivity(), content,
                projection, selection, null, TutListDatabase.COL_DATE + " desc");
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
