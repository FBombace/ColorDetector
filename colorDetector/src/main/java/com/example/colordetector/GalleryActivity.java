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

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.GridView;

public class GalleryActivity extends ActionBarActivity
{
	public final static String MESSAGE_POSITION = "com.example.ColorDetector.MESSAGE"; // used on method putExtra, for calling fullscreen activity

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		// Getting GridView from xml and adapting the image into
		GridView gridView = (GridView) findViewById(R.id.grid_view);
		gridView.setAdapter(new ImageAdapter(this));

		registerForContextMenu(gridView); // instruction for the context menu 

		// Setting image in fullscreen when clicked
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				callGalleryActivity(position);
			}
		});
	}

	/*** Context menu ***/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();

		switch (item.getItemId())
		{
			case R.id.delete:
				deleteImage(info.position);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	/* here (in this method) is passed the position of the file relatively at the ImageAdapter; 
	 * first obtain the list of the images in the directory and then remove the image with the passed position */
	public boolean deleteImage(int position)
	{
		// Making a filter to obtain only .jpg (or .jpeg) files in the directory
		FilenameFilter fileNameFilter = new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				if(name.lastIndexOf('.') > 0)
				{
					int lastIndex = name.lastIndexOf('.'); // getting last index of character '.'
					String str = name.substring(lastIndex); // getting extension
					if(str.equals(".jpg") || str.equals(".jpeg")){ return true; } // comparing extension with 'jpg' or 'jpeg'
				}
				return false;
			}
		};

		if(isExternalMemoryReadable())
		{
			// Path and images list.
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/ColorDetector");

			// Check if the directory that contain image exist, or create it
			if(!path.isDirectory()) path.mkdirs();
			else
			{
				// getting images list and deleting file
				File[] images = path.listFiles(fileNameFilter);
				File file = new File(path, images[position].getName());
				file.delete();

				// Update images list
				GridView gridView = (GridView) findViewById(R.id.grid_view);
				gridView.setAdapter(new ImageAdapter(this));

				return true;
			}
		}
		Toast.makeText(this, getString(R.string.not_deleted_image_error), Toast.LENGTH_LONG).show();
		return false;
	}

	/*** Menu ***/
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// menu inflater. N.B.: the elements are added at action bar
		getMenuInflater().inflate(R.menu.gallery_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Which menu object has been clicked? let see it
		switch (item.getItemId())
		{
			case R.id.action_settings: // start setting activity
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_camera: // start camera activity
				startActivity(new Intent(this, CamMainActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public boolean isExternalMemoryReadable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			// Can read and write memory
			return true;
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			// Only read
			return true;
		}
		return false;
	}

	public void callGalleryActivity(int pos)
	{
		Intent intent = new Intent(this, FullscreenImageActivity.class);
		intent.putExtra(MESSAGE_POSITION, pos);
		startActivity(intent);
	}
}
