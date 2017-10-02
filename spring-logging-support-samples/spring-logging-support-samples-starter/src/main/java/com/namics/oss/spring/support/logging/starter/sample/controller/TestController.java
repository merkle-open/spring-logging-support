package com.namics.oss.spring.support.logging.starter.sample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * TestController.
 *
 * @author bhelfenberger, Namics AG
 * @since 22.09.17 16:31
 */
@Controller
public class TestController {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String get() {
		return "logging";
	}

}
