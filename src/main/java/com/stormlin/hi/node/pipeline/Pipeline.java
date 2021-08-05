package com.stormlin.hi.node.pipeline;

import com.stormlin.hi.node.entity.TerminalDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 代理流水线
 *
 * @author lin-jinting
 */
public class Pipeline implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);
    private static final int TIME_OUT = 3;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final TerminalDefinition inDefinition;
    private final TerminalDefinition outDefinition;
    private final BlockingQueue<ConnectionInboundEvent> eventQueue = new LinkedBlockingQueue<>();


    /**
     * 传递入站事件，出站端点建立后，向此队列中返回出站连接
     *
     * @param in  入站节点定义
     * @param out 出站节点定义
     */
    public Pipeline(TerminalDefinition in, TerminalDefinition out) {
        this.inDefinition = in;
        this.outDefinition = out;
    }

    @Override
    public void run() {
        // 运行入站端点
        InboundTerminal inboundTerminal = new InboundTerminal(this.inDefinition, this.eventQueue);
        threadPool.submit(inboundTerminal);
        LOGGER.info("pipeline is running: local: {}:{}, remote: {}:{}", this.inDefinition.getHost(),
                this.inDefinition.getPort(), this.outDefinition.getHost(), this.outDefinition.getPort());
        // 每当收到连接入站事件时，就发起一条到对端的连接
        ConnectionInboundEvent inboundEvent;
        while (true) {
            try {
                inboundEvent = this.eventQueue.take();
            } catch (InterruptedException e) {
                LOGGER.error("error in taking event from connection inbound event queue");
                e.printStackTrace();
                break;
            }
            OutboundTerminal outboundTerminal = new OutboundTerminal(this.outDefinition, inboundEvent);
            threadPool.submit(outboundTerminal);
        }
        threadPool.shutdown();

        boolean isNormallyExited;
        try {
            isNormallyExited = threadPool.awaitTermination(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("pipeline, isNormallyExited: {}: local: {}:{}, remote: {}:{}", false,
                    this.inDefinition.getHost(),
                    this.inDefinition.getPort(), this.outDefinition.getHost(), this.outDefinition.getPort());
            e.printStackTrace();
            return;
        }
        LOGGER.info("pipeline, isNormallyExited: {}: local: {}:{}, remote: {}:{}", isNormallyExited, this.inDefinition.getHost(),
                this.inDefinition.getPort(), this.outDefinition.getHost(), this.outDefinition.getPort());
    }
}
