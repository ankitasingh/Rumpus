package com.singh.ankita.leaf.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.util.YelpResult;



import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
//DisplayAlbumAdapter acts as a bridge between view each yelp entry on the list and the corresponding data for that view
public class YelpAdapter extends BaseAdapter
{
	private ArrayList<YelpResult> mYelpResultList;//ArrayList consisting of  places retrieved from the Yelp API
	private LayoutInflater mInflater;//Inflater to inflate the view for each Yelp place on the list view

	//Constructor with corresponding Activity Context as parameter
	public YelpAdapter(Context c)
	{
		mInflater 			= LayoutInflater.from(c);

	}

	//sets data for the adapter
	public void setData(ArrayList<YelpResult> poolList) {
		mYelpResultList = poolList;
	}

	//returns number of items in the list
	@Override
	public int getCount() {
		return mYelpResultList.size();
	}

	//returns ith item on the list
	@Override
	public YelpResult getItem(int arg0) {
		return mYelpResultList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	//Generates view for all items in the ArrayList to form the complete ListView
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		URL imageUrl;
		if (convertView == null) {
			convertView	=  mInflater.inflate(R.layout.yelp_list, null);

			//Creating and Initializing ViewHolder for holding all the views for an item in the list
			holder = new ViewHolder();
			holder.mNameTxt 		= (TextView) convertView.findViewById(R.id.tv_business_name);
			holder.mAddressTxt 		= (TextView) convertView.findViewById(R.id.tv_address);
			holder.mPhoneTxt 		= (TextView) convertView.findViewById(R.id.tv_phone);
			holder.mReviewedByTxt 	= (TextView) convertView.findViewById(R.id.tv_reviewed_by);
			holder.mRatingImg		= (ImageView) convertView.findViewById(R.id.iv_rating);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//Assigning value from arraylist to corresponding item on the listview
		YelpResult venue 	= mYelpResultList.get(position);
		String rating       =venue.yelpRating;
		holder.mNameTxt.setText(venue.businessName);
		holder.mAddressTxt.setText(venue.address);
		holder.mPhoneTxt.setText(venue.phoneNumber );
		holder.mReviewedByTxt.setText("Reviewed by "+venue.reviewCount);
		try {
			//Retrieving rating image from the url
			imageUrl=new URL(rating);
			InputStream is = (InputStream)imageUrl.getContent();//Fetching the rating image form the url
			Drawable image = Drawable.createFromStream(is, "src");//Creating a Drawable from fetched image
			holder.mRatingImg.setImageDrawable(image);
		} 
		catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		holder.mRatingImg.setVisibility(1);

		return convertView;
	}
	//Holds view for corresponding item on the listview
	static class ViewHolder {
		TextView mNameTxt;//TextView for displaying name of the place
		TextView mAddressTxt;//TextView for displaying address of the place
		TextView mPhoneTxt;//TextView for displaying phone number of the place
		TextView mReviewedByTxt;//TextView for displaying number of users who reviewed the place
		ImageView mRatingImg;//ImageView for displaying rating of the place
	}
}
