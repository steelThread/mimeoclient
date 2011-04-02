package org.lavalamp.mimeoclient

import static org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

import redis.clients.jedis.Jedis


class MimeographSubscriberTest {
  def subscriber
  def jedis
   
  @Before
  void setUp() {
    subscriber = new MimeographSubscriber()
    jedis = new Jedis('localhost')
    jedis.configSet 'timeout', '300'
    jedis.connect()
    jedis.flushAll()
  }

  @After
  public void tearDown() {
    jedis.disconnect()
  }

  @Test
  void onPMMessage() {
	Thread t = new Thread(new Runnable() {
      void run() {
        try {
          Jedis j = new Jedis('localhost')
          Thread.sleep 1000
          j.publish 'mimeograph:job:test', 'mimeograph:job:test:complete'
          j.disconnect()
        } catch (Exception ex) {
          fail ex.message
        }
      }
    })

    t.start()
    jedis.psubscribe subscriber, 'mimeograph:job:*'
	t.join()
  }
}