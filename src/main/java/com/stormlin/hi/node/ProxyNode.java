package com.stormlin.hi.node;

import com.stormlin.hi.node.entity.ProxyDefinition;
import com.stormlin.hi.node.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 代理节点
 *
 * @author lin-jinting
 */
public class ProxyNode implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyNode.class);
    /**
     * 该代理节点正在运行的数据流水线，含一入站节点和一出站节点
     */
    private final List<Pipeline> pipelines;

    private final CountDownLatch latch;
    private final ExecutorService executor;

    public ProxyNode(ProxyDefinition definition, CountDownLatch latch) {
        this.pipelines = new ArrayList<>();
        definition.getPipelineDefinitionList().forEach(p -> this.pipelines.add(new Pipeline(p.in, p.out)));
        this.latch = latch;
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        this.pipelines.forEach(this.executor::submit);
        this.executor.shutdown();
        boolean isExited;
        try {
            isExited = this.executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("exception during awaiting thread exit");
            e.printStackTrace();
            return;
        }
        this.latch.countDown();
        LOGGER.info("proxy node exit with code: {}", isExited);
    }
}
