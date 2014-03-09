package com.singh.ankita.leaf.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

//This Class scales and/or adjusts the orientation of the image 
public class ImageOrientation {

	private Context context;
	private Uri uri;
	private int targetWidth;
	private int targetHeight;
	private int rotation;
	
	public ImageOrientation(Context c)
	{context=c;
	}
    
	public Bitmap fixOrientation(Uri uri, int targetWidth, int targetHeight)
	{
		this.targetHeight  = targetHeight;
		this.targetWidth   = targetWidth;
		this.uri           = uri;
		ExifInterface exif = null;
		try {
			 exif = new ExifInterface(uri.getPath());//Retrieve the EXIF Tag for the JPEG image
				
			}
			catch (IOException e) {
				Log.e("Display Album Adapter:","EXIFInterface image could not be opened");
				e.printStackTrace();
			}
		int temp= exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);//Retrieve the image orientation information from the EXIF Tag of the image 
		//set rotation required based on the orientation tag value
		if(temp==6){ rotation=90;return adjustOrientation();}
		else if (temp==3){rotation=180; return adjustOrientation();}
		else if(temp==8){rotation=270; return adjustOrientation();}
		else
		{
			try {
				
				return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
			
			} catch (FileNotFoundException e) {
				Log.e("BitmapFactory:","BitmapFactory Error");
				e.printStackTrace();
				return null;
			}
		}
	
	}
	
	
	public  Bitmap adjustOrientation() {
	    Bitmap bitmap = null;
	    
	    try {
	        // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	       

	        // Adjust extents
	        int sourceWidth, sourceHeight;
	        if (rotation == 90 || rotation == 270) {
	            sourceWidth = options.outHeight;//If rotation required then interchange source height and widths 
	            sourceHeight = options.outWidth;
	        } else {
	            sourceWidth = options.outWidth;
	            sourceHeight = options.outHeight;
	        }
	        
	        if(targetWidth==0)targetWidth=sourceWidth;
	        if(targetHeight==0)targetHeight=sourceHeight;

	        // Calculate the maximum required scaling ratio if required and load the bitmap
	        if (sourceWidth > targetWidth || sourceHeight > targetHeight) {
	            float widthRatio = (float)sourceWidth / (float)targetWidth;
	            float heightRatio = (float)sourceHeight / (float)targetHeight;
	            float maxRatio = Math.max(widthRatio, heightRatio);
	            options.inJustDecodeBounds = false;
	            options.inSampleSize = (int)maxRatio;
	            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
	        } else {
	            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
	        }
                
	        // Rotate the bitmap if required
	        if (rotation > 0) {
	            Matrix matrix = new Matrix();
	            matrix.postRotate(rotation);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	        }

	        // Re-scale the bitmap if necessary
	        sourceWidth = bitmap.getWidth();
	        sourceHeight = bitmap.getHeight();
	        if (sourceWidth != targetWidth || sourceHeight != targetHeight) {
	            float widthRatio = (float)sourceWidth / (float)targetWidth;
	            float heightRatio = (float)sourceHeight / (float)targetHeight;
	            float maxRatio = Math.max(widthRatio, heightRatio);
	            sourceWidth = (int)((float)sourceWidth / maxRatio);
	            sourceHeight = (int)((float)sourceHeight / maxRatio);
	            bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
	        }
	    } catch (Exception e) {
	    }
	    return bitmap;
	}
}
