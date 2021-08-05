package com.stormlin.hi.node.pipeline;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出站处理器
 *
 * @author lin-jinting
 */
public class OutboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundHandler.class);
    private final Channel inboundChannel;

    public OutboundHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                future.channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final Channel outboundChannel = ctx.channel();
        PipelineUtil.closeOnFlushChannel(outboundChannel);
        LOGGER.info("close outbound channel to {}", outboundChannel.remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        final Channel outboundChannel = ctx.channel();
        PipelineUtil.closeOnFlushChannel(outboundChannel);
        PipelineUtil.closeOnFlushChannel(this.inboundChannel);
        LOGGER.error("close inbound channel from {} and outbound channel to {} due to exception",
                inboundChannel.remoteAddress(), outboundChannel.remoteAddress());
    }
}
