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

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

public class CapturedFrameActivity extends Activity
{
	int flag = 0; // flag = 0 --> don't save image; flag = 1 --> save image

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_captured_frame);

		// ImageView with the captured image
		ImageView iv1 = (ImageView)findViewById(R.id.img_original);
		iv1.setImageBitmap(CamMainActivity.bitmap);

		// ImageView with the color mask
		ImageView iv2 = (ImageView)findViewById(R.id.img_filtered);
		iv2.setImageBitmap(CamMainActivity.bitmapMask);
	}

	/*** method for the button "save image" and "continue without saving" ***/
	public void setSaveFrame(View view)
	{
		flag = 1;
		callReturnActivity();
	}

	public void setDiscardFrame(View view)
	{
		flag = 0;
		callReturnActivity();
	}

	public void callReturnActivity()
	{
		Intent intent = new Intent();
		intent.putExtra(CamMainActivity.MESSAGE_FLAG, flag);
		setResult(RESULT_OK, intent);
		finish(); // exit current activity
	}
}
