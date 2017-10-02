/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.web;

import com.namics.oss.spring.support.logging.service.ConfigService;
import com.namics.oss.spring.support.logging.web.binding.LoggerBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

/**
 * LogConfigController.
 * 
 * @author aschaefer, Namics AG
 * @since 06.02.2013
 */
@RequestMapping("/config")
public class LogConfigController
{
	protected ConfigService configService;

	public LogConfigController(ConfigService configService)
	{
		this.configService = configService;
	}

	@RequestMapping(value = "/loggers", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Set<LoggerBean> getLoggerInformation()
	{
		return this.configService.getLoggerInformation();
	}

	@RequestMapping(value = "/level", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Set<LoggerBean> changeLevel(	@RequestParam String name,
										@RequestParam String level)
	{
		this.configService.changeLevel(name,level);
		return this.getLoggerInformation();
	}

}
