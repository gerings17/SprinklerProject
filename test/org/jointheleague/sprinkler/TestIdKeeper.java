package org.jointheleague.sprinkler;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class TestIdKeeper {

	@Test
	public void testGetId() {
		try {
			String id = new IdKeeper().getId();
			System.out.println(id);
			assertTrue(id != null);
			assertEquals(32, id.length());
		} catch (IOException e) {
			fail("Normally, should not get here.");
		}
		
	}

}
