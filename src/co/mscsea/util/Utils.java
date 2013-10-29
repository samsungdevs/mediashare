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

package co.mscsea.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils {
	
	public static class UriInfo {
		public String path;
		public String mimeType;
		public String title;
	}
	
	public static String[] uriInfoProjection = new String[] {
		MediaStore.MediaColumns.DATA,
		MediaStore.MediaColumns.MIME_TYPE,
		MediaStore.MediaColumns.TITLE
	};
	
	public static void openMedia(Activity activity, Uri uri, String mimeType) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, mimeType);
		activity.startActivity(intent);
	}
	
	public static void openMedia(Activity activity, String uri, String mimeType) {
		if (uri.indexOf("://") < 0) {
			uri = "file://" + uri;
		}
		
		openMedia(activity, Uri.parse(uri), mimeType);
	}
	
	public static UriInfo getUriInfo(ContentResolver cr, Uri uri) {
		UriInfo info = new UriInfo();
		
		Cursor cursor = cr.query(uri, uriInfoProjection, null, null, null);
		while(cursor.moveToNext()) {
			info.path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)); 
			info.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
			info.title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
		}
		cursor.close();
		
		return info;
	}
}
