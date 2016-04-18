import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.json.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class GroupFinder {
	
	private static final int CALLS_BEFORE_THROTTLE = 29;
	
	// keeps track of when throttling is necessary
	private static int num_calls = 0;
	
	// opens and closes an HttpURLConnection at url_string, returning a json string response
	private static String doAPICall(String url_string) {
		
		String response = "";
		
		try {
			
			// throttle calls
			if(num_calls == CALLS_BEFORE_THROTTLE) {
				System.out.println("throttling...");
				TimeUnit.SECONDS.sleep(10);
				num_calls = 0;
			}
			num_calls++;
			
			// make request
			URL url = new URL(url_string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			
			//request failed
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			
			//read response from stream
			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
		
			//parse response
			String line;
			while ((line = br.readLine()) != null) {
				response += line;
			}
			
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return response;
		
	}
	
	// scrape the group_id and group_name from a json response string, return a group map
	private static HashMap<Integer, String> parseGroupMapFromResponseString(String response) {
		
		// build json object from response string
		JSONArray results = new JSONArray(response);
		
		// build map
		int num_results = results.length();
		HashMap<Integer, String> group_map = new HashMap<>();
		for(int i = 0; i < num_results; i++) {
			JSONObject resultsJSON = results.getJSONObject(i);
			group_map.put(resultsJSON.getInt("id"), resultsJSON.getString("name"));
		}
		
		return group_map;
	}
	
	// makes an API call for each ID in groups, storing responses in group_events
	private static HashMap<Integer, String> getUpcomingEvents(Set<Integer> groups) {
		
		HashMap<Integer, String> group_events = new HashMap<>();
		String url_base = "https://api.meetup.com/2/events?status=upcoming&page=10&key=2c4a366e37e5a7c287b2e226d57b3e&group_id=";
		
		// make calls
		int i = 1;
		for(Integer id: groups) {
			group_events.put(id, doAPICall(url_base + id));
			System.out.println("getting events for group " + i++ + " of " + groups.size() + " recommended groups...");
		}
		
		return group_events;
	}
	
	// outputs each group in group_map that has at least one upcoming meeting on the specified day
	private static void printGroupsWithPreferredMeetingDay(String day, HashMap<Integer, String> group_map,
														   HashMap<Integer, String> group_events_map) {
		
		System.out.println("\nSuggested groups that have an upcoming meeting"
				+ " on your preferred day:");
		for(Integer group_id: group_map.keySet()) {
			
			// build a json array of this group's events
			String events_str = group_events_map.get(group_id);
			JSONArray events = new JSONObject(events_str).getJSONArray("results");
			
			// if any of the events for this group meet on the specified day, print the group name
			int num_events = events.length();
			for(int i = 0; i < num_events; i++) {
				
				// extract the day on which this event takes place
				JSONObject eventJSON = events.getJSONObject(i);
				long datetime = eventJSON.getLong("time");
		        Date date = new Date(datetime);
		        DateFormat format = new SimpleDateFormat("EE");
		        String formatted = format.format(date);
		        
		        //print group name, move on to next group
		        if(formatted.equals(day)) {
		        	System.out.println("\t" + group_map.get(group_id));
		        	break;
		        }
			}
		}
	}
	
	// ask the user which day of the week they like to meet
	private static String getDayFromUser() {
		
		Scanner scan = new Scanner(System.in);
		System.out.println("This tool will recommend groups that meet on your preferred day of the week.\n");
		System.out.print("Which day of the week would you prefer to meet (Mon/Tue/Wed/Thu/Fri/Sat/Sun)? ");
		String day = scan.next();
		
		while((!day.equals("Mon")) &&
			  (!day.equals("Tue")) &&
		      (!day.equals("Wed")) &&
		      (!day.equals("Thu")) &&
		      (!day.equals("Fri")) &&
		      (!day.equals("Sat")) &&
		      (!day.equals("Sun"))) {
			System.out.println("Invalid format");
			System.out.print("Which day of the week would you prefer to meet (Mon/Tue/Wed/Thu/Fri/Sat/Sun)? ");
			day = scan.next();
		}
		
		System.out.println();
		scan.close();
		return day;
	}
	
	public static void main(String[] args) {
		
		String day = getDayFromUser();

	    // request recommended groups
		String response = doAPICall("https://api.meetup.com/recommended/groups?page=100&key=2c4a366e37e5a7c287b2e226d57b3e");

		// get a map of recommended groups (group_id => group_name)
		HashMap<Integer, String> group_map = parseGroupMapFromResponseString(response);
		
		// get a map of upcoming events for recommended groups (group_id => events API call response)
		HashMap<Integer, String> group_events_map = getUpcomingEvents(group_map.keySet());
		
		printGroupsWithPreferredMeetingDay(day, group_map, group_events_map);

	}
}