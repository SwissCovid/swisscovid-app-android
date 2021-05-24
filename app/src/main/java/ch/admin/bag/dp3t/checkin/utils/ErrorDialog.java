package ch.admin.bag.dp3t.checkin.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState;

public class ErrorDialog extends AlertDialog {

	CrowdNotifierErrorState errorState;

	public ErrorDialog(@NonNull Context context, CrowdNotifierErrorState errorState) {
		super(context);
		this.errorState = errorState;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_error);
		findViewById(R.id.dialog_error_container).setBackgroundResource(R.color.white);
		View closeButton = findViewById(R.id.dialog_error_close_button);

		closeButton.setOnClickListener(v -> dismiss());
		ErrorHelper.updateErrorView(findViewById(R.id.dialog_error_container), errorState, this::dismiss, getContext());

		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
	}

}
