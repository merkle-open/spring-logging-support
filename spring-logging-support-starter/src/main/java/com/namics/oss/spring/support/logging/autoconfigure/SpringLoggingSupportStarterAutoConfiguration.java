package com.namics.oss.spring.support.logging.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * SpringLoggingSupportStarterAutoConfiguration.
 *
 * @author bhelfenberger, Namics AG
 * @since 22.09.17 14:43
 */
@Configuration
@Import({ SpringLoggingSupportWebAutoConfiguration.class})
public class SpringLoggingSupportStarterAutoConfiguration implements Ordered {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
