package org.jointheleague.sprinkler;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class MockGpio implements GpioInterface {

	@Override
	public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, PinState state) {
		return new MockDigitalOutput(pin, state);
	}

}
