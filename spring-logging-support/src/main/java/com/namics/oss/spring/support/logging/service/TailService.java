/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.service;

import java.util.Collection;

import org.springframework.web.context.request.async.DeferredResult;

/**
 * TailService.
 * 
 * @author aschaefer, Namics AG
 * @since 07.02.2013
 */
public interface TailService
{

	/**
	 * Register for a tail step.
	 * 
	 * @param filename
	 *            file to tail
	 * @param start
	 *            start watching the file from this amount of lines before end of file.
	 * @return DeferredResult to be handled by Servlet 3.0 to refer an async result of lines in file
	 */
	public DeferredResult<Collection<String>> tail(	String filename,
													long start) throws Exception;

}