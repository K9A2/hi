package com.stormlin.hi;

import com.stormlin.hi.node.entity.TerminalDefinition;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URISyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class HiApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiApplicationTests.class);

    @Test
    public void testParseTerminal() {
        String encryptedExample = "hi://AES_256_GCM:12345678@localhost:8488";
        String plainTextExample = "hi://localhost:8488";
        String malformedExample = "hi://AES_256_GCM:12345678@localhost:8488!!!!!";

        try {
            TerminalDefinition encryptedTerminal = HiApplication.parseTerminal(encryptedExample);
            Assertions.assertTrue(encryptedTerminal.getCipher().equalsIgnoreCase("AES_256_GCM"));
            Assertions.assertTrue(encryptedTerminal.getPassword().equalsIgnoreCase("12345678"));
            Assertions.assertTrue(encryptedTerminal.getHost().equalsIgnoreCase("localhost"));
            Assertions.assertEquals(8488, (int) encryptedTerminal.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
            return;
        }

        try {
            TerminalDefinition plainTestTerminal = HiApplication.parseTerminal(plainTextExample);
            if (!StringUtils.isEmpty(plainTestTerminal.getCipher())
                    || !StringUtils.isEmpty(plainTestTerminal.getPassword())) {
                Assertions.fail();
                return;
            }
            Assertions.assertTrue(plainTestTerminal.getHost().equalsIgnoreCase("localhost"));
            Assertions.assertEquals(8488, (int) plainTestTerminal.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
            return;
        }

        try {
            HiApplication.parseTerminal(malformedExample);
        } catch (URISyntaxException e) {
            LOGGER.info("catch malformed url exception");
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }
}
