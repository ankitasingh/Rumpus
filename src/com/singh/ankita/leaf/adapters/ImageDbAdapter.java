package com.singh.ankita.leaf.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.singh.ankita.leaf.util.ImageEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class ImageDbAdapter {

	//Column names for storing image details in the database
	public static final String KEY_LOCATION = "location";//for storing location where user checked in
	public static final String KEY_DATE = "image_date";//for storing date on which image was taken
	public static final String KEY_TIMESTAMP = "timestamp";//for storing timestamp of image
	public static final String KEY_IMAGE_URI = "imguri";//for storing uri at which image is saved in the external memory of phone
	public static final String KEY_CATEGORY="category";//For storing interest category of image
	public static final String KEY_SUBCATEGORY="subcategory";//For storing interest subcategory of image
	public static final String KEY_COMMENT = "comment";//for storing image caption
	public static final String KEY_ROWID = "_id";//unique id for each image entry
	public static final String KEY_LOCATION_URL = "locationurl";//Website Url of the location checked in by the user

	private static final String DATABASE_NAME = "rumpusDb";
	private static final String DATABASE_TABLE = "rumpusTable";
	private static final int DATABASE_VERSION = 2;

	private static final String TAG = "ImageDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

//SQL Statement that creates Database table if it does not exist
	private static final String DATABASE_CREATE =
			"create table if not exists "+ DATABASE_TABLE+ "("+KEY_ROWID+ " integer primary key autoincrement, "+
					KEY_IMAGE_URI + " text unique, "+KEY_DATE+" text, "+KEY_LOCATION+" text, "+KEY_TIMESTAMP+" text, "+
					KEY_CATEGORY+" text, "+KEY_SUBCATEGORY+" text, "+KEY_COMMENT+" text, "+ KEY_LOCATION_URL+" text "+");";




	private Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		 //Create SQLite Database
		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		//Drops previous Database Table and creates new one on application upgrade
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
			onCreate(db);
		}
	}


	public ImageDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public ImageDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

    //Saves and Image Entry to the SQLite Database
	public long saveImageEntry(HashMap<String,String> hm) {
		
		Log.i(TAG,"saving image in database");
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DATE, hm.get("date"));
		initialValues.put(KEY_TIMESTAMP, hm.get("timestamp"));
		initialValues.put(KEY_IMAGE_URI, hm.get("imageuri"));
		initialValues.put(KEY_COMMENT, hm.get("comment"));
		if(hm.containsKey("location"))initialValues.put(KEY_LOCATION, hm.get("location"));
		if(hm.containsKey("category"))initialValues.put(KEY_CATEGORY, hm.get("category"));
		if(hm.containsKey("subcategory"))initialValues.put(KEY_SUBCATEGORY, hm.get("subcategory"));
		if(hm.containsKey("locationurl")) initialValues.put(KEY_LOCATION_URL, hm.get("locationurl"));
		long temp= mDb.insert(DATABASE_TABLE, null, initialValues);
		Log.i(TAG,"New Row inserted"+hm.get("imageuri"));
		return temp;
	}

	//deletes an entry from the database with the corresponding rowId
	public boolean deleteImageEntry(int key_rowid) 
	{
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + key_rowid, null) > 0;
	}

	//deletes an entry from the database with the corresponding Image Uri
	public boolean deleteImageEntry(String key_image_uri)
	{
		return mDb.delete(DATABASE_TABLE, KEY_IMAGE_URI + "= '" + key_image_uri+"'", null) > 0;
	}

    //Fetching details for all the image records in the database
	public ArrayList<ImageEntry> fetchAllImageEntries() {

		ArrayList<ImageEntry> imageEntryList=new ArrayList<ImageEntry>();

		Cursor cursor=mDb.query(DATABASE_TABLE, null, null, null, null, null, null);
		int size=cursor.getCount();
		Log.i(TAG,Integer.toString(size));
		if(cursor!=null)
		{
			cursor.moveToFirst();
			int KEY_IMAGE_URI_index =cursor.getColumnIndex(KEY_IMAGE_URI);
			int KEY_LOCATION_index =cursor.getColumnIndex(KEY_LOCATION);
			int KEY_LOCATION_URL_index =cursor.getColumnIndex(KEY_LOCATION_URL);
			int KEY_SUBCATEGORY_index =cursor.getColumnIndex(KEY_SUBCATEGORY);
			int KEY_COMMENT_index =cursor.getColumnIndex(KEY_COMMENT);
			int KEY_TIMESTAMP_index =cursor.getColumnIndex(KEY_TIMESTAMP);
			int KEY_DATE_index =cursor.getColumnIndex(KEY_DATE);

			//Iterating through rows in the image database table
			for (int i=0; i<cursor.getCount(); i++) 
			{
				String tempUri=cursor.getString(KEY_IMAGE_URI_index);
				File f = new File((Uri.parse(tempUri)).getPath());
				ImageEntry temp=new ImageEntry();
				//Check if the image exists on the external Storage
				if(f.exists())
				{  
					//Retrieve all details for the image
					temp.imageUri=Uri.parse(tempUri);
					temp.comment=cursor.getString(KEY_COMMENT_index);
					temp.date=cursor.getString(KEY_DATE_index);
					temp.timestamp=cursor.getString(KEY_TIMESTAMP_index);
					if(!(cursor.isNull(KEY_LOCATION_index)))
						temp.location=cursor.getString(KEY_LOCATION_index);
					if(!(cursor.isNull(KEY_SUBCATEGORY_index)))
						temp.subcategory=cursor.getString(KEY_SUBCATEGORY_index);				  
					if(!(cursor.isNull(KEY_LOCATION_URL_index)))
						temp.locationurl=cursor.getString(KEY_LOCATION_URL_index);
					imageEntryList.add(temp);
				}
				else
				{
					//Deletes entry for an image that no longer exists on the external Storage
					deleteImageEntry(cursor.getInt(0));Log.i(TAG,"A row deleted");
				}

				cursor.moveToNext();
			}

		}
		cursor.close();
		return imageEntryList;


	} }
