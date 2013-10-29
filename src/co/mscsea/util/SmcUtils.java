package co.mscsea.util;

import android.content.ContentResolver;
import android.content.Intent;
import android.webkit.MimeTypeMap;
import co.mscsea.util.Utils.UriInfo;

import com.samsung.android.sdk.mediacontrol.SmcDevice;
import com.samsung.android.sdk.mediacontrol.SmcItem;

public class SmcUtils {
	
	public static SmcItem.LocalContent getLocalContent(ContentResolver cr, Intent intent) {
		SmcItem.LocalContent content = null;
		
		if (intent.getData().getScheme().equals("content")) {
			UriInfo info = Utils.getUriInfo(cr, intent.getData());
			content = new SmcItem.LocalContent(info.path, info.mimeType).setTitle(info.title);
		} else {
			String path = intent.getDataString();
			if (path != null) {
				String name = path.substring(path.lastIndexOf('/') + 1);
				String extension = name.substring(name.lastIndexOf('.') + 1);
				String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
				content = new SmcItem.LocalContent(path, mimeType).setTitle(name);
			}
		}
		
		return content;
	}
	
	public static int getDeviceType(int mediaType) {
		switch (mediaType) {
			case SmcItem.MEDIA_TYPE_ITEM_IMAGE: {
				return SmcDevice.TYPE_IMAGEVIEWER;
			}
			case SmcItem.MEDIA_TYPE_ITEM_AUDIO:
			case SmcItem.MEDIA_TYPE_ITEM_VIDEO: {
				return SmcDevice.TYPE_AVPLAYER;
			}
		}
		
		return SmcDevice.TYPE_UNKNOWN;
	}
}
