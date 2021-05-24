/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.contacts;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.dpppt.android.sdk.internal.history.HistoryEntry;
import org.dpppt.android.sdk.internal.history.HistoryEntryType;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.DateUtils;

public class HistoryViewHolder extends RecyclerView.ViewHolder {

	public HistoryViewHolder(@NonNull View itemView) {
		super(itemView);
	}

	public void bind(HistoryEntry item, int position) {
		itemView.findViewById(R.id.item_history_content_group).setBackgroundColor(
				ContextCompat.getColor(itemView.getContext(), position % 2 == 0 ? R.color.white : R.color.grey_light));
		StringBuilder label = new StringBuilder(itemView.getContext().getString(getLabelForType(item.getType())));
		if (item.getStatus() != null) label.append(" ").append(item.getStatus());
		((TextView) itemView.findViewById(R.id.item_history_label)).setText(label.toString());
		((TextView) itemView.findViewById(R.id.item_history_date))
				.setText(DateUtils.getFormattedDateTime(item.getTime()));
	}

	@StringRes
	private static int getLabelForType(HistoryEntryType type) {
		switch (type) {
			case OPEN_APP:
				return R.string.synchronizations_view_sync_via_open;
			case SYNC:
				return R.string.synchronizations_view_sync_via_background;
			case WORKER_STARTED:
				return R.string.synchronizations_view_sync_via_scheduled;
			case FAKE_REQUEST:
				return R.string.synchronizations_view_sync_via_fake_request;
			case NEXT_DAY_KEY_UPLOAD_REQUEST:
				return R.string.synchronizations_view_sync_via_next_day_key_upload;
			case NOTIFICATION:
				return R.string.synchronizations_view_notification;
			default:
				throw new IllegalArgumentException("Unknown HistoryEntryType: " + type);
		}
	}

}
