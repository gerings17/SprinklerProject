package org.jointheleague.sprinkler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScheduleReader {

	private static final TimeZone TZ = TimeZone.getTimeZone("PST");

	private static final Logger logger = Logger
			.getLogger(SprinklerController.class.getName());

	private static final String[] WEEKDAYS = new String[] { "Monday",
			"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	private int version = -1;
	private List<GpioAction> actionList = null;

	public static void main(String[] args) throws JSONException, IOException,
			ParseException {

		ScheduleReader reader = new ScheduleReader();
		URL url = reader.getClass().getResource("test.json");
		// URL url = null;
		// try {
		// url = new URL("http://98.176.141.155:3000/get/TEST");
		// } catch (MalformedURLException e) {
		// logger.log(Level.SEVERE, e.getMessage());
		// return;
		// }
		JSONObject schedule = reader.read(url);
		reader.parseShedule(schedule);
		List<GpioAction> list = reader.getActionList();
		for (GpioAction action : list) {
			System.out.println(action);
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public JSONObject read(URL url) throws IOException, JSONException {
		BufferedReader rd = null;
		try {
			InputStream is = url.openStream();
			rd = new BufferedReader(new InputStreamReader(is));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			rd.close();
		}
	}

	public void parseShedule(JSONObject schedule) throws IOException,
			JSONException, ParseException {
		version = Integer.parseInt(schedule.getString("version"));
		JSONArray zones = schedule.getJSONArray("schedule");
		List<GpioAction> gpioActions = new ArrayList<GpioAction>();
		for (int i = 0; i < zones.length(); i++) {
			JSONObject zone = zones.getJSONObject(i);
			int zoneId = zone.getInt("Zone");
			for (String day : WEEKDAYS) {
				if (zone.has(day)) {
					JSONObject daySchedule = zone.getJSONObject(day);
					if (daySchedule.getString("power").equals("on")) {
						String startTime = daySchedule.getString("start");
						GpioAction a = new GpioAction(new HashSet<Integer>(),
								new HashSet<Integer>(), getCalendarTime(day,
										startTime));
						a.getHeadsOn().add(zoneId);
						gpioActions.add(a);
						String endTime = daySchedule.getString("end");
						a = new GpioAction(new HashSet<Integer>(),
								new HashSet<Integer>(), getCalendarTime(day, endTime));
						a.getHeadsOff().add(zoneId);
						gpioActions.add(a);
					}
				}
			}
		}
		Collections.sort(gpioActions);
		actionList = compress(gpioActions);
	}

	private List<GpioAction> compress(List<GpioAction> gpioActions) {
		List<GpioAction> result = new ArrayList<GpioAction>();
		GpioAction current = gpioActions.remove(0);
		for (GpioAction a : gpioActions) {
			if (current.getTimeOfAction() == a.getTimeOfAction()) {
				current.getHeadsOff().addAll(a.getHeadsOff());
				current.getHeadsOn().addAll(a.getHeadsOn());
			} else {
				result.add(current);
				current = a;
			}
		}
		result.add(current);
		return result;
	}

	private Calendar getCalendarTime(String day, String timeOfDay)
			throws ParseException {
		Pattern timePattern = Pattern.compile("(\\d?\\d):(\\d\\d)\\s*([AP]M)");
		Matcher m = timePattern.matcher(timeOfDay);
		if (m.matches()) {

			Calendar t = Calendar.getInstance(TZ);
			int dayOfweek = 0;
			for (int i = 0; i < WEEKDAYS.length; i++) {
				if (WEEKDAYS[i].equals(day)) {
					dayOfweek = (Calendar.MONDAY + i) % 7;
					break;
				}
			}
			int hour = Integer.parseInt(m.group(1));
			if (hour == 12) {
				if (m.group(3).equals("AM")) {
					hour = 0;
				}
			} else {
				if (m.group(3).equals("PM")) {
					hour += 12;
				}
			}
			int minutes = Integer.parseInt(m.group(2));
			t.set(Calendar.HOUR_OF_DAY, hour);
			t.set(Calendar.MINUTE, minutes);
			t.set(Calendar.DAY_OF_WEEK, dayOfweek);
			t.set(Calendar.SECOND, 0);
			t.set(Calendar.MILLISECOND, 0);
			return t;
		} else {
			throw new ParseException("Not a time spec: " + timeOfDay, 0);
		}
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return the ActionList
	 */
	public List<GpioAction> getActionList() {
		return actionList;
	}

}
