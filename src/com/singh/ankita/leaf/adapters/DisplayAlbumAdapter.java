package com.singh.ankita.leaf.adapters;


import java.util.ArrayList;

import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.util.ImageEntry;
import com.singh.ankita.leaf.util.ImageOrientation;



import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
//DisplayAlbumAdapter acts as a bridge between view each image entry on the list and the corresponding data for that view
public class DisplayAlbumAdapter extends BaseAdapter {
 
	private ArrayList<ImageEntry> mImageEntryList;//ArrayList consisting of image details retrieved from the database
	private LayoutInflater mInflater;//Inflater to inflate the view for each image entry on the list view
	private Bitmap thumbnail;//compressed thumbnail image for each image entry

	private Context context;//Holds corresponding Activity Context
	private ImageOrientation imageOrientation;//to fix orientation of retrieved image before displaying

	 //Constructor with corresponding Activity Context as parameter
	public DisplayAlbumAdapter(Context c)
	{
		mInflater 			= LayoutInflater.from(c);
		context=c;
		imageOrientation=new ImageOrientation(context);
	}

	//sets data for the adapter
	public void setData(ArrayList<ImageEntry> poolList) {
		mImageEntryList = poolList;
	}

	//returns number of items in the list
	@Override
	public int getCount() {

		return mImageEntryList.size();
	}

	//returns ith item on the list
	@Override
	public ImageEntry getItem(int arg0) {

		return mImageEntryList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	//Generates view for all items in the ArrayList to form the complete ListView
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView	=  mInflater.inflate(R.layout.album_inner, null);	
			//Creating and Initializing ViewHolder for corresponding item in the list
			holder = new ViewHolder();			
			holder.mDate 		= (TextView) convertView.findViewById(R.id.tv_date);
			holder.mLocation		= (TextView) convertView.findViewById(R.id.tv_location);
			holder.mSubcategory 		= (TextView) convertView.findViewById(R.id.tv_subcategory);
			holder.mThumbnailImg 	= (ImageView) convertView.findViewById(R.id.iv_thumbnail);	
						
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//Assigning value from arraylist to corresponding item on the listview
		ImageEntry imgEntry 	= mImageEntryList.get(position);	  
		holder.mDate.setText(imgEntry.date);
		
		if(imgEntry.location!=null)
		{holder.mLocation.setText("At : "+imgEntry.location);holder.mLocation.setVisibility(1);}
		else holder.mLocation.setVisibility(TextView.GONE);		
		
		if(imgEntry.subcategory!=null)
		{holder.mSubcategory.setText("Tried Out : "+imgEntry.subcategory);holder.mSubcategory.setVisibility(TextView.VISIBLE);}
		else holder.mSubcategory.setVisibility(1);
		
		thumbnail=imageOrientation.fixOrientation(imgEntry.imageUri,120,120);//Fixing image orientation				
		holder.mThumbnailImg.setImageBitmap(thumbnail);//setting the imageview for corresponding item in the list to thumbnail		
		holder.mThumbnailImg.setVisibility(1);

		return convertView;
	}

	//Holds view for corresponding item on the listview
	static class ViewHolder {
		TextView mDate;      //TextView for displaying date when image was taken           
		TextView mLocation;  // TextView for displaying Location where image was taken
		TextView mSubcategory;	//TextView for displaying interest subcategory for the image	
		ImageView mThumbnailImg;//ImageView for displaying image thumbnail
	}



}
