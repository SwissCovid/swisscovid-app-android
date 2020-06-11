package ch.admin.bag.dp3t.reports;

public class PreCallInformation {

	private final String exposureDate;
	private final String code;

	public PreCallInformation(String exposureDate, String code) {
		this.exposureDate = exposureDate;
		this.code = code;
	}

	public String getExposureDate() {
		return exposureDate;
	}

	public String getCode() {
		return code;
	}

}
