package com.stormlin.hi.node.entity;

/**
 * 代理流水线定义类
 *
 * @author lin-jinting
 */
public class PipelineDefinition {
    public TerminalDefinition in;
    public TerminalDefinition out;

    public PipelineDefinition(TerminalDefinition in, TerminalDefinition out) {
        this.in = in;
        this.out = out;
    }
}
