/*
 * Copyright 2000-2014 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging;

import com.namics.oss.spring.support.logging.service.ConfigService;
import com.namics.oss.spring.support.logging.service.TailService;
import com.namics.oss.spring.support.logging.service.config.Log4jConfigServiceImpl;
import com.namics.oss.spring.support.logging.service.config.LogbackConfigServiceImpl;
import com.namics.oss.spring.support.logging.service.tail.TailServiceImpl;
import com.namics.oss.spring.support.logging.web.LogConfigController;
import com.namics.oss.spring.support.logging.web.LogfilesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.inject.Named;
import java.io.IOException;

/**
 * LoggingConfig.
 *
 * @author aschaefer, Namics AG
 * @since 24.03.14 13:28
 */
@Configuration
@EnableAsync
@EnableScheduling
public class LoggingConfig extends WebMvcConfigurationSupport
{
	private static final Logger LOG = LoggerFactory.getLogger(LoggingConfig.class);

	@Override
	protected void addViewControllers(ViewControllerRegistry registry)
	{
		super.addViewControllers(registry);
		registry.addViewController("/").setViewName("redirect:logging.html");
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		super.addResourceHandlers(registry);
		registry.addResourceHandler("/*.html").addResourceLocations("classpath:/META-INF/nxlog/terrific/assets/");
		registry.addResourceHandler("/**/*.html").addResourceLocations("classpath:/META-INF/nxlog/terrific/");
		registry.addResourceHandler("/**/*.css", "/**/*.js").addResourceLocations("classpath:/META-INF/nxlog/terrific/");
	}

	@Override
	protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer)
	{
		configurer.enable();
	}

	@Bean(name = "nxlogLogfilesController")
	public LogfilesController logfilesController(@Named("nxlogBasePath") Resource basePath) throws IOException
	{
		return new LogfilesController(basePath, tailService(basePath));
	}

	@Bean(name = "nxlogLog4jConfigController")
	public LogConfigController log4jConfigController()
	{
		return new LogConfigController(configService());
	}

	@Bean(name = "nxlogConfigService")
	public ConfigService configService(){
		boolean log4j = checkDependency("org.apache.log4j.LogManager");
		boolean logback = checkDependency("ch.qos.logback.classic.Logger");
		if ( logback) {
			LOG.info("Use logback implementation");
			return new LogbackConfigServiceImpl();
		} else if (log4j ){
			LOG.info("Use log4j implementation");
			return new Log4jConfigServiceImpl();
		}
		throw new IllegalArgumentException("No logging framework detected. Supported frameworks: log4j, logback");
	}

	protected boolean checkDependency(String dependency)
	{
		try
		{
			Class.forName(dependency);
		} catch (Throwable e)
		{
			return false;
		}
		return true;
	}

	@Bean(name = "nxlogTailService")
	public TailService tailService(@Named("nxlogBasePath") Resource basePath)
	{
		TailServiceImpl service = new TailServiceImpl(basePath);
		return service;
	}

	@Bean(name = "nxlogBasePath")
	public Resource basePath(Environment environment, ApplicationContext context)
	{
		String catalinaBase = environment.getProperty("catalina.base","");
		String path = environment.getProperty("nxlog.base.path", "file:" + catalinaBase + "/logs/");
		return context.getResource(path);
	}


}
