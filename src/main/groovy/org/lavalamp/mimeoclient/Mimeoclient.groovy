package org.lavalamp.mimeoclient

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

/*
 # Protocol handler for communication with mimeograph.
 */
class Mimeoclient {
  private final static LOGGER = LoggerFactory.getLogger(Mimeoclient.class)

  private def pool
  private def subscriber
  private def subscriberThread

  Mimeoclient() {
	pool = new JedisPool('localhost')
	pool.init()		
    subscriber = new Subscriber()
    subscribe()
	addShutdownHook { shutdown() }
  }

  //
  // start the subscriber in a new thread
  //
  def subscribe() {
	LOGGER.info 'Starting the subscriber.'	
	subscriberThread = Thread.start {
      jedis = new Jedis('localhost')
	  jedis.psubscribe subscriber, 'mimeograph:job:*'
	  jedis.disconnect()
    }
  }

  //
  // callback for the subscriber
  //
  def process() {
	LOGGER.info 'Processing completed job {}.'
  }

  //
  // fetches the next 'n'
  //
  def queueJobs() {
	LOGGER.info 'Requesting more work.'	
  }

  // 
  // destroy the pool and signal the subscriber
  // to stop
  //
  def shutdown() {
	pool?.destroy()
	subscriber?.unsubscribe()
    subscriberThread?.join()
  }

  //
  // Receives the job notifications, fetches the job hash
  // and notifies the client.
  //
  class Subscriber extends JedisPubSubAdapter {
    void onPMessage(String pattern, String channel, String message) {
      LOGGER.info "Channel -> {} : Message -> {}", channel, message
    }
  }
}