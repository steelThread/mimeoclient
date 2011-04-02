package org.lavalamp.mimeoclient

import redis.clients.jedis.Jedis

/*
 # 
 */
class Mimeoclient {
  private def jedis

  /*
   # connect needs to be done in a thread.
   */
  void connect() {
	jedis = new Jedis('localhost')
	jedis.psubscribe new MimeographSubscriber(), 'mimeograph:job:*'
  }

  void disconnect() { 
	jedis.disconnect() 
  }
}
