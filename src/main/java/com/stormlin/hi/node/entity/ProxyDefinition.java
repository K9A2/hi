package com.stormlin.hi.node.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理定义
 *
 * @author lin-jinting
 */
@Data
public class ProxyDefinition {
    List<PipelineDefinition> pipelineDefinitionList;

    public ProxyDefinition(PipelineDefinition definition) {
        this.pipelineDefinitionList = new ArrayList<>();
        this.pipelineDefinitionList.add(definition);
    }
}
