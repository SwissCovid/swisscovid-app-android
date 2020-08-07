package ch.admin.bag.dp3t.travel;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.admin.bag.dp3t.networking.models.CountryModel;

public class CountryListUtils {

	public static List<Country> sortCountriesList(List<Country> countries, Context context) {

		List<Country> sortedCountries = new ArrayList<>();
		List<Country> nonFavouriteCountries = new ArrayList<>();

		for (Country country : countries) {
			if (country.isFavourite()) sortedCountries.add(country);
			else nonFavouriteCountries.add(country);
		}

		Collections.sort(nonFavouriteCountries, (c1, c2) -> c1.getCountryName(context).compareTo(c2.getCountryName(context)));
		sortedCountries.addAll(nonFavouriteCountries);
		return sortedCountries;
	}

	public static List<Country> mergeLocalWithBackendCountries(List<Country> localCountries, List<CountryModel> backendCountries,
			Context context) {

		List<Country> updatedCountries = new ArrayList<>();

		//Check if any countries are removed from the list
		for (Country localCountry : localCountries) {
			boolean isRemovedFromList = true;
			for (CountryModel backendCountry : backendCountries) {
				if (backendCountry.getIsoCountryCode().equalsIgnoreCase(localCountry.getIsoCode())) {
					isRemovedFromList = false;
					break;
				}
			}
			if (!isRemovedFromList) updatedCountries.add(localCountry);
		}

		//Add new countries to the (end of the) local list of countries
		for (CountryModel backendCountry : backendCountries) {
			//Check if we already have this country in our current country list
			boolean isAlreadyPresent = false;
			for (Country currentCountry : updatedCountries) {
				if (currentCountry.getIsoCode().equalsIgnoreCase(backendCountry.getIsoCountryCode())) {
					isAlreadyPresent = true;
					break;
				}
			}
			if (!isAlreadyPresent) {
				updatedCountries.add(new Country(backendCountry.getIsoCountryCode(), false, false, -1));
			}
		}
		return sortCountriesList(updatedCountries, context);
	}

}
