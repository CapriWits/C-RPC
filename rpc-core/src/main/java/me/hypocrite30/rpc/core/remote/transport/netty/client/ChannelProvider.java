package me.hypocrite30.rpc.core.remote.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Channel provider is to store and offer available channel
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/20 22:38
 */
@Slf4j
public class ChannelProvider {

    /**
     * @key: InetSocketAddress
     * @value: Channel
     */
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    /**
     * Get connected Channel for InetSocketAddress
     * or return null if socket address does not have connected Channel
     *
     * @param inetSocketAddress InetSocketAddress
     * @return Available Channel
     */
    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // return available Channel
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        // this socket address does not have connected Channel
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size: [{}]", channelMap.size());
    }
}
