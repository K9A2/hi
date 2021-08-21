package com.stormlin.hi.node.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 节点终端定义
 *
 * @author lin-jinting
 */
@Data
public class TerminalDefinition {
    private static final String ENCRYPTED_TEMPLATE = "%s://%s:%s@%s:%s";
    private static final String PLAIN_TEXT_TEMPLATE = "%s://%s:%s";
    private String scheme;
    private String cipher;
    private String password;
    private String host;
    private Integer port;

    public TerminalDefinition(String scheme, String cipher, String password, String host, Integer port) {
        this.scheme = scheme;
        this.cipher = cipher;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public TerminalDefinition(String scheme, String host, Integer port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return StringUtils.isEmpty(this.cipher) || StringUtils.isEmpty(this.password) ?
                String.format(PLAIN_TEXT_TEMPLATE, this.scheme, this.host, this.port) :
                String.format(ENCRYPTED_TEMPLATE, this.scheme, this.cipher, this.password, this.host, this.port);
    }
}
