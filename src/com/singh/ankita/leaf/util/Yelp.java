package com.singh.ankita.leaf.util;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;


import android.content.Context;
import android.util.Log;

public class Yelp {
	OAuthService service;
	Token accessToken;


	public Yelp(Context context,String consumerKey, String consumerSecret, String token, String tokenSecret) {
		this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
	}

   //search for query on Yelp
	public ArrayList<YelpResult> search(String term, double latitude, double longitude) {

		ArrayList<YelpResult> venueList = new ArrayList<YelpResult>();
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
		request.addQuerystringParameter("term", term);
		request.addQuerystringParameter("ll", latitude + "," + longitude);
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();

        //Extract appropriate details from the JSON Web response
		JSONObject jsonObj;
		try {
			jsonObj = (JSONObject) new JSONTokener(response.getBody()).nextValue();
			JSONArray items	= (JSONArray) jsonObj.getJSONArray("businesses");
			int ilength 		= items.length();

			for (int j = 0; j < ilength; j++)
			{
				JSONObject item = (JSONObject) items.get(j);
				YelpResult result 	= new YelpResult();

				result.businessName		= item.getString("name");
				result.address=item.getJSONObject("location").getJSONArray("display_address").getString(0);
				result.phoneNumber=item.getString("phone");
				result.reviewCount=item.getInt("review_count");
				result.yelpRating=item.getString("rating_img_url_large");
				result.latitude=item.getJSONObject("location").getJSONObject("coordinate").getDouble("latitude");
				result.longitude=item.getJSONObject("location").getJSONObject("coordinate").getDouble("longitude");

				venueList.add(result);
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return venueList;
	}



}
