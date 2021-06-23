package ch.admin.bag.dp3t.inform.models

enum class Status {
	SUCCESS,
	ERROR,
	LOADING
}

data class Resource<out T>(val status: Status, val data: T?, val exception: Throwable?) {
	companion object {
		fun <T> success(data: T): Resource<T> = Resource(status = Status.SUCCESS, data = data, exception = null)

		fun <T> error(data: T?, exception: Throwable): Resource<T> =
			Resource(status = Status.ERROR, data = data, exception = exception)

		fun <T> loading(data: T?): Resource<T> = Resource(status = Status.LOADING, data = data, exception = null)
	}
}
