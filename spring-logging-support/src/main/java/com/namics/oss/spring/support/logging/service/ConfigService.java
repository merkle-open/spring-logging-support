/*
 * Copyright 2000-2014 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.service;

import com.namics.oss.spring.support.logging.web.binding.LoggerBean;

import java.util.Set;

/**
 * ConfigService.
 *
 * @author aschaefer, Namics AG
 * @since 23.06.14 15:20
 */
public interface ConfigService
{
	Set<LoggerBean> getLoggerInformation();

	Set<LoggerBean> changeLevel(String name,
								String level);
}
