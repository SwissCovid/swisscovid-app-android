/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;

import org.dpppt.android.sdk.backend.SignatureException;

import ch.admin.bag.dp3t.networking.StatsRepository;
import ch.admin.bag.dp3t.networking.errors.ResponseError;
import ch.admin.bag.dp3t.networking.models.StatsResponseModel;

public class StatsViewModel extends AndroidViewModel {

	private StatsRepository statsRepository;

	private final MutableLiveData<StatsResponseModel> statsLiveData = new MutableLiveData<>();

	public StatsViewModel(@NonNull Application application) {
		super(application);

		statsRepository = new StatsRepository(application);
	}

	public LiveData<StatsResponseModel> getStatsLiveData() {
		return statsLiveData;
	}

	public void loadStats() {
		new Thread(() -> {
			try {
				StatsResponseModel model = statsRepository.getStats();
				statsLiveData.postValue(model);
			} catch (IOException | ResponseError | SignatureException e) {
				statsLiveData.postValue(null);
			}
		}).start();
	}

}
