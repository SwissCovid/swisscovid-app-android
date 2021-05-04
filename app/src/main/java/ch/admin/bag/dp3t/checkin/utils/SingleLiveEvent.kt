package ch.admin.bag.dp3t.checkin.utils

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SingleLiveEvent"

/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 *
 *
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 *
 *
 * Note that only one observer is going to be notified of changes.
 *
 * Inspired by https://github.com/android/architecture-samples/blob/dev-todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {

	// flag to avoid calling Observer's `onChanged()` when it's registered
	private val mPending = AtomicBoolean(false)

	@MainThread
	override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
		if (hasActiveObservers()) {
			Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
		}

		// Observe the internal MutableLiveData
		super.observe(owner, { t ->
			if (mPending.compareAndSet(true, false)) {
				observer.onChanged(t)
			}
		})
	}

	@MainThread
	override fun setValue(t: T) {
		mPending.set(true)
		super.setValue(t)
	}
}

/**
 * Used for cases where T is Unit, to make calls cleaner.
 */
@MainThread
fun SingleLiveEvent<Unit?>.call() {
	setValue(null)
}
