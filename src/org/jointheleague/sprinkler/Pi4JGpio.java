package org.jointheleague.sprinkler;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class Pi4JGpio implements GpioInterface {
	
	private static GpioController gpio = GpioFactory.getInstance();

	@Override
	public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, PinState state) {
		return gpio.provisionDigitalOutputPin(pin, state);
	}
	
}
