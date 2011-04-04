package org.lavalamp.mimeoclient

import org.junit.After
import org.junit.Before
import org.junit.Test

import redis.clients.jedis.Jedis


class MimeoclientTest {
  def client
  def jedis
  def fixture
   
  @Before
  void setUp() {
	jedis = new Jedis('localhost')
    jedis.flushAll()
    client = new Mimeoclient()
  }

  @After
  public void tearDown() {
    client.shutdown()
    jedis.disconnect()
  }

  @Test
  void onPMMessage() {
    jedis.hmset 'mimeograph:job:test', [
      'started'       : 'now', 
      'ended'         : 'now',
      'text'          : 'text',
      'status'        : 'fail',
      'num_processed' : '2'
    ]
    jedis.publish 'mimeograph:job:test', 'mimeograph:job:test:complete'
  }
}