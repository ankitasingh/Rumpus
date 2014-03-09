package com.singh.ankita.leaf.screens;

import java.util.ArrayList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.app.ProgressDialog;
import android.content.Intent;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.adapters.DisplayAlbumAdapter;
import com.singh.ankita.leaf.adapters.ImageDbAdapter;
import com.singh.ankita.leaf.util.ImageEntry;


public class MainActivity extends SherlockActivity {

	private DisplayAlbumAdapter displayAlbumAdapter;
	private ImageDbAdapter imageDbAdapter;
	private ProgressDialog mProgress;
	private ArrayList<ImageEntry> imageEntryList;
	private ListView mListView;



	//Inflating Action Bar with corresponding menu defined in res/menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.album);//Sets Activity layout to the one specified in the res/album.xml file

		
		ActionBar actionBar = getSupportActionBar();//Enabling Action Bar
		actionBar.setDisplayHomeAsUpEnabled(false);//Disabling Home Arrow on the Action Bar
        
		//initialization
		displayAlbumAdapter=new DisplayAlbumAdapter(this);
		mListView=(ListView) findViewById(R.id.album_list);
		imageEntryList=new ArrayList<ImageEntry>();
		mProgress		= new ProgressDialog(this);
		
		mProgress.setMessage("Loading Images ...");//Displays Loading message while image details are being retrieved and displayed
		//loading images from the SQLite database and the External SD Card
		loadAlbumPhotos();

		//Displaying selected Enlarged image and details when the image is selected from the list
		mListView.setOnItemClickListener(new OnItemClickListener() 
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long arg3) {

				mListView.setClickable(false);
				ImageEntry imageEntry = (ImageEntry)displayAlbumAdapter.getItem(position);
				Intent intent = new Intent(MainActivity.this,DisplayAlbumImage.class);	//Create new intent to call DisplayAlbumImage Activity		
				intent.setData(imageEntry.imageUri);//Pass Image Uri as data to the called activity through the intent
				if(imageEntry.location!=null)intent.putExtra("LOCATION", imageEntry.location);//Pass Image Location to the called activity
				if(imageEntry.comment!=null)intent.putExtra("CAPTION", imageEntry.comment);//Pass Image Caption to the called activity
				if(imageEntry.subcategory!=null)intent.putExtra("SUBCATEGORY", imageEntry.subcategory);//Pass Image subcategory to the called activity
				if(imageEntry.locationurl!=null)intent.putExtra("LOCATIONURL", imageEntry.locationurl);//Pass Image Location Url data to the called activity
				intent.putExtra("DATE", imageEntry.date);	//Pass Image Date to the called activity				
				startActivity(intent);    
			}
		}
				);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.menu_camera://Start Image Capture and Modify Image Details Activity
			Intent intent = new Intent(this, ModifyImageActivity.class);
			startActivity(intent);
			return true;
			
		case R.id.search://Start Search On Yelp Activity
			Intent intent2=new Intent(this,SearchOnYelp.class);
			startActivity(intent2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}



	public void loadAlbumPhotos()
	{
		mProgress.show();
		imageDbAdapter=new ImageDbAdapter(this);

        //Thread that retrieves all the images from the database
		new Thread() {
			@Override
			public void run() {
				int what = 0;

				try {
					//Database operations
					imageDbAdapter.open();
					imageEntryList=imageDbAdapter.fetchAllImageEntries();
					imageDbAdapter.close();
				} catch (Exception e) {
					what = 1;
					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what));
			}
		}.start();
	}
    
	//Handler to handle results of the loadAlbumPhotos Thread
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgress.dismiss();

			if (msg.what == 0) {
				if (imageEntryList.size() == 0) {
					//if album size is zero display empty album toast
					Toast.makeText(MainActivity.this, "The album is Empty", Toast.LENGTH_SHORT).show();
					imageDbAdapter.close();
					return;
				}
                //Pass the ArrayList of image details received from database to the adapter
				displayAlbumAdapter.setData(imageEntryList);   
				//Set the listview with the corresponding adapter
				mListView.setAdapter(displayAlbumAdapter);
			} else {
				//Error retrieving images from database
				Toast.makeText(MainActivity.this, "Failed to load photos from the Album", Toast.LENGTH_SHORT).show();
				imageDbAdapter.close();
				return;
			}
		}
	};	

	@Override
	protected void onResume()
	{super.onResume();
	mListView.setClickable(true);
	}


}


