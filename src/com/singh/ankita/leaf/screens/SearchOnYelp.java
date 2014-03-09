package com.singh.ankita.leaf.screens;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.R.id;
import com.singh.ankita.leaf.R.layout;
import com.singh.ankita.leaf.R.menu;
import com.singh.ankita.leaf.adapters.YelpAdapter;
import com.singh.ankita.leaf.util.Yelp;
import com.singh.ankita.leaf.util.YelpResult;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchOnYelp extends SherlockActivity {

	private Yelp yelp;
	private ListView mListView;
	private YelpAdapter mAdapter;
	private ArrayList<YelpResult> mYelpList;
	private ProgressDialog mProgress;
	private LocationManager locationManager;
	private String provider;
	private double lat,lon;
	private String searchItem;

	//Developer Secret Codes for making requests to the Yelp API
	public static final String CONSUMER_KEY    = "JdWfC-j0bUbCD8s0XRk1dw";
	public static final String CONSUMER_SECRET = "wx3Gb8BQZvOOXrnNe7OazX8BFOo";
	public static final String TOKEN           = "D6COJlZUI_ZtHrbG0PprHa3Nt-WjcixV";
	public static final String TOKEN_SECRET    = "3QC682bxP2-ELkS4WAVD50QuTHY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_on_yelp);//Sets Activity layout to the one specified in the res/search_on_yelp.xml file
		ActionBar actionBar = getSupportActionBar();//Enable Action bar
		actionBar.setDisplayHomeAsUpEnabled(false);//Disable Back to home Arrow on Action Bar
		
		mListView	= (ListView) findViewById(R.id.listView1);	
		yelp 		= new Yelp(this, CONSUMER_KEY, CONSUMER_SECRET,TOKEN,TOKEN_SECRET);
		mYelpList	= new ArrayList<YelpResult>();//For storing places returned by Yelp API
		mAdapter    = new YelpAdapter(this); 		
		mProgress	= new ProgressDialog(this);

		//Retrieving latitude and longitude
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
		Location location = locationManager.getLastKnownLocation(provider);
                        
		if(location==null)
		{ if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))//Check if GPS is enabled
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		else   buildAlertMessageNoGps();//Ask user to enable GPS if location is null
		}
		lat = (double) (location.getLatitude());//Extract latitude from location
		lon = (double) (location.getLongitude());//Extract longitude from location

		final Button button1 = (Button) findViewById(R.id.button1);

		button1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				EditText mEditText=(EditText)findViewById(R.id.editText1);
				searchItem                  = (mEditText).getText().toString();
				//Hiding the Keypad
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

                //No search query entered
				if(searchItem.equals(""))
				{  
					Toast.makeText(SearchOnYelp.this, "Please enter the item to be searched", Toast.LENGTH_SHORT).show();
					mListView.setAdapter(null);
					return true;
				}
				else
				{
					mProgress.setMessage("Fetching data ...");
					loadYelpPlaces(searchItem,lat, lon);//Retrieving results from Yelp API
					return true;
				}
			}        	  
		});

	}

	//Retrieve places nearby using Yelp API
	private void loadYelpPlaces(final String term,final double latitude, final double longitude ) {
		mProgress.show();

		new Thread() {//New thread to fetch results from Yelp API
			@Override
			public void run() {
				int what = 0;

				try {

					mYelpList = yelp.search(term,latitude, longitude);//Fetch Places from the Yelp API
				} catch (Exception e) {
					what = 1;
					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what));
			}
		}.start();
	}

	//Handler to handle results from the Yelp API Thread
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgress.dismiss();

			if (msg.what == 0) {
				if (mYelpList.size() == 0) {
					//If zero places are retrieved for the given latitude and longitude from the Yelp API
					Toast.makeText(SearchOnYelp.this, "No results available for the query", Toast.LENGTH_SHORT).show();

				}

				mAdapter.setData(mYelpList);    			
				mListView.setAdapter(mAdapter);
			} else {
				//Error retrieving Places from the Yelp API
				Toast.makeText(SearchOnYelp.this, "Failed to load results for the query", Toast.LENGTH_SHORT).show();
				return;
			}
		}
	};	


	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, locationListener);
	}


	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}


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
					final DialogInterface dialog, final int id) {
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

	//Response for menu on Action Bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_camera://Capture a new image
			Intent intent = new Intent(this, ModifyImageActivity.class);
			startActivity(intent);
			return true;
		case R.id.home://Return to Home Screen
			Intent intent2=new Intent(SearchOnYelp.this,MainActivity.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			startActivity(intent2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//Inflating Action Bar with corresponding menu defined in res/menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.yelp_search_menu, menu);
		return true;
	}

}