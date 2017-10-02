/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.service.config;

import ch.qos.logback.classic.LoggerContext;
import com.namics.oss.spring.support.logging.service.ConfigService;
import com.namics.oss.spring.support.logging.web.binding.LoggerBean;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;

/**
 * LogbackConfigService.
 *
 * @author aschaefer, Namics AG
 * @since 06.02.2013
 */
public class LogbackConfigServiceImpl implements ConfigService
{
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LogbackConfigServiceImpl.class);

	@Override
	public Set<LoggerBean> getLoggerInformation()
	{
		Set<LoggerBean> result = new TreeSet<LoggerBean>();

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		for (ch.qos.logback.classic.Logger log : lc.getLoggerList())
		{
			result.add(new LoggerBean()
							   .name(log.getName())
							   .level(log.getEffectiveLevel().toString()));
		}
		return result;
	}

	@Override
	public Set<LoggerBean> changeLevel(String name,
									   String level)
	{
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory().getLogger(name);
		ch.qos.logback.classic.Level last = logger.getEffectiveLevel();
		ch.qos.logback.classic.Level newLevel = ch.qos.logback.classic.Level.valueOf(level);
		LOG.debug("Request level change of [{}] to [{}] perform change from [{}] to [{}]", new Object[]{name, level, last, newLevel});
		logger.setLevel(newLevel);
		return this.getLoggerInformation();
	}

}
