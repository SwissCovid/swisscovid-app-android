package ch.admin.bag.dp3t.hibernate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ch.admin.bag.dp3t.checkin.networking.CrowdNotifierKeyLoadWorker
import ch.admin.bag.dp3t.networking.ConfigWorker
import ch.admin.bag.dp3t.networking.FakeWorker
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.NotificationRepeatWorker
import kotlinx.coroutines.launch
import org.dpppt.android.sdk.internal.logger.Logger

class HibernatingViewModel(application: Application) : AndroidViewModel(application) {

	companion object {
		private val TAG = "HibernatingViewModel"

	}

	private val isHibernatingModeEnabledMutable = MutableLiveData(true)
	val isHibernatingModeEnabled: LiveData<Boolean> = isHibernatingModeEnabledMutable

	init {
		loadConfig()
	}


	private fun loadConfig() {
		viewModelScope.launch {
			try {
				ConfigWorker.loadConfig(getApplication())
				if (!SecureStorage.getInstance(getApplication()).isHibernating) {
					FakeWorker.safeStartFakeWorker(getApplication())
					CrowdNotifierKeyLoadWorker.startKeyLoadWorker(getApplication())
					NotificationRepeatWorker.startWorker(getApplication())
					ConfigWorker.scheduleConfigWorkerIfOutdated(getApplication())
					isHibernatingModeEnabledMutable.value = false
				}
			} catch (e: Exception) {
				Logger.e(TAG, "config request failed", e)
			}
		}
	}
}