package com.stormlin.hi.node.pipeline;

import io.netty.channel.Channel;

import java.util.concurrent.BlockingQueue;

/**
 * 连接入站事件
 *
 * @author lin-jinting
 */
public class ConnectionInboundEvent {
    /**
     * 提供给出站端点的入站连接
     */
    public Channel inboundChannel;
    /**
     * 用于接受出站连接的阻塞队列
     */
    public BlockingQueue<Channel> outboundQueue;

    public ConnectionInboundEvent(Channel inboundChannel, BlockingQueue<Channel> outboundQueue) {
        this.inboundChannel = inboundChannel;
        this.outboundQueue = outboundQueue;
    }
}
