package org.lavalamp.mimeoclient

import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPubSub

/*
 #
 */
class MimeographSubscriber extends JedisPubSubAdapter {
  private final static LOGGER = LoggerFactory.getLogger(MimeographSubscriber.class)
	
  void onPMessage(String pattern, String channel, String message) {
	LOGGER.info "Channel -> {} : Message -> {}", channel, message
    punsubscribe()
  }
}
