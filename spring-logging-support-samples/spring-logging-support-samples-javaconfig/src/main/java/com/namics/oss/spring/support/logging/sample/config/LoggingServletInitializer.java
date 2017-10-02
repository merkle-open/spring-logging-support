/*
 * Copyright 2000-2014 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.sample.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * LoggingServletInitializer.
 *
 * @author aschaefer, Namics AG
 * @since 24.03.14 13:25
 */
public class LoggingServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer
{
	@Override
	protected Class<?>[] getRootConfigClasses()
	{
		return new Class<?>[0];
	}

	@Override
	protected Class<?>[] getServletConfigClasses()
	{
		return new Class<?>[]{ LoggingServletConfig.class};
	}

	@Override
	protected String[] getServletMappings()
	{
		return new String[]{"/nx-log/*"};
	}
}
