package org.jointheleague.sprinkler;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public interface GpioInterface {

	
	GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, PinState state);
	

	
}
