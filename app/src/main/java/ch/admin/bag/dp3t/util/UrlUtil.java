package ch.admin.bag.dp3t.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class UrlUtil {

	public static void openUrl(Context context, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		if (intent.resolveActivity(context.getPackageManager()) != null) {
			context.startActivity(intent);
		}
	}

}
