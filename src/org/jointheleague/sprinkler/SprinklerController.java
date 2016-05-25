package org.jointheleague.sprinkler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A SprinklerController instance regularly checks to see if there are updates
 * of the sprinkler schedule and runs the sprinkler according to the most recent
 * update. It requires network connectivity to run.
 * 
 * @author ecolban
 * 
 */
public class SprinklerController {

	// Usage
	private final static String USAGE = "Usage: sudo java -jar RPiGPIOTester.jar";

	private static long SCHEDULE_CHECK_PERIOD = 60 * 60 * 1000; // = 1 hour

	private static final Logger logger = Logger
			.getLogger(SprinklerController.class.getName());

	/**
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			ParseException, IOException {
		if (args.length != 0) {
			System.out.println(USAGE);
			return;
		}
		new SprinklerController().run();
	}

	/**
	 * Runs the sprinkler forever according to the most recently read schedule.
	 * Regularly checks if there is a more recent update online, and if that is
	 * the case, tries to retrieve and read it.
	 * 
	 * @throws InterruptedException
	 *             if interrupted
	 * @throws ParseException
	 * @throws IOException
	 */
	public void run() throws InterruptedException, ParseException, IOException {
		ScheduleReader reader = new ScheduleReader();
		URL url = getURL();
		int lastRead = -1;
		ScheduleRunner runner = null;
		while (true) {
			try {
				JSONObject schedule = reader.read(url);
				reader.parseShedule(schedule);
				if (reader.getVersion() > lastRead) {
					if (runner != null) {
						runner.exitGracefully();
					}
					List<GpioAction> actions = reader.getActionList();
					logger.log(Level.INFO, "Got actions");
					lastRead = reader.getVersion();
					// runner = new ScheduleRunner(new Pi4JGpio(), actions);
					runner = new ScheduleRunner(new MockGpio(), actions);
					runner.start();
				}
				Thread.sleep(SCHEDULE_CHECK_PERIOD / TestTime.TIME_FACTOR);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			} catch (JSONException e) {
				logger.log(Level.SEVERE, e.getMessage());
//			} catch (Exception e) {
//				logger.log(Level.SEVERE, e.getClass().getName());
			}
		}

	}

//	private URL getURL() throws MalformedURLException {
//		IdKeeper idKeeper = new IdKeeper();
//		try {
//			return new URL(
//					"http://everydropcounts.jointheleague.org/schedules/s"
//							+ idKeeper.getId());
//		} catch (IOException e) {
//			return null;
//		}
//	}

	private URL getURL() throws IOException {
		IdKeeper idKeeper = new IdKeeper();
		File home = new File(System.getProperty("user.home"));
		File f = new File(home, "schedules/s" + idKeeper.getId() + ".json");
		logger.log(Level.INFO, "File: {0}", f.toString());
		return f.toURI().toURL();
	}
}
