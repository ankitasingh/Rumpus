package com.singh.ankita.leaf.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import com.singh.ankita.leaf.R;
import com.singh.ankita.leaf.util.FourSquareVenue;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

//DisplayAlbumAdapter acts as a bridge between view each FourSquare venue on the list and the corresponding data for that view
public class NearbyAdapter extends BaseAdapter {
	private ArrayList<FourSquareVenue> mVenueList;//ArrayList consisting of venues retrieved from the FourSquare API
	private LayoutInflater mInflater;//Inflater to inflate the view for each venue on the list view

	//Constructor with corresponding Activity Context as parameter
	public NearbyAdapter(Context c) {
		mInflater 			= LayoutInflater.from(c);
	}

	//sets data for the adapter
	public void setData(ArrayList<FourSquareVenue> poolList) {
		mVenueList = poolList;
	}

	//returns number of items in the list
	public int getCount() {
		return mVenueList.size();
	}

	//returns ith item on the list
	public Object getItem(int position) {
		return mVenueList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	//Generates view for all items in the ArrayList to form the complete ListView
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView	=  mInflater.inflate(R.layout.nearby_list, null);
			//Creating and Initializing ViewHolder for holding the view for an item in the list
			holder = new ViewHolder();

			holder.mNameTxt 		= (TextView) convertView.findViewById(R.id.tv_name);
			holder.mAddressTxt 		= (TextView) convertView.findViewById(R.id.tv_address);
			holder.mHereNowTxt 		= (TextView) convertView.findViewById(R.id.tv_here_now);
			holder.mDistanceTxt 	= (TextView) convertView.findViewById(R.id.tv_distance);
	

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//Assigning value from arraylist to corresponding item on the listview
		FourSquareVenue venue 	= mVenueList.get(position);

		holder.mNameTxt.setText(venue.name);
		holder.mAddressTxt.setText(venue.address);
		holder.mHereNowTxt.setText("(" + String.valueOf(venue.herenow) + " people here)");
		holder.mDistanceTxt.setText(formatDistance(venue.direction));


		return convertView;
	}

    //Convert distance to String
	private String formatDistance(double distance) {
		String result = "";

		DecimalFormat dF = new DecimalFormat("00");

		dF.applyPattern("0.#");

		if (distance < 1000)
			result = dF.format(distance) + " m";
		else {
			distance = distance / 1000.0;
			result   = dF.format(distance) + " km";
		}

		return result;
	}
	//Holds view for corresponding item on the listview
	static class ViewHolder {
		TextView mNameTxt;//TextView for displaying Venue name
		TextView mAddressTxt;//TextView for displaying Venue address
		TextView mHereNowTxt;//TextView for displaying number of people at the venue
		TextView mDistanceTxt;//TextView for displaying distance from the venue
		
	}
}
