package proxy.events

/**
 * This event is posted when a PubSub message is received.
 *
 *
 * **Warning**: This event is fired in a separate thread!
 *
 * @since 0.2.6
 */
class PubSubMessageEvent(val channel: String, val message: String)