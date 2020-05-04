/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package ch.admin.bag.dp3t.util;

import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ch.admin.bag.dp3t.main.model.NotificationStateError;
import ch.admin.bag.dp3t.R;

public class NotificatonErrorStateHelper {

	public static void updateNotificationErrorView(View reportErrorView, NotificationStateError notificationStateError) {
		if (notificationStateError == null) {
			reportErrorView.setVisibility(View.GONE);
			return;
		}
		reportErrorView.setVisibility(View.VISIBLE);
		ImageView iconView = reportErrorView.findViewById(R.id.error_status_image);
		TextView titleView = reportErrorView.findViewById(R.id.error_status_title);
		TextView textView = reportErrorView.findViewById(R.id.error_status_text);
		TextView errorCode = reportErrorView.findViewById(R.id.error_status_code);
		errorCode.setVisibility(View.GONE);
		TextView buttonView = reportErrorView.findViewById(R.id.error_status_button);

		iconView.setImageResource(NotificationStateError.getIcon(notificationStateError));
		iconView.setVisibility(View.VISIBLE);

		titleView.setText(NotificationStateError.getTitle(notificationStateError));
		titleView.setVisibility(View.VISIBLE);

		if (NotificationStateError.getText(notificationStateError) != -1) {
			textView.setText(NotificationStateError.getText(notificationStateError));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}

		if (NotificationStateError.getButtonText(notificationStateError) != -1) {
			buttonView.setText(NotificationStateError.getButtonText(notificationStateError));
			buttonView.setVisibility(View.VISIBLE);
			buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		} else {
			buttonView.setVisibility(View.GONE);
		}
	}

}
