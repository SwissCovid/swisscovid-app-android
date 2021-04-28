package ch.admin.bag.dp3t.checkin.networking;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface TraceKeysService {

	@Headers("Accept: application/x-protobuf")
	@GET("v3/traceKeys")
	Call<ResponseBody> getTraceKeys(@Query("lastKeyBundleTag") long lastKeyBundleTag);

}
