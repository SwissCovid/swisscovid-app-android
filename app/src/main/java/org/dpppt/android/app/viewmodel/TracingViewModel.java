/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.viewmodel;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.dpppt.android.app.debug.TracingStatusWrapper;
import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.app.main.model.TracingStatusInterface;
import org.dpppt.android.app.networking.ConfigWorker;
import org.dpppt.android.app.networking.errors.ResponseError;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.app.util.DeviceFeatureHelper;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;

public class TracingViewModel extends AndroidViewModel {

	private final MutableLiveData<TracingStatus> tracingStatusLiveData = new MutableLiveData<>();
	private BroadcastReceiver tracingStatusBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			invalidateTracingStatus();
		}
	};

	private final MutableLiveData<Boolean> tracingEnabledLiveData = new MutableLiveData<>();
	private final MutableLiveData<Pair<Boolean, Boolean>> exposedLiveData = new MutableLiveData<>();
	private final MutableLiveData<Integer> numberOfHandshakesLiveData = new MutableLiveData<>(0);
	private final MutableLiveData<Collection<TracingStatus.ErrorState>> errorsLiveData =
			new MutableLiveData<>(Collections.emptyList());
	private final MutableLiveData<TracingStatusInterface> appStatusLiveData = new MutableLiveData<>();

	private TracingStatusWrapper tracingStatusWrapper = new TracingStatusWrapper(DebugAppState.NONE);

	private final MutableLiveData<Boolean> bluetoothEnabledLiveData = new MutableLiveData<>();
	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
				invalidateBluetoothState();
				invalidateTracingStatus();
			}
		}
	};

	private final MutableLiveData<Boolean> forceUpdateLiveData = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasInfoboxLiveData = new MutableLiveData<>(false);
	private BroadcastReceiver configUpdateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateConfigStatus();
		}
	};

	public TracingViewModel(@NonNull Application application) {
		super(application);

		tracingStatusLiveData.observeForever(status -> {
			errorsLiveData.setValue(status.getErrors());
			tracingEnabledLiveData.setValue(status.isAdvertising() && status.isReceiving());
			numberOfHandshakesLiveData.setValue(status.getNumberOfContacts());
			tracingStatusWrapper.setStatus(status);

			exposedLiveData
					.setValue(new Pair<>(tracingStatusWrapper.isReportedAsInfected(),
							tracingStatusWrapper.wasContactReportedAsExposed()));

			appStatusLiveData.setValue(tracingStatusWrapper);
		});

		invalidateBluetoothState();
		invalidateTracingStatus();
		invalidateConfigStatus();

		application.registerReceiver(tracingStatusBroadcastReceiver, DP3T.getUpdateIntentFilter());
		application.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		application.registerReceiver(configUpdateBroadcastReceiver, new IntentFilter(ConfigWorker.ACTION_CONFIG_UPDATE));
	}

	public void resetSdk(Runnable onDeleteListener) {
		if (tracingEnabledLiveData.getValue()) DP3T.stop(getApplication());
		tracingStatusWrapper.setDebugAppState(DebugAppState.NONE);
		DP3T.clearData(getApplication(), onDeleteListener);
	}

	public void invalidateTracingStatus() {
		TracingStatus status = DP3T.getStatus(getApplication());
		tracingStatusLiveData.setValue(status);
	}

	public LiveData<Boolean> getTracingEnabledLiveData() {
		return tracingEnabledLiveData;
	}

	public LiveData<Pair<Boolean, Boolean>> getSelfOrContactExposedLiveData() {
		return exposedLiveData;
	}

	public LiveData<Collection<TracingStatus.ErrorState>> getErrorsLiveData() {
		return errorsLiveData;
	}

	public LiveData<TracingStatusInterface> getAppStatusLiveData() {
		return appStatusLiveData;
	}

	public LiveData<TracingStatus> getTracingStatusLiveData() {
		return tracingStatusLiveData;
	}

	public LiveData<Boolean> getBluetoothEnabledLiveData() {
		return bluetoothEnabledLiveData;
	}

	public void setTracingEnabled(boolean enabled) {
		if (enabled) {
			DP3T.start(getApplication());
		} else {
			DP3T.stop(getApplication());
		}
	}

	public void sync() {
		DP3T.sync(getApplication());
	}

	public void invalidateService() {
		if (tracingEnabledLiveData.getValue()) {
			DP3T.start(getApplication());
		}
	}

	private void invalidateBluetoothState() {
		bluetoothEnabledLiveData.setValue(DeviceFeatureHelper.isBluetoothEnabled());
	}

	@Override
	protected void onCleared() {
		getApplication().unregisterReceiver(tracingStatusBroadcastReceiver);
		getApplication().unregisterReceiver(bluetoothReceiver);
		getApplication().unregisterReceiver(configUpdateBroadcastReceiver);
	}

	public DebugAppState getDebugAppState() {
		return tracingStatusWrapper.getDebugAppState();
	}

	public void setDebugAppState(DebugAppState debugAppState) {
		tracingStatusWrapper.setDebugAppState(debugAppState);
	}

	private void invalidateConfigStatus() {
		new Thread(() -> {
			try {
				ConfigWorker.loadConfig(getApplication());
			} catch (IOException | ResponseError e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void updateConfigStatus() {
		SecureStorage secureStorage = SecureStorage.getInstance(getApplication());
		boolean forceUpdate = secureStorage.getDoForceUpdate();
		forceUpdateLiveData.postValue(forceUpdate);

		boolean hasInfobox = secureStorage.getHasInfobox();
		hasInfoboxLiveData.postValue(hasInfobox);
	}

	public LiveData<Boolean> getForceUpdateLiveData() {
		return forceUpdateLiveData;
	}

	public LiveData<Boolean> getHasInfoboxLiveData() {
		return hasInfoboxLiveData;
	}

}
