package org.lavalamp.bootstrap

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

import org.junit.Test
import org.slf4j.LoggerFactory;

class TestiesTest {
  private final static LOGGER = LoggerFactory.getLogger(TestiesTest.class)

  @Test
  void one() {
	LOGGER.info "one"		
	assertTrue true 
  }
	
  @Test
  void two() {
	LOGGER.info "two"
    assertTrue true
  }

  @Test
  void three() {
	LOGGER.info "three?"
    assertFalse false
  }
}