package org.dpppt.android.app.inform.networking;

import org.dpppt.android.app.inform.model.AuthenticationCodeRequestModel;
import org.dpppt.android.app.inform.model.AuthenticationCodeResponseModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthCodeService {

	@Headers({
					 "accept: */*",
					 "content-type: application/json"
			 })
	@POST("v1/onset")
	Call<AuthenticationCodeResponseModel> getAccessToken(@Body AuthenticationCodeRequestModel code);

}
