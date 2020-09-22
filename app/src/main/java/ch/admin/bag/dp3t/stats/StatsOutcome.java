package ch.admin.bag.dp3t.stats;

import androidx.annotation.NonNull;

import ch.admin.bag.dp3t.networking.models.StatsResponseModel;
import ch.admin.bag.dp3t.util.Outcome;

public class StatsOutcome {

	private @NonNull Outcome outcome;
	private StatsResponseModel statsResponseModel;

	public StatsOutcome() {
		this.outcome = Outcome.LOADING;
	}

	public @NonNull Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(@NonNull Outcome outcome) {
		this.outcome = outcome;
	}

	public StatsResponseModel getStatsResponseModel() {
		return statsResponseModel;
	}

	public void setStatsResponseModel(StatsResponseModel statsResponseModel) {
		this.statsResponseModel = statsResponseModel;
	}

}