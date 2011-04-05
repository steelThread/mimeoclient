package org.lavalamp.mimeoclient

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

/*
 # Protocol handler for communication with mimeograph.
 */
abstract class Mimeoclient {
  protected final static LOGGER = LoggerFactory.getLogger(Mimeoclient.class)

  protected def pool
  protected def subscriber

  Mimeoclient() {
    LOGGER.info 'Mimeoclient starting.'
    pool = new JedisPool(new JedisPoolConfig(), 'localhost')
    subscriber = new Subscriber()
    addShutdownHook { end() }
  }

  //
  // process a completed job (Map)
  //
  abstract process(job)

  //
  // queue some work. will accept either one or two
  // arguments.  when passing an id for the job then
  // pass that as the first arg and the file path
  // as the second.  RuntimeException will be thrown
  // if the job could not be started.
  //
  def work(String... work) {
    assert work.size() < 3
    def proc  = "mimeograph -p ${work.join(' ')}".execute()
    def out = new StringBuilder()
    def err = new StringBuilder()
    proc.waitForProcessOutput out, err
    if (err) {
      throw new IllegalArgumentException(err.toString())
    }
  
    def job = (out.toString() =~ /job:\S+/)[0]
    [id : job[4..-1]]
  }

  //
  // start the subscriber.  Note: blocks the current thread!
  //
  def connect() {
    LOGGER.info 'Starting the subscriber.'  
    Jedis jedis = new Jedis('localhost')
    jedis.psubscribe subscriber, 'mimeograph:job:*'
    LOGGER.info 'Subscriber shutting down.'
    jedis.disconnect()
  }

  // 
  // destroy the pool and signal the subscriber to stop
  //
  def end() {
    LOGGER.info 'Mimeoclient shutting down.'    
    if (subscriber?.isSubscribed()) { subscriber.punsubscribe() }
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