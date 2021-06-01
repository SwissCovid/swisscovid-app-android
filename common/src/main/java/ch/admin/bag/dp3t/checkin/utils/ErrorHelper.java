package ch.admin.bag.dp3t.checkin.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState;

public class ErrorHelper {

	public static void updateErrorView(View errorView, CrowdNotifierErrorState errorState, Runnable customButtonClickAction,
			Context context) {
		updateErrorView(errorView, errorState, customButtonClickAction, context, true);
	}

	public static void updateErrorView(View errorView, CrowdNotifierErrorState errorState, Runnable customButtonClickAction,
			Context context, boolean showButton) {
		((TextView) errorView.findViewById(R.id.error_status_title)).setText(errorState.getTitleResId());
		((TextView) errorView.findViewById(R.id.error_status_text)).setText(errorState.getTextResId());
		((ImageView) errorView.findViewById(R.id.error_status_image))
				.setImageDrawable(ContextCompat.getDrawable(errorView.getContext(), errorState.getImageResId()));

		TextView buttonView = errorView.findViewById(R.id.error_status_button);
		if (showButton) {
			buttonView.setVisibility(View.VISIBLE);
			buttonView.setText(errorState.getActionResId());
			buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			buttonView.setOnClickListener(v -> executeErrorAction(errorState, customButtonClickAction, context));
		} else {
			buttonView.setVisibility(View.GONE);
		}
	}

	private static void executeErrorAction(CrowdNotifierErrorState errorState, Runnable customButtonClickAction, Context context) {
		if (customButtonClickAction != null) customButtonClickAction.run();
		switch (errorState) {
			case CAMERA_ACCESS_DENIED:
				openApplicationSettings(context);
				break;
			case UPDATE_REQUIRED:
				updateApp(context);
				break;
		}
	}

	private static void updateApp(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + context.getPackageName()));
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, "No browser installed", Toast.LENGTH_LONG).show();
		}
	}

	private static void openApplicationSettings(Context context) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", context.getPackageName(), null);
		intent.setData(uri);
		context.startActivity(intent);
	}

}
