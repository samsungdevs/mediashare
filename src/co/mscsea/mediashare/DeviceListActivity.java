/*
 * Copyright 2013 Samsung Developer Relations Team (MSCSEA)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.mscsea.mediashare;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

import com.samsung.android.sdk.mediacontrol.Smc;
import com.samsung.android.sdk.mediacontrol.SmcDevice;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder.DeviceListener;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder.StatusListener;

public class DeviceListActivity extends ListActivity implements OnClickListener, OnItemClickListener {

	public static final String EXTRA_DEVICE_TYPE = "deviceType";
	public static final String EXTRA_DEVICE_ID = "deviceId";
	
	private SmcDeviceFinder deviceFinder;
	private int deviceType;
	
	private DeviceAdapter adapter;
	private RelativeLayout thisDeviceLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device);
		
		Intent intent = getIntent();
		if (intent != null) {
			deviceType = intent.getIntExtra(EXTRA_DEVICE_TYPE, SmcDevice.TYPE_IMAGEVIEWER);
		}
		
		thisDeviceLayout = (RelativeLayout) findViewById(R.id.this_device_layout);
		thisDeviceLayout.setOnClickListener(this);
		
		adapter = new DeviceAdapter(this);
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(this);
		
		/**
		 * TODO:
		 * 1. Create device finder
		 * 2. Set status listener
		 * 3. Start device finder
		 */
		//<task>
		deviceFinder = new SmcDeviceFinder(this);
		deviceFinder.setStatusListener(statusListener);
		deviceFinder.start();
		//</task>
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (deviceFinder != null) {
			deviceFinder.stop();
			deviceFinder = null;
		}
	}
	
	private StatusListener statusListener = new StatusListener() {
		
		@Override
		public void onStopped(SmcDeviceFinder deviceFinder) {
			if (DeviceListActivity.this.deviceFinder == deviceFinder) {
				deviceFinder.setDeviceListener(deviceType, null);
				deviceFinder.setStatusListener(null);
				DeviceListActivity.this.deviceFinder = null;
			}
		}
		
		@Override
		public void onStarted(SmcDeviceFinder deviceFinder, int error) {
			if (error == Smc.SUCCESS) {
				DeviceListActivity.this.deviceFinder = deviceFinder;
				
				DeviceAdapter adapter = (DeviceAdapter) getListAdapter();
				List<SmcDevice> devices = null;
				/**
				 * TODO: Get device list
				 */
				//<task>
				devices = deviceFinder.getDeviceList(deviceType);
				//</task>
				adapter.setItems(devices);
				
				/**
				 * TODO:
				 * 1. Set device listener 
				 * 2. Rescan to listen to added / removed devices
				 */
				//<task>
				deviceFinder.setDeviceListener(deviceType, deviceListener);
				deviceFinder.rescan();
				//</task>
			}
		}
	};
	
	private DeviceListener deviceListener = new DeviceListener() {
		
		@Override
		public void onDeviceRemoved(SmcDeviceFinder deviceFinder, SmcDevice device, int error) {
			DeviceAdapter adapter = (DeviceAdapter) getListAdapter();
			List<SmcDevice> devices = null;
			/**
			 * TODO: Get device list
			 */
			//<task>
			devices = deviceFinder.getDeviceList(deviceType);
			//</task>
			adapter.setItems(devices);
		}
		
		@Override
		public void onDeviceAdded(SmcDeviceFinder deviceFinder, SmcDevice device) {
			DeviceAdapter adapter = (DeviceAdapter) getListAdapter();
			List<SmcDevice> devices = null;
			/**
			 * TODO: Get device list
			 */
			//<task>
			devices = deviceFinder.getDeviceList(deviceType);
			//</task>
			adapter.setItems(devices);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.this_device_layout: {
				Intent data = new Intent();
				data.putExtra(EXTRA_DEVICE_ID, "");
				setResult(RESULT_OK, data);
				finish();
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SmcDevice device = adapter.getItem(position);
		Intent data = new Intent();
		data.putExtra(EXTRA_DEVICE_ID, device.getId());
		data.putExtra(EXTRA_DEVICE_TYPE, device.getDeviceType());
		setResult(RESULT_OK, data);
		finish();
	}
}
