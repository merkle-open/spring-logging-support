/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.web;

import org.springframework.web.context.request.async.DeferredResult.DeferredResultHandler;

/**
 * LegacyDeferredResultHandler.
 * 
 * @author aschaefer, Namics AG
 * @since 12.02.2013
 */
public class LegacyDeferredResultHandler<T> implements DeferredResultHandler, Runnable
{
	private T result = null;
	private boolean stop = false;

	/** {@inheritDoc} */
	@Override
	public void handleResult(Object result)
	{
		try
		{
			this.result = (T) result;
		}
		catch (ClassCastException e)
		{

		}
		this.stop = true;
	}

	/** {@inheritDoc} */
	@Override
	public void run()
	{
		try
		{
			while (!this.stop)
			{
				Thread.sleep(500);
			}
		}
		catch (InterruptedException e)
		{

		}
	}

	/**
	 * Indicate to this runnable to finish.
	 * 
	 */
	public void stop()
	{
		this.stop = true;
	}

	/** Getter for result. @return the result */
	public T getResult()
	{
		return this.result;
	}

}
