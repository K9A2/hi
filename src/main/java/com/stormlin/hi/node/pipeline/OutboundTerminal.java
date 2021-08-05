package com.stormlin.hi.node.pipeline;

import com.stormlin.hi.node.entity.TerminalDefinition;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * 出站节点终端
 *
 * @author lin-jinting
 */
public class OutboundTerminal implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundTerminal.class);
    /**
     * 出站连接建立后，通过该队列反馈给入站节点
     */
    private final BlockingQueue<Channel> outboundChannelOpened;
    private final Channel inboundChannel;

    private final String cipher;
    private final String password;
    private final String host;
    private final int port;

    public OutboundTerminal(TerminalDefinition definition, ConnectionInboundEvent inboundEvent) {
        this.cipher = definition.getCipher();
        this.password = definition.getPassword();
        this.host = definition.getHost();
        this.port = definition.getPort();
        this.outboundChannelOpened = inboundEvent.outboundQueue;
        this.inboundChannel = inboundEvent.inboundChannel;
    }

    @Override
    public void run() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(this.inboundChannel.getClass())
                .handler(new OutboundHandler(this.inboundChannel));
        ChannelFuture connectFuture;
        try {
            connectFuture = bootstrap.connect(this.host, this.port).sync();
        } catch (InterruptedException e) {
            LOGGER.info("error in connecting to remote: {}:{}", this.host, this.port);
            e.printStackTrace();
            return;
        }
        // 成功连接之后才通知入站端进行操作
        final Channel outboundChannel = connectFuture.channel();
        try {
            this.outboundChannelOpened.put(outboundChannel);
        } catch (InterruptedException e) {
            LOGGER.error("error in putting outbound channel back to inbound handler");
            e.printStackTrace();
            return;
        }
        connectFuture.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                inboundChannel.read();
            } else {
                LOGGER.warn("close inbound channel due to failed remote connection: inbound: {}, remote: {}",
                        inboundChannel.remoteAddress(), outboundChannel.remoteAddress());
                inboundChannel.close();
            }
        });
    }
}
