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
package com.mamlambo.tutorial.tutlist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TutListDatabase extends SQLiteOpenHelper {
    private static final String DEBUG_TAG = "TutListDatabase";
    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "tutorial_data";

    public static final String TABLE_TUTORIALS = "tutorials";
    public static final String ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_URL = "url";

    public static final String COL_DATE = "tut_date";
    // sqlite has restrictions on add column -- no expressions or
    // current time values, this value is mid february 2011
    private static final String ALTER_ADD_COL_DATE = "ALTER TABLE "
            + TABLE_TUTORIALS + " ADD COLUMN " + COL_DATE
            + " INTEGER NOT NULL DEFAULT '1297728000' ";

    public static final String COL_READ = "read";
    private static final String ALTER_ADD_COL_READ = "ALTER TABLE "
            + TABLE_TUTORIALS + " ADD COLUMN " + COL_READ
            + " INTEGER NOT NULL DEFAULT 0";

    private static final String CREATE_TABLE_TUTORIALS = "CREATE TABLE "
            + TABLE_TUTORIALS + " (" + ID
            + " integer PRIMARY KEY AUTOINCREMENT, " + COL_TITLE
            + " text NOT NULL, " + COL_URL + " text UNIQUE NOT NULL, "
            + COL_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), "
            + COL_READ + " INTEGER NOT NULL default 0" + ");";

    private static final String DB_SCHEMA = CREATE_TABLE_TUTORIALS;

    public TutListDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_SCHEMA);
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 4) {
            // do our best to keep the date, using alter tables
            if (oldVersion == 3) {
                db.execSQL(ALTER_ADD_COL_READ);
            } else if (oldVersion == 2) {
                db.execSQL(ALTER_ADD_COL_DATE);
                db.execSQL(ALTER_ADD_COL_READ);
            }
        } else {
            Log.w(DEBUG_TAG,
                    "Upgrading database. Existing contents will be lost. ["
                            + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TUTORIALS);
            onCreate(db);
        }
    }

    /**
     * Create sample data to use
     * 
     * @param db
     *            The open database
     */
    private void seedData(SQLiteDatabase db) {
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Best of Tuts+ in February 2011', 'http://mobile.tutsplus.com/articles/news/best-of-tuts-in-february-2011/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Design & Build a 1980s iOS Phone App: Design Comp Slicing', 'http://mobile.tutsplus.com/tutorials/mobile-design-tutorials/80s-phone-app-slicing/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Create a Brick Breaker Game with the Corona SDK: Game Controls', 'http://mobile.tutsplus.com/tutorials/corona/create-a-brick-breaker-game-with-the-corona-sdk-game-controls/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Exporting Graphics for Mobile Apps: PNG or JPEG?', 'http://mobile.tutsplus.com/tutorials/mobile-design-tutorials/mobile-design_png-or-jpg/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Android Tablet Design', 'http://mobile.tutsplus.com/tutorials/android/android-tablet-design/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Build a Titanium Mobile Pizza Ordering App: Order Form Setup', 'http://mobile.tutsplus.com/tutorials/appcelerator/build-a-titanium-mobile-pizza-ordering-app-order-form-setup/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Create a Brick Breaker Game with the Corona SDK: Application Setup', 'http://mobile.tutsplus.com/tutorials/corona/corona-sdk_brick-breaker/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Android Tablet Virtual Device Configurations', 'http://mobile.tutsplus.com/tutorials/android/android-sdk_tablet_virtual-device-configuration/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Build a Titanium Mobile Pizza Ordering App: Topping Selection', 'http://mobile.tutsplus.com/tutorials/appcelerator/pizza-ordering-app-part-2/', (strftime('%s', '2011-02-01')));");
        db.execSQL("insert into tutorials (title, url, tut_date) values ('Design & Build a 1980s iOS Phone App: Interface Builder Setup', 'http://mobile.tutsplus.com/tutorials/iphone/1980s-phone-app_interface-builder-setup/', (strftime('%s', '2011-02-01')));");
    }
}
