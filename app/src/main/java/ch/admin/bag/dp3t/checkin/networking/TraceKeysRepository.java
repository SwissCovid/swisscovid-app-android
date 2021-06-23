package ch.admin.bag.dp3t.checkin.networking;

import android.content.Context;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.crowdnotifier.android.sdk.model.DayDate;
import org.crowdnotifier.android.sdk.model.ProblematicEventInfo;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.backend.SignatureVerificationInterceptor;
import org.dpppt.android.sdk.backend.UserAgentInterceptor;
import org.dpppt.android.sdk.util.SignatureUtil;
import org.jetbrains.annotations.NotNull;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.checkin.models.ProblematicEvent;
import ch.admin.bag.dp3t.checkin.models.ProblematicEventWrapper;
import ch.admin.bag.dp3t.storage.SecureStorage;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TraceKeysRepository {

	private static final String KEY_BUNDLE_TAG_HEADER = "x-key-bundle-tag";

	private TraceKeysService traceKeysService;
	private SecureStorage storage;

	public TraceKeysRepository(Context context) {

		storage = SecureStorage.getInstance(context);
		String baseUrl = BuildConfig.PUBLISHED_CROWDNOTIFIER_KEYS_BASE_URL;

		OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
		okHttpBuilder.networkInterceptors().add(new UserAgentInterceptor(DP3T.getUserAgent()));

		PublicKey signaturePublicKey = SignatureUtil.getPublicKeyFromBase64OrThrow(BuildConfig.BUCKET_PUBLIC_KEY);
		okHttpBuilder.addInterceptor(new SignatureVerificationInterceptor(signaturePublicKey));


		Retrofit bucketRetrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.client(okHttpBuilder.build())
				.build();

		traceKeysService = bucketRetrofit.create(TraceKeysService.class);
	}

	public void loadTraceKeysAsync(Callback callback) {
		traceKeysService.getTraceKeys(storage.getCrowdNotifierLastKeyBundleTag()).enqueue(new retrofit2.Callback<ResponseBody>() {
			@Override
			public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					callback.onTraceKeysLoaded(handleSuccessfulResponse(response));
				} else {
					callback.onTraceKeysLoaded(null);
				}
			}

			@Override
			public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
				callback.onTraceKeysLoaded(null);
			}
		});
	}

	public List<ProblematicEventInfo> loadTraceKeys() {
		try {
			Response<ResponseBody> response = traceKeysService.getTraceKeys(storage.getCrowdNotifierLastKeyBundleTag()).execute();
			if (response.isSuccessful()) {
				return handleSuccessfulResponse(response);
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	private List<ProblematicEventInfo> handleSuccessfulResponse(Response<ResponseBody> response) {
		try {
			String keyBundleTag = response.headers().get(KEY_BUNDLE_TAG_HEADER);
			if (keyBundleTag != null) {
				long keyBundleTagValue = Long.parseLong(keyBundleTag);
				storage.setCrowdNotifierLastKeyBundleTag(keyBundleTagValue);
			}
			ProblematicEventWrapper problematicEventWrapper = ProblematicEventWrapper.parseFrom(response.body().byteStream());
			ArrayList<ProblematicEventInfo> problematicEventInfos = new ArrayList<>();
			for (ProblematicEvent event : problematicEventWrapper.getEventsList()) {
				problematicEventInfos.add(new ProblematicEventInfo(event.getIdentity().toByteArray(),
						event.getSecretKeyForIdentity().toByteArray(),
						event.getEncryptedAssociatedData().toByteArray(), event.getCipherTextNonce().toByteArray(),
						new DayDate(event.getDay() * 1000L))
				);
			}
			return problematicEventInfos;
		} catch (IOException e) {
			return null;
		}
	}

	public interface Callback {
		void onTraceKeysLoaded(List<ProblematicEventInfo> traceKeys);

	}

}
