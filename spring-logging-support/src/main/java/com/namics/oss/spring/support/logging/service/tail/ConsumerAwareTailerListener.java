/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.service.tail;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * ConsumerAwareTailerListener imlements TailerListener and writes lines to a buffer that is consumed by Deferred consumers.
 * 
 * @author aschaefer, Namics AG
 * @since 07.02.2013
 */
public class ConsumerAwareTailerListener implements TailerListener
{
	private static final Logger LOG = LoggerFactory.getLogger(ConsumerAwareTailerListener.class);

	private final Queue<DeferredResult<Collection<String>>> consumers = new ConcurrentLinkedQueue<DeferredResult<Collection<String>>>();

	private final Queue<String> lineBuffer = new ConcurrentLinkedQueue<String>();

	private Tailer tailer;

	@Override
	public void init(Tailer tailer)
	{
		this.tailer = tailer;
	}

	/**
	 * Stop and cleanup itself!.
	 * 
	 */
	public void stop()
	{
		LOG.info("Stop Tailer");
		if (this.tailer != null)
		{
			this.tailer.stop();
		}
		this.tailer = null;
	}

	/**
	 * Returns current length of consumer queue.
	 * 
	 * @return current length of consumer queue
	 */
	public int consumerCount()
	{
		int count = this.consumers.size();
		// LOG.debug("Consumers listening {}", count);
		return count;
	}

	/**
	 * Register a new consumer to the listener.
	 * 
	 * @param consumer
	 *            consumer to be notified with lines
	 */
	public void register(DeferredResult<Collection<String>> consumer)
	{
		this.consumers.add(consumer);
		this.consumerCount();
	}

	/**
	 * Read all queued lines from buffer and notify registered consumers.
	 * 
	 */
	public void consume()
	{
		// LOG.debug("Consum queued lines");
		if (this.consumerCount() > 0)
		{
			Collection<String> currentLines = new LinkedList<String>();
			// sync to avoid filling up while beeing processed -> consistence
			synchronized (this.lineBuffer)
			{
				String line = this.lineBuffer.poll();
				while (line != null)
				{
					currentLines.add(line);
					line = this.lineBuffer.poll();
				}
			}

			// queue starts filling again, provide result to current consumers!
			DeferredResult<Collection<String>> consumer = this.consumers.poll();
			while (consumer != null)
			{
				consumer.setResult(currentLines);
				consumer = this.consumers.poll();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void handle(String line)
	{
		// LOG.debug("Add line to buffer: {}", line);
		synchronized (this.lineBuffer)
		{
			this.lineBuffer.add(line);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void handle(Exception ex)
	{
		LOG.warn("Exception in Tailer process {}", new Object[] { ex });
	}

	/** {@inheritDoc} */
	@Override
	public void fileRotated()
	{
	}

	/** {@inheritDoc} */
	@Override
	public void fileNotFound()
	{
		LOG.warn("File not found for Tailer process");
	}

}