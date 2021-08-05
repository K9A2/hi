package com.stormlin.hi.node.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 入站连接处理器
 *
 * @author lin-jinting
 */
public class InboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundHandler.class);
    private final BlockingQueue<ConnectionInboundEvent> inboundEventQueue;
    private Channel outboundChannel;

    public InboundHandler(BlockingQueue<ConnectionInboundEvent> channelQueue) {
        this.inboundEventQueue = channelQueue;
    }

    /**
     * 入栈连接可以就绪后通知后端准备出站连接
     *
     * @param ctx 上下文
     * @throws InterruptedException 异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        // 这里使用一个队列来接收来自出站端点建立的出站连接
        // 在把队列传过去之后，出入站节点需要互相交换出入站连接
        final Channel inboundChannel = ctx.channel();
        BlockingQueue<Channel> channelQueue = new LinkedBlockingQueue<>(2);
        // 通知 pipeline 该事件，payload 就是交换出入站连接的阻塞队列
        this.inboundEventQueue.put(new ConnectionInboundEvent(inboundChannel, channelQueue));
        // 取到出站连接之后才能开始代理数据
        Channel outboundChannel = channelQueue.take();
        this.outboundChannel = outboundChannel;
        LOGGER.info("proxy connection: {} <-> {}", inboundChannel.remoteAddress(), outboundChannel.remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    // 写完之后立刻开始读取从入站连接中读取下一部分数据
                    ctx.channel().read();
                } else {
                    LOGGER.warn("close remote connection due to failed write: {}", f.channel().remoteAddress());
                    f.channel().close();
                }
            });
        }
    }
}
