/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.stats;

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
import ch.admin.bag.dp3t.util.Outcome;

public class StatsViewModel extends AndroidViewModel {

	private StatsRepository statsRepository;

	private final MutableLiveData<StatsOutcome> statsLiveData = new MutableLiveData<>(new StatsOutcome());

	public StatsViewModel(@NonNull Application application) {
		super(application);

		statsRepository = new StatsRepository(application);
	}

	public LiveData<StatsOutcome> getStatsLiveData() {
		return statsLiveData;
	}

	public void loadStats() {
		new Thread(() -> {
			StatsOutcome outcome = new StatsOutcome();
			try {
				outcome.setOutcome(Outcome.LOADING);
				statsLiveData.postValue(outcome);

				StatsResponseModel model = statsRepository.getStats();
				if (model.getLastUpdatedRaw() == null && model.getHistory().isEmpty()) {
					outcome.setOutcome(Outcome.ERROR);
				} else {
					outcome.setStatsResponseModel(model);
					outcome.setOutcome(Outcome.RESULT);
				}
				statsLiveData.postValue(outcome);
			} catch (IOException | ResponseError e) {
				outcome.setOutcome(Outcome.ERROR);
				statsLiveData.postValue(outcome);
			}
		}).start();
	}

}
