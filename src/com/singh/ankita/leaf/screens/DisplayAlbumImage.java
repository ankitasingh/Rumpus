package com.singh.ankita.leaf.screens;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.adapters.ImageDbAdapter;
import com.singh.ankita.leaf.util.ImageEntry;
import com.singh.ankita.leaf.util.ImageOrientation;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

//This class displays a single image saved in the Rumpus Database along with the details
public class DisplayAlbumImage extends SherlockActivity {

	private ImageDbAdapter imageDbAdapter=null;
	private ImageEntry imageEntry=null;
	private ImageOrientation imageOrientation=null;
	private Bitmap mBitmap=null;
	private String TEMP_IMAGE_NAME="RumpusImage.jpg";

	//Inflating Action Bar with corresponding menu defined in res/menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.image_display_menu, menu);
		return true;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_image);//Sets Activity layout to the one specified in the album_image.xml file

		imageOrientation=new ImageOrientation(this);
		imageEntry=new ImageEntry();
		
		ActionBar actionBar = getSupportActionBar();//Enabling Action Bar		
		actionBar.setDisplayHomeAsUpEnabled(false);//Disabling Home Arrow on the Action Bar

		//Retrieving values from parent intent
		Intent intent=getIntent();	//Gets parent Intent			
		imageEntry.imageUri = intent.getData();//Extract Uri passed as data from parent intent
		if(intent.hasExtra("LOCATION"))
			imageEntry.location=intent.getStringExtra("LOCATION");//Extract location passed from parent intent
		if(intent.hasExtra("CAPTION"))
			imageEntry.comment=intent.getStringExtra("CAPTION");//Extract caption passed from parent intent
		if(intent.hasExtra("SUBCATEGORY"))
			imageEntry.subcategory=intent.getStringExtra("SUBCATEGORY");//Extract subcategory passed from parent intent
		if(intent.hasExtra("LOCATIONURL"))
			imageEntry.locationurl=intent.getStringExtra("LOCATIONURL");//Extract locationurl passed from parent intent
		imageEntry.date=intent.getStringExtra("DATE");//Extract date passed from parent intent

		//Initializing view objects with the ones specified in the layout xml file
		final ImageView imageView=(ImageView)findViewById(R.id.imageView1);
		final TextView txtView1=(TextView)findViewById(R.id.textView1);
		final TextView txtView2=(TextView)findViewById(R.id.textView2);

		//Retrieving image to be displayed with appropriate orientation
		mBitmap=imageOrientation.fixOrientation(imageEntry.imageUri, 0, 0);

		//Displaying image details on the screen
		StringBuilder temp=new StringBuilder();
		if(!((imageEntry.comment).equals("")))temp.append(imageEntry.comment+"\n");
		if((imageEntry.subcategory)!=null)temp.append("Tried out: "+imageEntry.subcategory);
		if(!(temp.length()==0))txtView1.setText(temp.toString());else txtView1.setVisibility(TextView.GONE);

		if((imageEntry.location)!=null)
		{	if((imageEntry.locationurl)!=null)
		{
			txtView2.setText(imageEntry.locationurl);
			String html="<a href='"+imageEntry.locationurl+"'>"+imageEntry.location+"</a> ";
			txtView2.setText(Html.fromHtml( (html)));
			txtView2.setMovementMethod(LinkMovementMethod.getInstance());
			
		}
		else txtView2.setText(imageEntry.location);

		}
		else txtView2.setVisibility(TextView.GONE);

		imageView.setImageBitmap(mBitmap);
	}

	//Action Bar 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.share://Sharing Image
			Intent share = new Intent(Intent.ACTION_SEND);
			File f1=overlay();
			share.setType("image/jpeg");
			share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f1));
			startActivity(Intent.createChooser(share, "Share Image"));
			return true;
			
		case R.id.discard://Deleting image from External SD card and the corresponding image entry in the database
			imageDbAdapter=new ImageDbAdapter(getBaseContext());//Create new Database Adapter
			imageDbAdapter.open();
			imageDbAdapter.deleteImageEntry(imageEntry.imageUri.toString());//Delete corresponding image entry in the database
			imageDbAdapter.close();
			File f = new File(imageEntry.imageUri.getPath());
			if(f.exists())f.delete();//Delete corresponding image on the External SD
			//Directing back to home screen
			Intent intent2=new Intent(DisplayAlbumImage.this,MainActivity.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			startActivity(intent2);
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	private File overlay() {
      
		
		FrameLayout view = (FrameLayout)findViewById(R.id.framelayout);
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bm = view.getDrawingCache();
		File file=null;
        
        try {
            
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            file = getBaseContext().getFileStreamPath(TEMP_IMAGE_NAME);
            if(!file.exists())file = new File(path, TEMP_IMAGE_NAME);
            fOut = new FileOutputStream(file);

            bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
          
            
        } catch(IOException e) {
            Log.v("error saving","error saving temp image");
            e.printStackTrace();
           
        }
        return file;
    }
}
