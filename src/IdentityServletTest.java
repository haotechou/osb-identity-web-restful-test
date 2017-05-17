/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * @author Haote Chou
 */
public class IdentityServletTest {

	public static void main(String[] args) throws Exception {
		File file = new File(
			new File("").getAbsolutePath() + "/resources/users.csv");

//		File file = new File(
//			new File("").getAbsolutePath() + "/../resources/users.csv");

		if (file.exists()) {
			BufferedReader bufferedReader =
				new BufferedReader(new FileReader(file));

			int counter = 1;
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				URL url = getURL(line);

				HttpURLConnection httpURLConnection =
					(HttpURLConnection)url.openConnection();

				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setRequestProperty (
					"Authorization",
					"token iAAXumhiGCqEwFkWwsmTXg9WmstZLcwxYsIjePYarO2jlcNnwKzwiW0knfJZqMyQ");

				Date startTime = new Date();

				int status = httpURLConnection.getResponseCode();

				Date endTime = new Date();

				long difference = endTime.getTime() - startTime.getTime();

//				System.out.println(
//					"Entry " + counter + " spent " + difference + "ms");

				if (status == 200) {
					printMessage(counter, httpURLConnection);
				}
				else {
					System.out.println(
						"Entry " + counter + " got reponse code " + status);

					System.out.println(url.toString());
				}

				counter++;

				httpURLConnection.disconnect();
			}

			bufferedReader.close();
		}
		else {
			System.out.println("users.csv does not exist");
		}
	}

	protected static URL getURL(String line) throws Exception {
		String[] userData = line.split(", ");

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("http://localhost:8081/osb-users-web/users?");

//		stringBuilder.append(
//			"http://cloud-10-50-0-1.lax.liferay.com/osb-users-web/users?");

		stringBuilder.append("firstName=");
		stringBuilder.append(userData[1].replaceAll("'",""));
		stringBuilder.append("&");
		stringBuilder.append("lastName=");
		stringBuilder.append(userData[2].replaceAll("'",""));
		stringBuilder.append("&");
		stringBuilder.append("emailAddress=");
		stringBuilder.append(userData[3].replaceAll("'",""));
		stringBuilder.append("&");
		stringBuilder.append("keywords=");
		stringBuilder.append("");
		stringBuilder.append("&");
		stringBuilder.append("start=");
		stringBuilder.append(-1);
		stringBuilder.append("&");
		stringBuilder.append("end=");
		stringBuilder.append(-1);

		return new URL(stringBuilder.toString());
	}

	protected static void printMessage(
			int counter, HttpURLConnection httpURLConnection)
		throws Exception {

		InputStreamReader inputStreamReader = new InputStreamReader(
			httpURLConnection.getInputStream());

		BufferedReader bufferedReader = new BufferedReader(
			inputStreamReader);

		StringBuilder stringBuilder = new StringBuilder();

		String line = bufferedReader.readLine();

		if (line != null) {
			stringBuilder.append(line);
		}

		JSONObject outerJSONObject = new JSONObject(
			stringBuilder.toString());

		JSONArray jsonArray = outerJSONObject.getJSONArray("users");

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject innerJSONObject = jsonArray.getJSONObject(i);

			String message = "Entry " + counter + ": " +
				innerJSONObject.get("userId") + ", " +
				innerJSONObject.get("firstName") + ", " +
				innerJSONObject.get("lastName") + ", " +
				innerJSONObject.get("fullName") + ", " +
				innerJSONObject.get("emailAddress") + ", " +
				innerJSONObject.get("uuid");

			System.out.println(message);
		}

		bufferedReader.close();
		inputStreamReader.close();
	}
}