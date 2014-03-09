package com.singh.ankita.leaf.util;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;




import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;




import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

//This class handles Web Requests to the FourSquare API
public class FourSquareApp {


	private ProgressDialog mProgress;
	private String CLIENT_ID;
	private String CLIENT_SECRET;

	
	private static final String API_URL     = "https://api.foursquare.com/v2";//FourSquare API Url
	private static final String TAG         = "FoursquareApi";//Tag For Logging

	public FourSquareApp(Context context, String clientId, String clientSecret) {
		CLIENT_ID        = clientId;
		CLIENT_SECRET    = clientSecret;
		mProgress		 = new ProgressDialog(context);
		mProgress.setCancelable(false);
	}



   //Method that makes request to FourSquare API and fetches results
	public ArrayList<FourSquareVenue> getNearby(double latitude, double longitude) throws Exception {
		ArrayList<FourSquareVenue> venueList = new ArrayList<FourSquareVenue>();

		try {
			String v	= timeMilisToString(System.currentTimeMillis()); //Timestamp in String format
			String ll 	= String.valueOf(latitude) + "," + String.valueOf(longitude);
			//Request Url for FourSquare API
			URL url 	= new URL(API_URL + "/venues/search?ll=" + ll + "&client_id=" + CLIENT_ID +"&client_secret="+CLIENT_SECRET+ "&v=" + v);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);

			urlConnection.connect();
			String response		= streamToString(urlConnection.getInputStream());//Fetching Web Response in String format

			//Retrieving response in JSON format
			JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();//Converting String Web response to JSON Format

			JSONArray items	    = (JSONArray) jsonObj.getJSONObject("response").getJSONArray("venues");



			int ilength 		= items.length();//Length of the Venues Array
            //Retrieving details for each Venue from the Web response
			for (int j = 0; j < ilength; j++) {

				JSONObject item         = (JSONObject) items.get(j);//Retrieve ith Venue from the array						
				FourSquareVenue venue 	= new FourSquareVenue();						
				venue.id 		        = item.getString("id");//Retrieve Venue Id
				venue.name		        = item.getString("name");//Retrieve Venue Name
				
				if(item.has("url"))
					venue.locationurl=item.getString("url");//Retrieve Venue Website Url if the Venue Website exists

				JSONObject location = (JSONObject) item.getJSONObject("location");						
				Location loc     	= new Location(LocationManager.GPS_PROVIDER);

				loc.setLatitude(Double.valueOf(location.getString("lat")));//Retrieve latitude of Venue
				loc.setLongitude(Double.valueOf(location.getString("lng")));//Retrieve longitude of Venue

				venue.location	= loc;
				if(location.has("address"))
					venue.address	= location.getString("address");//Retrieve address of Venue if exists
				else
					venue.address="";
				
				venue.distance	= location.getInt("distance");//Retrieve distance of Venue from given latitude and longitude
				venue.herenow	= item.getJSONObject("hereNow").getInt("count");//Retrieve Number of people at Venue


				venueList.add(venue);//Add Venue to the list


			}
		} catch (Exception ex) {
			throw ex;
		}

		return venueList;
	}
//Convert InputStream to String
	private String streamToString(InputStream is) throws IOException {
		String str  = "";

		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader 	= new BufferedReader(new InputStreamReader(is));

				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
			} finally {
				is.close();
			}

			str = sb.toString();
		}

		return str;
	}

	//Convert time from long milliseconds to String 
	private String timeMilisToString(long milis) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar   = Calendar.getInstance();

		calendar.setTimeInMillis(milis);

		return sd.format(calendar.getTime());
	}

}
