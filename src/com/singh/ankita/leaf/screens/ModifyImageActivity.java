package com.singh.ankita.leaf.screens;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.R.array;
import com.singh.ankita.leaf.R.id;
import com.singh.ankita.leaf.R.layout;
import com.singh.ankita.leaf.R.menu;
import com.singh.ankita.leaf.adapters.ImageDbAdapter;
import com.singh.ankita.leaf.util.ImageOrientation;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyImageActivity extends SherlockActivity  {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static int CHECK_IN_REQUEST_CODE=400;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private Uri imageCaptureUri;	
	private static SharedPreferences settings;
	private static SharedPreferences.Editor prefEditor;

	private ProgressDialog mProgress;
	private ImageDbAdapter imageDbAdapter;
	private ImageOrientation imageOrientation;
	private String CHECK_IN_LOCATION=null;
	private String CATEGORY=null;
	private String SUBCATEGORY=null;
	private String CHECK_IN_LOCATION_URL=null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);	

		//Singleton Pattern through Shared preferences 
		settings = getSharedPreferences("settings", MODE_PRIVATE);
		prefEditor = settings.edit();

		//Check for availability of camera application
		boolean isCameraAvailable=isIntentAvailable(getBaseContext(),MediaStore.ACTION_IMAGE_CAPTURE);
		if(isCameraAvailable)
		{	
			setContentView(R.layout.modify_image);//Sets Activity layout to the one specified in the res/modify_image.xml file
			ActionBar actionBar = getSupportActionBar();//Enabling Action Bar
			actionBar.setDisplayHomeAsUpEnabled(false);//Disabling Home Arrow on the Action Bar
			takePictureFromCamera();	//Start camera activity
		}
		else
		{
			Toast.makeText(ModifyImageActivity.this, "No Camera Application Found", Toast.LENGTH_SHORT).show();//Display message if Camera Application not found
		}


	}

	//Result from Camera and Rumpus Check In Activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);


		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)//Result from Camera Activity
		{
			//If Image Captured Using camera
			if (resultCode == Activity.RESULT_OK) {
				//Shared Preferences for camera set to false on reception of results
				prefEditor.putString("usedOnce", "false");
				prefEditor.commit();
				// After Capturing Image
				loadImageSetup();

			} else //If Image Capture using Camera was cancelled
				if (resultCode == Activity.RESULT_CANCELED) {
				//Redirect to Home Activity if Camera Activity is Cancelled
				Intent intent=new Intent(ModifyImageActivity.this,MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				startActivity(intent);
			}

			else {

			}
		}  

		else{
			
			if (requestCode == CHECK_IN_REQUEST_CODE) //Result from Rumpus Check In Activity
			{
				TextView txt=(TextView)findViewById(R.id.textView2);
				//If a Location was selected by the user
				if (resultCode == Activity.RESULT_OK)
				{	             
					//Displaying Checked in location on Screen
					txt.setText("At:  "+data.getStringExtra("location"));	    			    		
					txt.setVisibility(View.VISIBLE);
					CHECK_IN_LOCATION=data.getStringExtra("location");
					if(data.hasExtra("locationurl"))CHECK_IN_LOCATION_URL=data.getStringExtra("locationurl");
				} else if (resultCode == Activity.RESULT_CANCELED) //If user cancelled Check In Activity
				{
					//Setting location to null when the Rumpus Check In Activity is cancelled
					txt.setText("");
					txt.setVisibility(View.GONE);
					CHECK_IN_LOCATION=null;
				} 


			}   

		}
	}


	public void loadImageSetup()
	{
		imageDbAdapter=new ImageDbAdapter(this);
		imageOrientation=new ImageOrientation(this);
		mProgress		= new ProgressDialog(this);
		mProgress.setMessage("Loading Image ...");
		Bitmap mBitmap=null;

		//Adjusting the orientation of image and diplaying it
		mBitmap=imageOrientation.fixOrientation(imageCaptureUri,500,600);


		ImageView image = (ImageView) findViewById(R.id.imageView1);
		image.setImageBitmap(mBitmap);

		//Hiding the keypad if user clicks outside the Edit text
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.editText1)).getWindowToken(), 0);

		//Response to Check In button
		final Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				//Starting Rumpus Check In Activity
				Intent intent=new Intent(ModifyImageActivity.this,RumpusCheckIn.class);				
				startActivityForResult(intent,CHECK_IN_REQUEST_CODE);
				return true;
			}        	  
		}); 

		final Spinner spinner1= (Spinner) findViewById(R.id.spinner1);
		final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);

		//Loading category options from the corresponding resource xml file
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
				R.array.category, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter1);
		spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			//Responding to user selection for the category spinner
			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1, int arg2,
					long arg3) {

				Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
				String opt=(String)parent.getItemAtPosition(arg2);

				//Loading subcategory options from the corresponding  resource xml file
				if(opt.equals("Sports")){
					CATEGORY="Sports";
					ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getBaseContext(),
							R.array.activeLife, android.R.layout.simple_spinner_item);		    
					adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		     
					spinner2.setAdapter(adapter2);

				}
				else if(opt.equals("Arts and Entertainment")){
					CATEGORY="Arts and Entertainment";
					ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getBaseContext(),
							R.array.arts, android.R.layout.simple_spinner_item);
					adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner2.setAdapter(adapter2);

				}
				else if(opt.equals("Select")){
					CATEGORY=null;
					ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getBaseContext(),
							R.array.defaultSpinner, android.R.layout.simple_spinner_item);
					adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);					
					SUBCATEGORY=null;
					spinner2.setAdapter(adapter2);

				}


			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getBaseContext(),
						R.array.defaultSpinner, android.R.layout.simple_spinner_item);
				Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
				adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				CATEGORY=null;
				spinner2.setAdapter(adapter2);

			}     

		} );

		//Responding to user selection for the subcategory spinner
		spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int arg2, long arg3) {
				String temp=(String)parent.getItemAtPosition(arg2);
				if(!(temp.equals("Select")))SUBCATEGORY=temp;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				SUBCATEGORY=null;

			}});


	}	

	//Method that checks if a particular intent is available
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}


	//Start Camera Activity
	public void takePictureFromCamera()
	{   

        
		String val=settings.getString("usedOnce", "false");
		if(val.equals("false"))//Checking value of usedOnce in shared Preferences to ensure that camera activity is called only once
		{
			try
			{
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				imageCaptureUri = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE)); // create a file to save the image
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri); // set the image file name		       
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				prefEditor.putString("usedOnce", "true");//Setting value of usedOnce in sharedPreferences to true
				prefEditor.commit();

			}
			catch(ActivityNotFoundException anfe){
				//display an error message
				String errorMessage = "Your device doesn't support capturing images!";
				Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
				toast.show();
			}

		}
	}

	//Create a File for saving an image or video 
	private static File getOutputMediaFile(int type){
		boolean hasWritableExternalStorage = hasStorage(true);

		if(hasWritableExternalStorage)
		{
			File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(		
					Environment.DIRECTORY_PICTURES), "RUMPUS");

			if (! mediaStorageDir.exists()){
				if (! mediaStorageDir.mkdirs()){
					Log.d("Rumpus", "failed to create directory");
					return null;
				}
			}

			// Create a media file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			File mediaFile;
			if (type == MEDIA_TYPE_IMAGE){
				mediaFile = new File(mediaStorageDir.getPath() + File.separator +
						"IMG_"+ timeStamp + ".jpg");

			} else {
				return null;
			}

			return mediaFile;
		}
		else
		{
			Log.d("Rumpus", "No Writable External Storage Found");
			return null;
		}
	}

	//Check if External SD  Available
	static public boolean hasStorage(boolean requireWriteAccess) {

		String state = Environment.getExternalStorageState();
		Log.v("MainActivity:", "storage state is " + state);

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			if (requireWriteAccess) {
				boolean writable = checkFsWritable();
				Log.v("MainActivity:", "storage writable is " + writable);
				return writable;
			} else {
				return true;
			}
		} else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	//Check if External SD  is writable
	private static boolean checkFsWritable() {

		String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM";
		File directory = new File(directoryName);
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				return false;
			}
		}
		return directory.canWrite();
	}


	protected void onStart()
	{
		super.onStart();
		prefEditor.putString("usedOnce", "false");
		prefEditor.commit(); 

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save://Save the image details to the SQLite Database
		{
			mProgress.setMessage("Saving Image ...");
			HashMap<String,String> hm=new HashMap<String,String>();//Create HashMap for storing image details
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());//Get Current timestamp
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			hm.put("timestamp", timeStamp);
			if(SUBCATEGORY!=null)hm.put("subcategory", SUBCATEGORY);
			if(CATEGORY!=null)hm.put("category", CATEGORY);
			if(CHECK_IN_LOCATION!=null)hm.put("location", CHECK_IN_LOCATION);
			if(CHECK_IN_LOCATION_URL!=null)hm.put("locationurl", CHECK_IN_LOCATION_URL);
			hm.put("comment", ((EditText)findViewById(R.id.editText1)).getText().toString());
			hm.put("date", date);
			hm.put("imageuri",imageCaptureUri.toString());

			imageDbAdapter.open();

			long success=imageDbAdapter.saveImageEntry(hm);//Pass image details in hashmap to the Image Database Adapter
			if(success>-1)
			{   //Image Details saved successfully in the Rumpus Database
				Log.i("ModifyImageActivity","Image stored successfully");
				Toast.makeText(ModifyImageActivity.this, "Image saved Successfully", Toast.LENGTH_SHORT).show();
				imageDbAdapter.close();
			}
			else
			{   //Error saving image details into the Rumpus database
				imageDbAdapter.close();
				Toast.makeText(ModifyImageActivity.this, "Image could not be Saved", Toast.LENGTH_SHORT).show();
			}

			//Redirect to the Home Screen
			Intent intent=new Intent(ModifyImageActivity.this,MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			startActivity(intent);
			return true;

		}

		case R.id.discard:
			//Delete Captured image and redirect to Home Activity
			File f = new File(imageCaptureUri.getPath());
			if(f.exists())f.delete();
			Intent intent=new Intent(ModifyImageActivity.this,MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			startActivity(intent);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	//Inflating Action Bar with corresponding menu defined in res/menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.modify_image_menu, menu);
		return true;
	}



}
