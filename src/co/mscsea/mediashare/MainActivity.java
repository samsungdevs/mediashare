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
import java.util.Stack;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.mscsea.util.SmcUtils;
import co.mscsea.util.Utils;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.mediacontrol.Smc;
import com.samsung.android.sdk.mediacontrol.SmcAvPlayer;
import com.samsung.android.sdk.mediacontrol.SmcAvPlayer.PlayInfo;
import com.samsung.android.sdk.mediacontrol.SmcDevice;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder.StatusListener;
import com.samsung.android.sdk.mediacontrol.SmcImageViewer;
import com.samsung.android.sdk.mediacontrol.SmcItem;
import com.samsung.android.sdk.mediacontrol.SmcItem.LocalContent;
import com.samsung.android.sdk.mediacontrol.SmcProvider;
import com.samsung.android.sdk.mediacontrol.SmcProvider.EventListener;
import com.samsung.android.sdk.mediacontrol.SmcProvider.ResponseListener;
import com.samsung.android.sdk.mediacontrol.SmcProvider.SearchCriteria;

public class MainActivity extends ListActivity implements OnItemClickListener, OnClickListener {

	private static final int NUM_OF_ITEMS = 100;
	private static final String IMAGE_TYPE = "image/*";
	private static final String AUDIO_TYPE = "audio/*";
	private static final String VIDEO_TYPE = "video/*";
	private static final String ALL_TYPES = "*/*";
	private static final int VIDEO_POSITION = 0;
	private static final int AUDIO_POSITION = 1;
	@SuppressWarnings("unused")
	private static final int IMAGE_POSITION = 2;
	
	private PhoneAdapter phoneAdapter;
	
	private ItemAdapter itemAdapter;

	private Smc smc;
	private SmcDeviceFinder deviceFinder;
	private SmcProvider source = null;
	private SmcDevice target = null;
	
	private SmcItem selectedItem;
	private Stack<SmcItem> navigationHistory = new Stack<SmcItem>();
	private SmcItem currentFolder;
	
	private RelativeLayout sourceLayout;
	private TextView sourceName;
	private TextView sourceIp;
	private ImageView upload;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/**
		 * TODO:
		 * 1. Create Smc object
		 * 2. Initialize and handle exception
		 */
		//<task>
		smc = new Smc();
		try {
			smc.initialize(getBaseContext());
		} catch (SsdkUnsupportedException e) {
			Toast.makeText(this, getString(R.string.sdk_not_supported), Toast.LENGTH_LONG).show();
			e.printStackTrace();
			finish();
			return;
		}
		//</task>
		
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
		
		sourceLayout = (RelativeLayout) findViewById(R.id.source_layout);
		sourceLayout.setOnClickListener(this);
		
		sourceName = (TextView) findViewById(R.id.source_name);
		
		sourceIp = (TextView) findViewById(R.id.source_ip);
		
		upload = (ImageView) findViewById(R.id.upload);
		upload.setOnClickListener(this);
		
		itemAdapter = new ItemAdapter(this);
		
		phoneAdapter = new PhoneAdapter(this, getResources().getStringArray(R.array.phone_items));
		
		getListView().setOnItemClickListener(this);
		setListAdapter(phoneAdapter);
		
