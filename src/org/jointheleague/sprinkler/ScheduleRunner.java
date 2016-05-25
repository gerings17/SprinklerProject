package org.jointheleague.sprinkler;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * A ScheduleRunner instance executes repeatedly the list of actions that is
 * passed to it in its constructor.
 * 
 * @author ecolban
 * 
 */
public class ScheduleRunner extends Thread {

	private List<GpioAction> actions;
	// private final static GpioController gpio = GpioFactory.getInstance();
	private final GpioInterface gpio;
	// Output pins
	private final GpioPinDigitalOutput[] myLeds;
	private volatile boolean running;

	private static final Logger logger = Logger
			.getLogger(SprinklerController.class.getName());

	public ScheduleRunner(GpioInterface gpio, List<GpioAction> actions) {

		this.actions = actions;
		this.gpio = gpio;
		this.myLeds = getDigitalOutputs();
	}

	/**
	 * Executes a list of GpioAction's repeatedly. Exits gracefully if
	 * interrupted. The list of actions must be sorted by time and the first
	 * must have a time later than now. The list must not contain more than one
	 * week's worth of actions. After executing an action, the time of the
	 * action is incremented by one week so it can be executed again a week
	 * later.
	 * 
	 * @param actions
	 *            a list of GpioAction's
	 */
	@Override
	public void run() {
		running = true;
		try {
			while (running) {
				for (GpioAction action : actions) {
					long sleepTime = action.getTimeOfAction()
							- TestTime.currentTimeMillis();
					if (0L < sleepTime) {
						Thread.sleep(sleepTime / TestTime.TIME_FACTOR);
						Set<Integer> head = action.getHeadsOff();
						for (Integer i : head) {
							myLeds[i.intValue() - 1].setState(true);
						}
						head = action.getHeadsOn();
						for (Integer i : head) {
							myLeds[i.intValue() - 1].setState(false);
						}
					}
					action.addWeek();
				}
			}
		} catch (InterruptedException ex) {
			logger.log(Level.INFO, "Interrupted on: {0}",
					new Date(TestTime.currentTimeMillis()));

		} finally {
			// Set all pins to "off"
			for (int i = 0; i < myLeds.length; i++) {
				myLeds[i].setState(true);
			}
			logger.info("Turning all heads off.");
			// gpio.shutdown();
		}
	}

	/**
	 * Called to exit gracefully. This method blocks until this thread is dead.
	 * 
	 * @throws InterruptedException
	 *             if interrupted while waiting for this runner to die.
	 */
	public void exitGracefully() throws InterruptedException {
		running = false;
		if (isAlive()) {
			interrupt();
			join();
		}
	}

	/**
	 * Sets up the output pins.
	 * 
	 * @return a GpioGateway instance
	 */
	private GpioPinDigitalOutput[] getDigitalOutputs() {
		GpioPinDigitalOutput[] leds = new GpioPinDigitalOutput[8];
		leds[0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00,
				PinState.HIGH);
		leds[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01,
				PinState.HIGH);
		leds[2] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02,
				PinState.HIGH);
		leds[3] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03,
				PinState.HIGH);
		leds[4] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04,
				PinState.HIGH);
		leds[5] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05,
				PinState.HIGH);
		leds[6] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06,
				PinState.HIGH);
		leds[7] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21,
				PinState.HIGH);

		return leds;
	}

}
