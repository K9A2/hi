package com.stormlin.hi;

import com.stormlin.hi.node.ProxyNode;
import com.stormlin.hi.node.entity.PipelineDefinition;
import com.stormlin.hi.node.entity.ProxyDefinition;
import com.stormlin.hi.node.entity.TerminalDefinition;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主程序
 *
 * @author lin-jinting
 */
@SpringBootApplication
public class HiApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiApplication.class);
    private static final String PROTOCOL_HI_NAME = "hi";
    private static final int INDEX_CIPHER = 0;
    private static final int INDEX_PASSWORD = 1;

    public static void main(String[] args) {
        ProxyDefinition proxyDef;
        try {
            proxyDef = parseArgs(args);
        } catch (ParseException
                | UnknownSchemeException
                | URISyntaxException
                | WrongCipherOrPasswordException e) {
            LOGGER.error("error in parsing command args");
            e.printStackTrace();
            return;
        } catch (PrintHelpException e) {
            // 这个异常已经打印了说明，直接退出就行
            return;
        }

        // 运行 web 部分
        ConfigurableApplicationContext ctx = SpringApplication.run(HiApplication.class, args);

        // 运行代理部分
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(1);
        ProxyNode node = new ProxyNode(proxyDef, latch);
        executor.submit(node);

        LOGGER.info("web and proxy are all ready");

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.error("error in waiting executor to exit");
            e.printStackTrace();
            return;
        }

        LOGGER.info("executor exited");
        int exitCode = SpringApplication.exit(ctx, () -> 0);
        LOGGER.info("spring boot exited with code: {}", exitCode);
        System.exit(exitCode);
    }

    /**
     * 解析程序参数，返回程序定义
     *
     * @return 程序定义
     */
    public static ProxyDefinition parseArgs(String[] args)
            throws ParseException, PrintHelpException, URISyntaxException, UnknownSchemeException,
            WrongCipherOrPasswordException {
        // 程序参数定义
        Options optionGroup = new Options();

        Option helpDef = new Option("h", "help", false, "print program help");
        helpDef.setRequired(false);
        optionGroup.addOption(helpDef);

        Option inboundDef = new Option("i", "in", true, "inbound definition");
        inboundDef.setRequired(true);
        optionGroup.addOption(inboundDef);

        Option outboundDef = new Option("o", "out", true, "outbound definition");
        outboundDef.setRequired(true);
        optionGroup.addOption(outboundDef);

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(120);

        // 解析输入输出端口定义
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(optionGroup, args);
        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("hi", optionGroup, true);
            throw new PrintHelpException();
        }

        String inUrl = commandLine.getOptionValue("i");
        String outUrl = commandLine.getOptionValue("o");
        TerminalDefinition inTerminalDef = parseTerminal(inUrl);
        TerminalDefinition outTerminalDef = parseTerminal(outUrl);
        PipelineDefinition pipelineDefinition =
                new PipelineDefinition(inTerminalDef, outTerminalDef);
        LOGGER.info("read inbound terminal info: cipher: {}, password: {}, host: {}, port: {}",
                inTerminalDef.getCipher(), inTerminalDef.getPassword(), inTerminalDef.getHost(), inTerminalDef.getPort());
        LOGGER.info("read outbound terminal info: cipher: {}, password: {}, host: {}, port: {}",
                outTerminalDef.getCipher(), outTerminalDef.getPassword(), outTerminalDef.getHost(), outTerminalDef.getPort());
        return new ProxyDefinition(pipelineDefinition);
    }

    public static TerminalDefinition parseTerminal(String terminalUrl)
            throws UnknownSchemeException, URISyntaxException, WrongCipherOrPasswordException {
        URI uri = new URI(terminalUrl);
        // 协议名称检查
        if (!uri.getScheme().equalsIgnoreCase(PROTOCOL_HI_NAME)) {
            throw new UnknownSchemeException();
        }
        if (StringUtils.isEmpty(uri.getUserInfo())) {
            // 明文传输
            return new TerminalDefinition("", "", uri.getHost(), uri.getPort());
        }

        // 加密方法和密码检查
        if (!uri.getUserInfo().contains(":")) {
            throw new WrongCipherOrPasswordException();
        }
        // 加密方法和密码都必须指定
        String[] encryption = uri.getUserInfo().split(":");
        for (String s : encryption) {
            if (StringUtils.isBlank(s)) {
                throw new WrongCipherOrPasswordException();
            }
        }
        return new TerminalDefinition(
                encryption[INDEX_CIPHER], encryption[INDEX_PASSWORD], uri.getHost(), uri.getPort());
    }
}
