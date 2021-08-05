package com.stormlin.hi.node.pipeline;

import com.stormlin.hi.node.entity.TerminalDefinition;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * 入站节点终端
 *
 * @author lin-jinting
 */
public class InboundTerminal implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundTerminal.class);
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final BlockingQueue<ConnectionInboundEvent> inboundEventQueue;

    private final String cipher;
    private final String password;
    private final String host;
    private final int port;

    public InboundTerminal(TerminalDefinition definition, BlockingQueue<ConnectionInboundEvent> inboundEventQueue) {
        this.cipher = definition.getCipher();
        this.password = definition.getPassword();
        this.host = definition.getHost();
        this.port = definition.getPort();
        this.inboundEventQueue = inboundEventQueue;
    }

    @Override
    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(Channel socketChannel) {
                                socketChannel.pipeline().addLast(new InboundHandler(inboundEventQueue));
                            }
                        })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture bindFuture;
        try {
            bindFuture = bootstrap.bind(this.host, this.port).sync();
        } catch (InterruptedException e) {
            LOGGER.error("error in binding: host: {}, port: {}", this.host, this.port);
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            return;
        }
        if (bindFuture.isSuccess()) {
            LOGGER.info("tcp server start at: {}:{}", this.host, this.port);
        } else {
            LOGGER.error("bind failed, host: {}, port: {}", this.host, this.port);
            return;
        }

        // 等待关闭
        try {
            bindFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("exception occurred when waiting for inbound terminal closing");
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        LOGGER.info("inbound terminal {}:{} closed", this.host, this.port);
    }
}
