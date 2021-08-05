package com.stormlin.hi.node.pipeline;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 流水线工具类
 *
 * @author lin-jinting
 */
public class PipelineUtil {
    /**
     * 工具类的私有构造方法，禁止实例化工具类
     */
    private PipelineUtil() {
    }

    /**
     * 关闭并清空目标 channel
     *
     * @param channel 需要关闭的 channel
     */
    public static void closeOnFlushChannel(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