		refreshSource(null);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		/**
		 * TODO: Stop device finder
		 */
		//<task>
		if (deviceFinder != null) {
			deviceFinder.stop();
			deviceFinder = null;
		}
		//</task>
	}
	
	private StatusListener statusListener = new StatusListener() {
		
		@Override
		public void onStopped(SmcDeviceFinder deviceFinder) {
			if (MainActivity.this.deviceFinder == deviceFinder) {
				deviceFinder.setStatusListener(null);
				MainActivity.this.deviceFinder = null;
			}
		}
		
		@Override
		public void onStarted(SmcDeviceFinder deviceFinder, int error) {
			if (error == Smc.SUCCESS) {
				MainActivity.this.deviceFinder = deviceFinder;
			}
		}
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.source_layout: {
				Intent intent = new Intent(this, DeviceListActivity.class);
				intent.putExtra(DeviceListActivity.EXTRA_DEVICE_TYPE, SmcDevice.TYPE_PROVIDER);
				startActivityForResult(intent, R.id.request_source_device);
				break;
			}
			case R.id.upload: {
				Intent intent = new Intent(Intent.ACTION_PICK).setType(ALL_TYPES);
	            startActivityForResult(intent, R.id.request_file_for_upload);
				break;
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		ListAdapter adapter = getListAdapter();
		if (adapter == phoneAdapter) {
			super.onBackPressed();
		} else if (adapter == itemAdapter) {
			if (navigationHistory.size() == 0) {
				super.onBackPressed();
				return;
			}
			
			browseUp(source);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListAdapter adapter = getListAdapter();
		if (adapter == phoneAdapter) {
			String action = Intent.ACTION_PICK;
			String type = IMAGE_TYPE;
			int requestCode = R.id.request_image;
			
			switch (position) {
				case VIDEO_POSITION: {
					action = Intent.ACTION_PICK;
					type = VIDEO_TYPE;
					requestCode = R.id.request_video;
					break;
				}
				case AUDIO_POSITION: {
					action = Intent.ACTION_GET_CONTENT;
					type = AUDIO_TYPE;
					requestCode = R.id.request_audio;
					break;
				}
			}
			
			Intent intent = new Intent(action);
			intent.setType(type);
			startActivityForResult(intent, requestCode);
		} else if (adapter == itemAdapter) {
			/*
			 * Browse folder or open the file
			 */
			SmcItem item = ((ItemAdapter) adapter).getItem(position);
			if (item.getMediaType() == SmcItem.MEDIA_TYPE_ITEM_FOLDER) {
				browse(source, item);
			} else {
				int deviceType = SmcUtils.getDeviceType(item.getMediaType());
				openOnTargetDevice(deviceType, item);
			}
		}
	}
	
	private void handleImageSelected(Intent intent) {
		LocalContent content = SmcUtils.getLocalContent(getContentResolver(), intent);
		SmcItem item = new SmcItem(content);
		openOnTargetDevice(SmcDevice.TYPE_IMAGEVIEWER, item);
	}
	
	private void handleAudioOrVideoSelected(Intent intent) {
		LocalContent content = SmcUtils.getLocalContent(getContentResolver(), intent);
		SmcItem item = new SmcItem(content);
		openOnTargetDevice(SmcDevice.TYPE_AVPLAYER, item);
	}
	
	private void openOnTargetDevice(int deviceType, SmcItem item) {
		selectedItem = item;
		
		Intent intent = new Intent(this, DeviceListActivity.class);
		intent.putExtra(DeviceListActivity.EXTRA_DEVICE_TYPE, deviceType);
		startActivityForResult(intent, R.id.request_target_device);
	}
	
	private void handleTargetDeviceSelected(Intent data) {
		String id = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ID);
		if (TextUtils.isEmpty(id)) {
			Utils.openMedia(this, selectedItem.getUri().toString(), selectedItem.getMimeType());
		} else {
			int type = data.getIntExtra(DeviceListActivity.EXTRA_DEVICE_TYPE, SmcDevice.TYPE_UNKNOWN);
			target = deviceFinder.getDevice(type, id);
			if (target != null) {
				switch (type) {
					case SmcDevice.TYPE_IMAGEVIEWER: {
						/**
						 * TODO: Show selected item
						 */
						//<task>
						((SmcImageViewer) target).show(selectedItem);
						//</task>
						break;
					}
					case SmcDevice.TYPE_AVPLAYER: {
						/**
						 * TODO: Play selected item
						 */
						//<task>
						((SmcAvPlayer) target).play(selectedItem, new PlayInfo(0));
						//</task>
						break;
					}
				}
			}
		}
	}
	
	private void refreshSource(SmcProvider provider) {
		if (provider == null) {
			sourceName.setText(getString(R.string.this_device));
			sourceIp.setText(getString(R.string.android_device));
			upload.setVisibility(View.GONE);
			setListAdapter(phoneAdapter);
		} else {
			sourceName.setText(provider.getName());
			sourceIp.setText(provider.getIpAddress());
			
			navigationHistory.clear();
			currentFolder = null;
			
			itemAdapter.clear();
			setListAdapter(itemAdapter);
			
			if (provider.isUploadable()) {
				upload.setVisibility(View.VISIBLE);
			} else {
				upload.setVisibility(View.GONE);
			}
			provider.setEventListener(sourceEventListener);
			provider.setResponseListener(sourceResponseListener);
			browse(provider, provider.getRootFolder());
		}
	}
	
	private void handleSourceDeviceSelected(Intent data) {
		String id = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ID);
		if (TextUtils.isEmpty(id)) {
			source = null;
		} else {
			int type = data.getIntExtra(DeviceListActivity.EXTRA_DEVICE_TYPE, SmcDevice.TYPE_UNKNOWN);
			SmcDevice device = deviceFinder.getDevice(type, id);
			if (type == SmcDevice.TYPE_PROVIDER) {
				source = (SmcProvider) device;
			}
		}
		
		refreshSource(source);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode != RESULT_OK) return;
		
		switch (requestCode) {
			case R.id.request_target_device: {
				handleTargetDeviceSelected(data);
				break;
			}
			case R.id.request_image: {
				handleImageSelected(data);
				break;
			}
			case R.id.request_audio:
			case R.id.request_video: {
				handleAudioOrVideoSelected(data);
				break;
			}
			case R.id.request_source_device: {
				handleSourceDeviceSelected(data);
				break;
			}
			case R.id.request_file_for_upload: {
				handleFileForUpload(data);
				break;
			}
		}
	}

	private void browse(SmcProvider provider, SmcItem item) {
		browse(provider, item, true);
	}
	
	private void browse(SmcProvider provider, SmcItem item, boolean addToHistory) {
		if (item.getMediaType() != SmcItem.MEDIA_TYPE_ITEM_FOLDER) return;
		
		if (addToHistory && currentFolder != null) {
			navigationHistory.push(currentFolder);
		}
		
		currentFolder = item;
		/**
		 * TODO: browse items from the provider
		 */
		//<task>
		provider.browse(currentFolder, 0, NUM_OF_ITEMS);
		//</task>
	}
	
	private void browseUp(SmcProvider provider) {
		if (navigationHistory.size() == 0) return;
		
		SmcItem item = navigationHistory.pop();
		browse(provider, item, false);
	}
	
	private ResponseListener sourceResponseListener = new ResponseListener() {
		
		@Override
		public void onUploadCancel(SmcProvider smcProvider, SmcItem smcItem, int error) {

		}
		
		@Override
		public void onUpload(SmcProvider smcProvider, SmcItem smcItem, int error) {
			if (error != Smc.SUCCESS) {
				dismissProgressDialog();
				Toast.makeText(MainActivity.this, String.format(getString(R.string.upload_error), error), Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		public void onSearch(SmcProvider smcProvider, List<SmcItem> smcItems, int requestedStartIndex,
				int requestedCount, SearchCriteria searchCriteria, boolean endOfItems, int error) {
			
		}
		
		@Override
		public void onBrowse(SmcProvider smcProvider, List<SmcItem> smcItems, int requestedStartIndex,
				int requestedCount, SmcItem requestedFolderItem, boolean endOfItems, int error) {
			if (error == Smc.SUCCESS) {
				itemAdapter.setItems(smcItems);
			}
		}
	};
	
	private EventListener sourceEventListener = new EventListener() {
		
		@Override
		public void onUploadProgressUpdated(SmcProvider smcProvider, long receivedSize, long totalSize,
				SmcItem smcItem, int error) {
			updateProgressDialog((int)receivedSize, (int)totalSize);
		}
		
		@Override
		public void onUploadCompleted(SmcProvider smcProvider, SmcItem smcItem) {
			dismissProgressDialog();
			Toast.makeText(MainActivity.this, getString(R.string.upload_complete), Toast.LENGTH_LONG).show();
		}
		
		@Override
		public void onContentUpdated(SmcProvider smcProvider, int error) {
			
		}
	};
	
	private void handleFileForUpload(Intent data) {
		LocalContent content = SmcUtils.getLocalContent(getContentResolver(), data);
		selectedItem = new SmcItem(content);
		
		showProgressDialog(String.format(getString(R.string.uploading), selectedItem.getTitle()));
		
		/**
		 * Upload selected item to source device
		 */
		//<task>
		source.upload(selectedItem);
		//</task>
	}
	
	private android.content.DialogInterface.OnClickListener onProgressDialogCanceled = new android.content.DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (source != null) {
				Toast.makeText(MainActivity.this, getString(R.string.upload_canceled), Toast.LENGTH_LONG).show();
				/**
				 * Cancel uploading of the selected item
				 */
				//<task>
				source.uploadCancel(selectedItem);
				//</task>
				selectedItem = null;
			}
		}
	};
	
	private void showProgressDialog(String message) {
		dismissProgressDialog();
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), onProgressDialogCanceled);
		
		progressDialog.setTitle(R.string.app_name);
		progressDialog.setMessage(message);
		progressDialog.setIndeterminate(false);
		progressDialog.setProgress(0);
		progressDialog.setMax(0);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.show();
	}
	
	private void dismissProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	private void updateProgressDialog(int progress, int total) {
		if (progressDialog == null) return;
		
		progressDialog.setProgress(progress);
		progressDialog.setMax(total);
	}
}
