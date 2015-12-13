/*
 * Copyright 2015 Fabio Bombace
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.colordetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class CamMainActivity extends ActionBarActivity implements CvCameraViewListener2
{
	/*** Variables ***/
	public static final String KEY_PREF_COLOR = "pref_color";
	public static final String KEY_PREF_QUALITY = "pref_quality";
	public static final String KEY_PREF_METHOD = "pref_method";
	public static final String KEY_PREF_WHAT_SAVE = "pref_what_save";
	public static final String KEY_PREF_AUTOMATIC_SHOOT = "pref_automatic_shoot";
	public static final String MESSAGE_FLAG = "com.example.ColorDetector.FLAG";

	// These are static for use them on method 'onActivityResult' without
	// pass them as extra in bundle activity (it gave me error)
	public static Bitmap bitmap, bitmapMask;

	private JavaCameraView cameraView;
	private ImageButton button;

	private Mat hsvFrame, rgbaFrame, rgbFrame, inRangeMask, filteredFrame, hChannel;

	private int camHeight, camWidth, frameDim;
	Integer timeToElapse;
	private boolean methodAuto, countDown;
	String osdSecond;

	// N.B.: OpenCV range (from min to max) values in HSV color space are
	// H = [0, 180], S = [0, 255], V = [0, 255]
	// meanwhile in the HSV space (NOT OpenCV) are
	// H = [0, 255], S = [0, 100], V = [0, 100]
	private Scalar thresMin = new Scalar(0, 0, 0);
	private Scalar thresMax = new Scalar(180, 255, 255);

	private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this)
	{
		@Override
		public void onManagerConnected(int status)
		{
			switch (status)
			{
				case LoaderCallbackInterface.SUCCESS:
				{
					cameraView.enableView();
					break;
				}
				default:
				{
					super.onManagerConnected(status);
					break;
				}
			}
		}
	};

	/*** onCreate and onResume ***/
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Load default preferences, unless the user has changed them in a previous use of the app
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// Setting window in fullscreen here because I use the compatibility library for the action bar,
		// furthermore I can't set the fullscreen from the manifest; it also inhibits the standby for the activity
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main_cam);
		// Finding the camera defined in activity_main_cam.xml and make it visible
		cameraView = (JavaCameraView) findViewById(R.id.java_cam_view);
		cameraView.setVisibility(SurfaceView.VISIBLE);
		cameraView.setCvCameraViewListener(this);
		// Finding the button to save the images, used (and displayed)
		// only if the way of capturing images is set to manual
		button = (ImageButton) findViewById(R.id.capture_button);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, loaderCallback);
	}

	/*** onPause and onDestroy ***/
	@Override
	public void onPause()
	{
		super.onPause();
		if(cameraView != null) cameraView.disableView();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(cameraView != null) cameraView.disableView();
	}

	/*** Menu ***/
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.cam_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Which voice of menu has been clicked? let see it...
		switch (item.getItemId())
		{
			// Open the option activity
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			// Open the gallery activity
			case R.id.action_picture:
				startActivity(new Intent(this, GalleryActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/* Method that will be called when CapturedFrameActivity finish,
	 * after the user has choosed to save or not the image */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// Checking if the request are made from user's choose and that are no error
		if ((requestCode == 1) && (resultCode == RESULT_OK))
		{
			// User choose:
			// flag = 0 --> don't save image; flag = 1 --> save image
			int flag = data.getIntExtra(MESSAGE_FLAG, 0);
			if(flag == 1)
			{
				if(isExternalMemoryWritable())
				{
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

					// Getting the quality the image has to be saved
					String quality = sharedPref.getString(KEY_PREF_QUALITY, "80");
					// Getting the directory where to save the image
					// N.B.: the name of the image is the timestamp
					File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/ColorDetector");
					if(!path.isDirectory()) path.mkdirs();
					// Dipendently from user's choose, we saving only the image or the color mask too
					String whatSave = sharedPref.getString(KEY_PREF_WHAT_SAVE, "");
					boolean onlyPicture;
					if(whatSave.compareTo("onlyPicture") == 0) onlyPicture = true;
					else if(whatSave.compareTo("pictureAndColorMask") == 0) onlyPicture = false;
					else
					{
						// Unexpected error; show the toast
						Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
						return;	
					}
					File img = null;
					File mask = null;

					// Make the image path
					if(onlyPicture) img = new File(path, "" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date()) + ".jpg");
					else
					{
						img = new File(path, "" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date()) + "_img.jpg");
						mask = new File(path, "" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date()) + "_mask.jpg");
					}
					OutputStream outStream;

					// Writing the image on the persistent memory
					try
					{
						outStream = new FileOutputStream(img); 
						bitmap.compress(Bitmap.CompressFormat.JPEG, Integer.parseInt(quality), outStream);
						outStream.close();

						if(mask != null)
						{
							outStream = new FileOutputStream(mask);
							bitmapMask.compress(Bitmap.CompressFormat.JPEG, Integer.parseInt(quality), outStream);
							outStream.close();
						}
					} 
					catch (FileNotFoundException e)
					{
						Toast.makeText(this, getString(R.string.write_error), Toast.LENGTH_LONG).show();
						e.printStackTrace(); 
					}
					catch (Exception e)
					{
						Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
						e.printStackTrace(); 
					}
				}
				else { Toast.makeText(this, getString(R.string.not_writeable_memory_error), Toast.LENGTH_LONG).show(); }
			}
		}
	}

	public void setColorValues(double thresArray[], double h, double s, double v)
	{
		thresArray[0] = h;
		thresArray[1] = s;
		thresArray[2] = v;
	}

	/*** Abstract method inerhited from CvCameraViewListener2 ***/
	public void onCameraViewStopped() {}

	public void onCameraViewStarted(int width, int height)
	{
		// initialize the matrix that will contain the working's frame
		rgbaFrame = new Mat();
		rgbFrame = new Mat();
		hsvFrame = new Mat();
		filteredFrame = new Mat();
		inRangeMask = new Mat();
		hChannel = new Mat();

		// Getting the dimension of the frame
		camHeight = height;
		camWidth = width;
		frameDim = height * width;

		// Variables for countdown in automatic shoot setting
		timeToElapse = 24; // 24 wait cycles of 75ms before taking the picture
		osdSecond = ""; // string containing the countdown display screen

		// Getting preferences list
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String colorSelected = sharedPref.getString(KEY_PREF_COLOR, "");

		// Getting the shooting image method choosed by the user;
		// if set to manual display the button to take the picture
		if(sharedPref.getString(KEY_PREF_METHOD, "").compareTo("manual") == 0)
		{
			methodAuto = false; 
			button.setVisibility(SurfaceView.VISIBLE);
		}
		else
		{
			methodAuto = true; 
			button.setVisibility(SurfaceView.GONE);
			// Getting the automatic capture method (countdown, immediate)
			if(sharedPref.getString(KEY_PREF_AUTOMATIC_SHOOT, "").compareTo("countdown") == 0){ countDown = true; }
			else{ countDown = false; }
		}

		// Setting threshold values according to the color currently selected
		double thresArray[] = new double[3];

		// Red --> H = 0 | 360
		if(colorSelected.equals("red"))
		{
			setColorValues(thresArray, 0, 50, 50); // Min
			thresMin.set(thresArray);
			setColorValues(thresArray, 5, 255, 255); // Max
			thresMax.set(thresArray);
		}
		// Yellow --> H = 60 / 2 = 30
		else if(colorSelected.equals("yellow"))
		{
			setColorValues(thresArray, 25, 50, 50); // Min
			thresMin.set(thresArray);
			setColorValues(thresArray, 35, 255, 255); // Max
			thresMax.set(thresArray);
		}
		// Green --> H = 120 / 2 = 60
		else if(colorSelected.equals("green"))
		{
			setColorValues(thresArray, 40, 50, 50); // Min
			thresMin.set(thresArray);
			setColorValues(thresArray, 89, 255, 255); // Max
			thresMax.set(thresArray);
		}
		// Light blue --> H = 180 / 2 = 90
		else if(colorSelected.equals("lightBlue"))
		{
			setColorValues(thresArray, 90, 50, 50); // Min
			thresMin.set(thresArray);
			setColorValues(thresArray, 109, 255, 255); // Max
			thresMax.set(thresArray);
		}
		// Blue --> H = 240 / 2 = 120
		else if(colorSelected.equals("blue"))
		{
			setColorValues(thresArray, 110, 50, 50); // Min
			thresMin.set(thresArray);
			setColorValues(thresArray, 128, 255, 255); // Max
			thresMax.set(thresArray);
		}
		// Magenta --> H = 300 / 2 = 150
		else if(colorSelected.equals("magenta"))
		{
			setColorValues(thresArray, 140, 50, 50); // Min
			thresMin.set(thresArray);
			setColorValues(thresArray, 170, 255, 255); // Max
			thresMax.set(thresArray);
		}
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame)
	{
		// The frame currently captured by the camera, converted in the color RGBA
		rgbaFrame = inputFrame.rgba();

		// Convert the frame in the HSV color space, to be able to identify the color with the thresholds
		Imgproc.cvtColor(rgbaFrame, rgbFrame, Imgproc.COLOR_RGBA2RGB); // Cant't convert directly rgba->hsv
		Imgproc.cvtColor(rgbFrame, hsvFrame, Imgproc.COLOR_RGB2HSV);

		// Create a mask with ONLY zones of the chosen color on the frame currently captured
		Core.inRange(hsvFrame, thresMin, thresMax, inRangeMask);
		filteredFrame.setTo(new Scalar(0, 0, 0)); 
		rgbFrame.copyTo(filteredFrame, inRangeMask);

		// if the method of shooting image is set to manual, exit and return the filtered image...
		if(!methodAuto){ return filteredFrame; }

		//...else it was setted the automatic method, so continue with the method
		// Check the H channel of the image to see if the searched color is present on the frame
		Core.extractChannel(filteredFrame, hChannel, 0);

		/* There are two method to verify the color presence; below a little explanation */

		/* checkRange: if almost one pixel of the searched color is found, continue with the countdown
		 * Pro -> fast.
		 * Versus -> less accurate, possible presence of false positive depending the quality of the camera
		 * if(!Core.checkRange(hChannel, true, 0, 1)){ */

		/* Percentage: count the pixel of the searched color, and if there are almost the
		 * 0.1% of total pixel of the frame with the searched color, continue with the countdown
		 * Pro: more accurate, lower risk of false positive
		 * Versus: slower than checkRange
		 * N.B.: the threshold percentage is imposted with a low value, otherwise small object will not be seen */

		int perc = Core.countNonZero(hChannel); // Percentage
		if(perc > (frameDim * 0.001))
		{
			// if the shooting method is setted to 'immediate', the photo is returned now;
			// otherwise continue with the countdown
			if(!countDown){ takePicture(); return rgbaFrame; }
			
			// 'point' is where the countdown will be visualized; in that case at
			//  a quarter of height and width than left up angle
			Point point = new Point(rgbaFrame.cols() >> 2 ,rgbaFrame.rows() >> 2);

			// Update the osd countdown every 75*8 ms (if color searched is present)
			// Use the division in 75 ms cause a higher value would give the user the feeling of screen/app 'blocked'.
			if(timeToElapse % 8 == 0)
			{
				if(osdSecond.compareTo("") == 0) osdSecond = ((Integer)(timeToElapse >> 3)).toString();
				else osdSecond = osdSecond.concat(".." + (((Integer)(timeToElapse >> 3)).toString()));
				Core.putText(rgbaFrame, osdSecond, point, 1, 3, Scalar.all(255));
			}
			timeToElapse -= 1; 

			// the user has framed an object for more than 3 seconds; shoot the photo
			if(timeToElapse <= 0)
			{
				timeToElapse = 24;
				takePicture();
			}
			// the user has framed an object for less than 3 seconds; wait
			else
			{
				try { synchronized (this){ wait(75); } }
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
		// the user has NOT framed a color searched object; reset osd
		else
		{
			timeToElapse = 24;
			osdSecond = "";
		}
		return rgbaFrame;
	}

	public boolean isExternalMemoryWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) { return true; } 
		return false;
	}

	public void takePicture()
	{
		// Make bitmaps to display images and (if the user want) save them on storage memory
		bitmap = Bitmap.createBitmap(camWidth, camHeight, Bitmap.Config.ARGB_8888) ;
		Utils.matToBitmap(rgbFrame, bitmap);

		bitmapMask = Bitmap.createBitmap(camWidth, camHeight, Bitmap.Config.ARGB_8888) ;
		Utils.matToBitmap(filteredFrame, bitmapMask);

		// Showing the image at the user, and ask if save them or not; the response will be processed on method onActivityResult
		Intent intent = new Intent(this, CapturedFrameActivity.class);
		startActivityForResult(intent, 1);
	}

	public void takePicture(View view)
	{
		// Make bitmaps to display images and (if the user want) save them on storage memory
		bitmap = Bitmap.createBitmap(camWidth, camHeight, Bitmap.Config.ARGB_8888) ;
		Utils.matToBitmap(rgbFrame, bitmap);

		bitmapMask = Bitmap.createBitmap(camWidth, camHeight, Bitmap.Config.ARGB_8888) ;
		Utils.matToBitmap(filteredFrame, bitmapMask);

		// Showing the image at the user, and ask if save them or not; the response will be processed on method onActivityResult
		Intent intent = new Intent(this, CapturedFrameActivity.class);
		startActivityForResult(intent, 1);
	}
}