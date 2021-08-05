package com.stormlin.hi.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 地址控制器
 *
 * @author lin-jinting
 */
@Slf4j
@RestController("AddressController")
@RequestMapping(value = "/address")
public class AddressController {
    private final HttpServletRequest req;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressController.class);

    public AddressController(HttpServletRequest request) {
        this.req = request;
    }

    /**
     * 返回用户网络地址
     *
     * @return 用户网络地址
     */
    @RequestMapping(value = "/echo")
    public String echo() {
        String result = String.format("%s:%s", req.getRemoteAddr(), req.getRemotePort());
        LOGGER.info(result);
        return result;
    }
}
