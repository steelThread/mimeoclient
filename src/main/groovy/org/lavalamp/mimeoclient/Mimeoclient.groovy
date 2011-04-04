package org.lavalamp.mimeoclient

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

/*
 # Protocol handler for communication with mimeograph.
 */
abstract class Mimeoclient {
  private final static LOGGER = LoggerFactory.getLogger(Mimeoclient.class)

  protected def pool
  protected def subscriber
  protected def subscriberThread

  Mimeoclient() {
    LOGGER.info 'Mimeoclient starting.'
    pool = new JedisPool(new JedisPoolConfig(), 'localhost')
    subscriber = new Subscriber()
    subscribe()
    addShutdownHook { shutdown() }
  }

  //
  // process a completed job (Map)
  //
  abstract process(job)

  //
  // queue some work.  work can be either a string (file path),
  // a list of strings (file paths) or a map of jobid to strings.
  // assumes mimeograph is installed locally to the client.
  //
  def work(work) {
    if (work instanceof String) {
      LOGGER.info 'Work is a string'        
    } else if (work instanceof List) {
      LOGGER.info 'Work is a List'              
    } else if (work instanceof Map) {
      LOGGER.info 'Work is a map'               
    }
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
  // destroy the pool and signal the subscriber to stop
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
      Jedis jedis = pool.resource
      try { jedis.hgetAll message[0..<message.lastIndexOf(':')] } 
      finally {
        pool.returnResource jedis
      }
    }
  }
}