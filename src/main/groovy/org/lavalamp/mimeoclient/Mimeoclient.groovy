package org.lavalamp.mimeoclient

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

/*
 # Protocol handler for communication with mimeograph.
 */
class Mimeoclient {
  private final static LOGGER = LoggerFactory.getLogger(Mimeoclient.class)

  private def pool
  private def subscriber
  private def subscriberThread

  Mimeoclient() {
    LOGGER.info 'Mimeoclient starting.'
    pool = new JedisPool(new JedisPoolConfig(), 'localhost')
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
      Jedis jedis = new Jedis('localhost')
      jedis.psubscribe subscriber, 'mimeograph:job:*'
      LOGGER.info 'Subscriber shutting down.'
      jedis.disconnect()
    }
  }

  //
  // callback for the subscriber
  //
  def process(job) {
    LOGGER.info 'Processing message {}.', job
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
    LOGGER.info 'Mimeoclient shutting down.'    
    subscriber?.punsubscribe()
    subscriberThread?.join()
    pool?.destroy()
  }

  //
  // Receives the job notifications, fetches the job hash
  // and notifies the client.
  //
  class Subscriber extends JedisPubSubAdapter {
    void onPMessage(String pattern, String channel, String message) {
      LOGGER.info "Received msg.  Channel -> {} : Message -> {}", channel, message   
      process decode(message)
    }

    def decode(message) {
      Jedis jedis = pool.getResource()
      try { jedis.hgetAll message[0..<message.lastIndexOf(':')] } 
      finally {
        pool.returnResource(jedis)  
      }
    }
  }
}