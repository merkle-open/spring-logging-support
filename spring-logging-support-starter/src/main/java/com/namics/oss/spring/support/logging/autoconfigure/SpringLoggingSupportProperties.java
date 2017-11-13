package com.namics.oss.spring.support.logging.autoconfigure;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SpringLoggingSupportProperties.
 *
 * @author bhelfenberger, Namics AG
 * @since 22.09.17 15:01
 */
@ConfigurationProperties(prefix = SpringLoggingSupportProperties.SPRING_LOGGING_SUPPORT_PROPERTIES_PREFIX)
public class SpringLoggingSupportProperties {

	public static final String SPRING_LOGGING_SUPPORT_PROPERTIES_PREFIX = "com.namics.oss.spring.support.logging";

	private Web web = new Web();

	public Web getWeb() {
		return web;
	}

	public void setWeb(Web web) {
		this.web = web;
	}

	public static class Web {

		public static final String SPRING_LOGGING_SUPPORT_PROPERTIES_WEB = SPRING_LOGGING_SUPPORT_PROPERTIES_PREFIX + ".web";

		private String servletName;

		private String servletMapping;

		public String getServletName() {
			return servletName;
		}

		public void setServletName(String servletName) {
			this.servletName = servletName;
		}

		public String getServletMapping() {
			return servletMapping;
		}

		public void setServletMapping(String servletMapping) {
			this.servletMapping = servletMapping;
		}
	}

}
