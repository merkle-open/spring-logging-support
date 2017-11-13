/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.service.tail;

import com.namics.oss.spring.support.logging.service.TailService;
import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * TailService.
 *
 * @author aschaefer, Namics AG
 * @since 07.02.2013
 */
public class TailServiceImpl implements TailService
{
	private static final Logger LOG = LoggerFactory.getLogger(TailServiceImpl.class);

	/**
	 * Active listeners.
	 */
	private final Map<String, ConsumerAwareTailerListener> listeners = new HashMap<String, ConsumerAwareTailerListener>();

	/**
	 * saves the basePath where the logfiles are located
	 */
	protected Resource basePath;

	public TailServiceImpl()
	{
	}

	public TailServiceImpl(Resource basePath)
	{
		this.basePath = basePath;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException problem with file to tail
	 */
	@Override
	public DeferredResult<Collection<String>> tail(String filename,
												   long start) throws IOException
	{
		DeferredResult<Collection<String>> result = new DeferredResult<Collection<String>>();
		ConsumerAwareTailerListener listener = null;

		synchronized (this.listeners)
		{
			LOG.debug("Tail file {}/{}", this.basePath.getFile(), filename);
			// retrieve listerner
			listener = this.listeners.get(filename);

			// no existing listener for filename
			if (listener == null || start > 0)
			{
				if (listener != null)
				{
					listener.consume();
					listener.stop();
					listener = null;
				}

				// create and start a Tailer for the file
				File file = new File(this.basePath.getFile() + "/" + filename);

				// create and refer a listener
				listener = new ConsumerAwareTailerListener();
				this.listeners.put(filename, listener);

				Tailer tailer = null;
				if (start > 0)
				{
					tailer = new LookbackTailer(file, listener, 100l, start);
				}
				else
				{
					tailer = new LookbackTailer(file, listener, 100l, true, 0);
				}

				new Thread(tailer).start();
				// no references left, except of listener! @see maintain()
			}
		}
		// register consumer
		listener.register(result);
		return result;
	}

	/**
	 * Trigger tail results processing.
	 */
	@Scheduled(fixedRate = 1000)
	public void scheduledConsum()
	{
		synchronized (this.listeners)
		{
			if (this.listeners.size() > 0)
			{
				for (ConsumerAwareTailerListener listener : this.listeners.values())
				{
					listener.consume();
				}
			}
		}
	}

	/**
	 * Maintains unused listeners.
	 */
	@Scheduled(fixedRate = 60000)
	public void maintain()
	{
		LOG.debug("Scheduled maintenance");
		// prevent influence of new creation in the meantime
		synchronized (this.listeners)
		{
			Set<String> deleteCandidates = new HashSet<String>();
			for (Entry<String, ConsumerAwareTailerListener> entry : this.listeners.entrySet())
			{
				final int count = entry.getValue().consumerCount();
				if (count == 0)
				{
					LOG.debug("Listener for file {} has {} consumers: stop tailer and remove listener", entry.getKey(), count);
					deleteCandidates.add(entry.getKey());
					entry.getValue().stop(); // stops Tailer Thread
				}
				else
				{
					LOG.debug("Listener for file {} has {} consumers", entry.getKey(), count);
				}
			}
			for (String delete : deleteCandidates)
			{
				this.listeners.remove(delete); // kills remaining reference to listerner -> free for GC
			}
		}
	}

	public void setBasePath(Resource basePath)
	{
		this.basePath = basePath;
	}
}
