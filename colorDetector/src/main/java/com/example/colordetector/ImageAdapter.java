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
import java.io.FilenameFilter;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter
{
	File[] images;
	private Activity act;

	public ImageAdapter(Activity act)
	{
		this.act = act;

		if(isExternalMemoryReadable())
		{
			// Making a filter for obtain .jpg
			FilenameFilter fileNameFilter = new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					if(name.lastIndexOf('.') > 0)
					{
						int lastIndex = name.lastIndexOf('.'); // getting last index of character '.'
						String str = name.substring(lastIndex); // getting extension.
						if(str.equals(".jpg") || str.equals(".jpeg")){ return true; } // Comparing file extension with .jpg
					}
					return false;
				}
			};
			// Getting images directory
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/ColorDetector");
			if(!path.isDirectory()) path.mkdirs(); // Check if path exists, otherwise create it
			images = path.listFiles(fileNameFilter); // Getting images on the path
		}
	}

	// Abstract method inerhited
	public int getCount() { return images.length; }
	public Object getItem(int position) { return null; }
	public long getItemId(int position) { return 0; }

	// Make for every image on the path a view that contains an ImageView with image and a TextView for the title
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;

		// add ImageView and TextView contained in grid_item at the View actually processed
		LayoutInflater li = act.getLayoutInflater();
		view = li.inflate(R.layout.grid_item, null);

		final TextView txt = (TextView)view.findViewById(R.id.txt_item);
		txt.setText(images[position].getName());

		ImageView img = (ImageView)view.findViewById(R.id.img_item);
		img.setImageURI(Uri.parse(images[position].getAbsolutePath()));
		img.setScaleType(ImageView.ScaleType.CENTER_CROP);

		return view;
	}

	public boolean isExternalMemoryReadable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true; // can read and write
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			return true; // can only read
		}
		return false;
	}
}