package com.stormlin.hi.node.entity;

import lombok.Data;

/**
 * 节点终端定义
 *
 * @author lin-jinting
 */
@Data
public class TerminalDefinition {
    private String cipher;
    private String password;
    private String host;
    private Integer port;

    public TerminalDefinition(String cipher, String password, String host, Integer port) {
        this.cipher = cipher;
        this.password = password;
        this.host = host;
        this.port = port;
    }
}
