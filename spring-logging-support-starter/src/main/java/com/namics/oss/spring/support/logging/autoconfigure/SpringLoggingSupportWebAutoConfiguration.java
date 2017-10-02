package com.namics.oss.spring.support.logging.autoconfigure;

import com.namics.oss.spring.support.logging.LoggingConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.inject.Inject;

import static org.springframework.util.StringUtils.hasText;

/**
 * SpringLoggingSupportWebAutoConfiguration.
 *
 * @author bhelfenberger, Namics AG
 * @since 22.09.17 14:53
 */
@Configuration
@ConditionalOnClass(LoggingConfig.class)
@EnableConfigurationProperties(SpringLoggingSupportProperties.class)
public class SpringLoggingSupportWebAutoConfiguration {

	protected static final String defaultServletMapping = "/nx-log/*";
	protected static final String defaultServletName = "loggingServlet";

	@Inject
	protected SpringLoggingSupportProperties springLoggingSupportProperties;


	@Bean
	public ServletRegistrationBean configurationServlet() {

		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(LoggingConfig.class);

		DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet.setApplicationContext(applicationContext);

		ServletRegistrationBean registrationBean = new ServletRegistrationBean(dispatcherServlet, getServletMapping());
		registrationBean.setName(getServletName());
		registrationBean.setLoadOnStartup(1);
		return registrationBean;
	}

	protected String getServletMapping() {
		if (hasText(springLoggingSupportProperties.getWeb().getServletMapping())) {
			return springLoggingSupportProperties.getWeb().getServletMapping();
		}
		return defaultServletMapping;
	}

	protected String getServletName() {
		if (hasText(springLoggingSupportProperties.getWeb().getServletName())) {
			return springLoggingSupportProperties.getWeb().getServletName();
		}
		return defaultServletName;
	}

}
