package ch.admin.bag.dp3t.hibernate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ch.admin.bag.dp3t.MainApplication
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.networking.CrowdNotifierKeyLoadWorker
import ch.admin.bag.dp3t.networking.ConfigWorker
import ch.admin.bag.dp3t.networking.FakeWorker
import ch.admin.bag.dp3t.networking.models.InfoBoxModel
import ch.admin.bag.dp3t.networking.models.InfoBoxModelCollection
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.NotificationRepeatWorker
import kotlinx.coroutines.launch
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.internal.logger.Logger

class HibernatingViewModel(application: Application) : AndroidViewModel(application) {

	companion object {
		private val TAG = "HibernatingViewModel"

	}

	private val isHibernatingModeEnabledMutable = MutableLiveData<Boolean>()
	val isHibernatingModeEnabled: LiveData<Boolean> = isHibernatingModeEnabledMutable

	private val secureStorage: SecureStorage by lazy { SecureStorage.getInstance(application) }

	private val hibernatingInfoboxMutable = MutableLiveData<InfoBoxModelCollection?>(secureStorage.hibernatingInfoboxCollection)
	val hibernatingInfoBox: LiveData<InfoBoxModelCollection?> = hibernatingInfoboxMutable

	init {
		loadConfig()
	}


	private fun loadConfig() {
		viewModelScope.launch {
			try {
				ConfigWorker.loadConfig(getApplication())
				if (secureStorage.isHibernating) {
					isHibernatingModeEnabledMutable.value = true
					hibernatingInfoboxMutable.value = secureStorage.hibernatingInfoboxCollection
				} else {
					MainApplication.initDP3T(getApplication())
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