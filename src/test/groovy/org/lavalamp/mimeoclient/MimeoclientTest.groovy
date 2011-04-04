package org.lavalamp.mimeoclient

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

import org.junit.After
import org.junit.Before
import org.junit.Test

import redis.clients.jedis.Jedis


class MimeoclientTest {
  def client
  def jedis
  def job
   
  @Before
  void setUp() {
    jedis = new Jedis('localhost')
    jedis.flushAll()
    client = new FixtureMimeoclient()
    job = [
      'started'       : 'now', 
      'ended'         : 'now',
      'text'          : 'text',
      'status'        : 'success',
      'num_processed' : '2'
    ]
  }

  @After
  public void tearDown() {
    client.shutdown()
    jedis.disconnect()
  }

  @Test
  void onPMMessage() {
	def t = Thread.start {
      jedis.hmset 'mimeograph:job:test', job
      jedis.publish 'mimeograph:job:test', 'mimeograph:job:test:complete'
      Thread.sleep 1000
    }
    t.join()
  }

  ///////////////////////////////////////////////
  static class FixtureMimeoclient extends Mimeoclient {
    def process(job) {
	  assertNull job
    }
  }
}