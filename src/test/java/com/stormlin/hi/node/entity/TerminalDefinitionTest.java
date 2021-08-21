package com.stormlin.hi.node.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TerminalDefinitionTest {
    @Test
    void testToString() {
        String protocolHi = "hi";
        String protocolTcp = "tcp";
        String protocolTls = "tls";
        String cipher = "AES_256_GCM";
        String password = "12345678";
        String host = "localhost";
        int port = 8488;

        String encryptedExample = "hi://AES_256_GCM:12345678@localhost:8488";
        String plainTextExample = "hi://localhost:8488";
        String tcpExample = "tcp://AES_256_GCM:12345678@localhost:8488";
        String tlsExample = "tls://AES_256_GCM:12345678@localhost:8488";

        TerminalDefinition encryptedTerminal = new TerminalDefinition(protocolHi, cipher, password, host, port);
        Assertions.assertTrue(encryptedTerminal.toString().equalsIgnoreCase(encryptedExample));

        TerminalDefinition plainTextTerminal = new TerminalDefinition(protocolHi, host, port);
        Assertions.assertTrue(plainTextTerminal.toString().equalsIgnoreCase(plainTextExample));

        TerminalDefinition tcpTerminal = new TerminalDefinition(protocolTcp, cipher, password, host, port);
        Assertions.assertTrue(tcpTerminal.toString().equalsIgnoreCase(tcpExample));

        TerminalDefinition tlsTerminal = new TerminalDefinition(protocolTls, cipher, password, host, port);
        Assertions.assertTrue(tlsTerminal.toString().equalsIgnoreCase(tlsExample));
    }
}
