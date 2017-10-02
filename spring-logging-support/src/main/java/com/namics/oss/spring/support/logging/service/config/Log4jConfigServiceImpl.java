/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.service.config;

import com.namics.oss.spring.support.logging.service.ConfigService;
import com.namics.oss.spring.support.logging.web.binding.LoggerBean;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

/**
 * LogbackConfigService.
 * 
 * @author aschaefer, Namics AG
 * @since 06.02.2013
 */
public class Log4jConfigServiceImpl implements ConfigService
{
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Log4jConfigServiceImpl.class);

	@Override
	public Set<LoggerBean> getLoggerInformation()
	{
		Set<LoggerBean> result = new TreeSet<LoggerBean>();

		result.add(this.mapLoggerbean(LogManager.getRootLogger()));

		@SuppressWarnings("unchecked")
		Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
		while (loggers.hasMoreElements())
		{
			Logger logger = loggers.nextElement();
			LoggerBean loggerBean = this.mapLoggerbean(logger);
			result.add(loggerBean);
		}

		return result;
	}

	@Override
	public Set<LoggerBean> changeLevel(String name,
									   String level)
	{
		Logger logger = LogManager.getLogger(name);
		Level last = logger.getEffectiveLevel();
		Level next = Level.toLevel(level, last);
		LOG.debug("Request level change of [{}] to [{}] perform change from [{}] to [{}]", new Object[] { name, level, last, next });
		logger.setLevel(next);
		return this.getLoggerInformation();
	}

	protected LoggerBean mapLoggerbean(Logger logger)
	{
		LoggerBean loggerBean = new LoggerBean();
		loggerBean.setName(logger.getName());
		loggerBean.setLevel(logger.getEffectiveLevel().toString());
		String parent = logger.getParent() != null ? logger.getParent().getName() : null;
		loggerBean.setParent(parent);
		return loggerBean;
	}
}
