package com.singh.ankita.leaf.screens;

import java.util.ArrayList;

import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.adapters.NearbyAdapter;
import com.singh.ankita.leaf.util.FourSquareApp;
import com.singh.ankita.leaf.util.FourSquareVenue;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class RumpusCheckIn extends Activity {

	private FourSquareApp mFsqApp;
	private ListView mListView;
	private NearbyAdapter mAdapter;
	private ArrayList<FourSquareVenue> mNearbyList;
	private ProgressDialog mProgress;
	private LocationManager locationManager;
	private String provider;
	private double lat,lon;

	//Developer Secret Codes for making requests to the FourSquare API
	public static final String CLIENT_ID = "******************************";
	public static final String CLIENT_SECRET = "************************************";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_in);//Sets Activity layout to the one specified in the res/check_in.xml file
		mListView					= (ListView) findViewById(R.id.listView1);
		mFsqApp 		= new FourSquareApp(this, CLIENT_ID, CLIENT_SECRET);
		mNearbyList		= new ArrayList<FourSquareVenue>();//For Storing Venues returned by FourSquare API
		mAdapter=new NearbyAdapter(this); 
		mProgress		= new ProgressDialog(this);
		mProgress.setMessage("Loading data ...");//Displays Loading message while Venues are being fetched

         //Retrieve Current Location
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
		Location location = locationManager.getLastKnownLocation(provider);
		
		if(location==null)
		{
			if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))//Check if GPS is enabled
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		else   buildAlertMessageNoGps();//Ask user to enable GPS if location is null
		}

		lat = (double) (location.getLatitude());//Extract latitude from location
		lon = (double) (location.getLongitude());//Extract longitude from location
		loadNearbyPlaces(lat, lon);


		mListView.setClickable(true);
		
		//Method that handles user selection of a Venue to Check In
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mListView.setClickable(false);
				FourSquareVenue v=(FourSquareVenue) mAdapter.getItem(arg2);
				String location = v.name;//Extract location name
				String url=v.locationurl;//Extract location website url 
				Intent returnIntent = new Intent();
				returnIntent.putExtra("location",location);//Pass location as result back to the parent Activity
				if(url!=null)returnIntent.putExtra("locationurl", url);//Pass location website url as result back to the parent Activity
				setResult(RESULT_OK,returnIntent);     
				finish();

			}

		});

		
       //Cancel Button
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED,returnIntent); //Cancel Check In and return Cancel Result to the parent activity  
				finish();
				return true;
			}        	  
		});  

	}


//Retrieve places nearby using FourSquare API
	private void loadNearbyPlaces(final double latitude, final double longitude) {
		mProgress.show();

		new Thread() {//New thread to fetch results from FourSquare API
			@Override
			public void run() {
				int what = 0;

				try {

					mNearbyList = mFsqApp.getNearby(latitude, longitude);//Fetch Venues from FourSquare API
				} catch (Exception e) {
					what = 1;
					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what));
			}
		}.start();
	}

	//Handler to handle results from the FourSquare API thread
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgress.dismiss();

			if (msg.what == 0) {
				if (mNearbyList.size() == 0) {
					//If zero venues are retrieved for the given latitude and longitude from the FourSquare API
					Toast.makeText(RumpusCheckIn.this, "No nearby places available", Toast.LENGTH_SHORT).show();
					return;
				}

				mAdapter.setData(mNearbyList);    			
				mListView.setAdapter(mAdapter);
			} else {
				//Error retrieving Venues from the FourSquare API
				Toast.makeText(RumpusCheckIn.this, "Failed to load nearby places", Toast.LENGTH_SHORT).show();
			}
		}
	};	


	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, locationListener);
		mListView.setClickable(true);
	}


	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}


    //Implementing the location listener for receiving location updates
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			lat = (double) (location.getLatitude());
			lon = (double) (location.getLongitude());
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};

	//GPS Alert Box
	 private void buildAlertMessageNoGps() {
 	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setMessage("Your GPS is disabled, do you want to enable it?")
 	           .setCancelable(false)
 	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	               public void onClick(
 	               final DialogInterface dialog,  final int id) {
 	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));//Open Android Provider Settings
 	               }
 	           })
 	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	               public void onClick(final DialogInterface dialog, final int id) {
 	                    dialog.cancel();
 	               }
 	           });
 	    final AlertDialog alert = builder.create();
 	    alert.show();
 	}

}
