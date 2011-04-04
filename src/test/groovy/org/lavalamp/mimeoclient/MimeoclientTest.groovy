package org.lavalamp.mimeoclient

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

import org.junit.After
import org.junit.Before
import org.junit.Test

import redis.clients.jedis.Jedis


class MimeoclientTest {
  def client
  def jedis
   
  @Before
  void setUp() {
    jedis = new Jedis('localhost')
    jedis.flushAll()
    client = new FixtureMimeoclient()
  }

  @After
  public void tearDown() {
    client.end()
    jedis.disconnect()
  }

  @Test
  void onPMMessage() {
    def t = Thread.start {
      jedis.hmset 'mimeograph:job:test', [
        'started'       : 'now', 
        'ended'         : 'now',
        'text'          : 'text',
        'status'        : 'success',
        'num_processed' : '2'
      ]
      jedis.publish 'mimeograph:job:test', 'mimeograph:job:test:complete'
      Thread.sleep 1000
      client.end()
    }
    client.connect()
    t.join()
  }

  @Test
  void workWithException() {
    try { 
	  def result = client.work('work')
	  println(result)
	  fail()
	} catch (Exception e) {
      e.printStackTrace()		
	}
  }
  
  ///////////////////////////////////////////////
  static class FixtureMimeoclient extends Mimeoclient {
    def process(job) {
      assertNotNull job
      job.with {
        assertEquals 'now', get('started')
        assertEquals 'now', get('ended')
        assertEquals 'text', get('text')
        assertEquals 'success', get('status')
        assertEquals '2', get('num_processed')
      }
    }
  }
}