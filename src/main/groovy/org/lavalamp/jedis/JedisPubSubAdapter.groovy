package org.lavalamp.mimeoclient

import redis.clients.jedis.JedisPubSub

/*
 # Noop adapter.
 */
class JedisPubSubAdapter extends JedisPubSub {
  void onMessage(String channel, String message) { }
  void onPMessage(String pattern, String channel, String message) { }
  void onSubscribe(String channel, int subscribedChannels) { }
  void onUnsubscribe(String channel, int subscribedChannels) { }
  void onPUnsubscribe(String pattern, int subscribedChannels) { }
  void onPSubscribe(String pattern, int subscribedChannels) { }
}
