package org.lavalamp.mimeoclient

import org.junit.After
import org.junit.Before
import org.junit.Test

import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis


class MimeographSubscriberTest {
  final static LOGGER = LoggerFactory.getLogger(MimeographSubscriberTest.class)
  
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
          Jedis j = new Jedis()
          LOGGER.debug 'test'
          Thread.sleep 1000
          j.publish 'mimeograph:job:test', 'mimeograph:job:test:complete'
          j.disconnect();
        } catch (Exception ex) {
          fail ex.message
        }
      }
    })

    t.start()
    //jedis.psubscribe subscriber, 'mimeograph:job:*'
	t.join()
  }
}