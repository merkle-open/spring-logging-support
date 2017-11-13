/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.service.tail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of the unix "tail -100f" functionality.
 * <p>
 * <h2>1. Create a TailerListener implementation</h3>
 * <p>
 * First you need to create a {@link TailerListener} implementation ({@link TailerListenerAdapter} is provided for convenience so that you don't have
 * to implement every method).
 * </p>
 * 
 * <p>
 * For example:
 * </p>
 * 
 * <pre>
 * public class MyTailerListener extends TailerListenerAdapter
 * {
 * 	public void handle(String line)
 * 	{
 * 		System.out.println(line);
 * 	}
 * }
 * </pre>
 * 
 * <h2>2. Using a Tailer</h2>
 * 
 * You can create and use a Tailer in one of three ways:
 * <ul>
 * <li>Using one of the static helper methods:
 * <ul>
 * <li>{@link Tailer#create(File, TailerListener)}</li>
 * <li>{@link Tailer#create(File, TailerListener, long)}</li>
 * <li>{@link Tailer#create(File, TailerListener, long, boolean)}</li>
 * </ul>
 * </li>
 * <li>Using an {@link java.util.concurrent.Executor}</li>
 * <li>Using an {@link Thread}</li>
 * </ul>
 * 
 * An example of each of these is shown below.
 * 
 * <h3>2.1 Using the static helper method</h3>
 * 
 * <pre>
 * TailerListener listener = new MyTailerListener();
 * Tailer tailer = Tailer.create(file, listener, delay);
 * </pre>
 * 
 * <h3>2.2 Use an Executor</h3>
 * 
 * <pre>
 * TailerListener listener = new MyTailerListener();
 * Tailer tailer = new Tailer(file, listener, delay);
 * 
 * // stupid executor impl. for demo purposes
 * Executor executor = new Executor()
 * {
 * 	public void execute(Runnable command)
 * 	{
 * 		command.run();
 * 	}
 * };
 * 
 * executor.execute(tailer);
 * </pre>
 * 
 * 
 * <h3>2.3 Use a Thread</h3>
 * 
 * <pre>
 * TailerListener listener = new MyTailerListener();
 * Tailer tailer = new Tailer(file, listener, delay);
 * Thread thread = new Thread(tailer);
 * thread.setDaemon(true); // optional
 * thread.start();
 * </pre>
 * 
 * <h2>3. Stop Tailing</h3>
 * <p>
 * Remember to stop the tailer when you have done with it:
 * </p>
 * 
 * <pre>
 * tailer.stop();
 * </pre>
 * 
 * @see TailerListener
 * @see TailerListenerAdapter
 * 
 * @author aschaefer, Namics AG
 * @since 08.02.2013
 */
public class LookbackTailer extends Tailer // preserve interface!
{

	private static final Logger LOG = LoggerFactory.getLogger(LookbackTailer.class);

	public static final int DEFAULT_DELAY_MILLIS = 1000;

	private static final String RAF_MODE = "r";

	public static final int DEFAULT_BUFSIZE = 4096;

	/**
	 * Buffer on top of RandomAccessFile.
	 */
	private final byte inbuf[];

	/**
	 * The file which will be tailed.
	 */
	private final File file;

	/**
	 * The amount of time to wait for the file to be updated.
	 */
	private final long delayMillis;

	/**
	 * Whether to tail from the end or start of file
	 */
	private final boolean end;

	/** Tail from this amount of lines backwards (tail -n start -f file ). */
	private final long start;

	/**
	 * The listener to notify of events when tailing.
	 */
	private final TailerListener listener;

	/**
	 * Whether to close and reopen the file whilst waiting for more input.
	 */
	private final boolean reOpen;

	/**
	 * The tailer will run as long as this value is true.
	 */
	private volatile boolean run = true;

	/** The last time the file was checked for changes. */
	private volatile long last = 0;

	/** position within the file. */
	private volatile long position = 0;

	/**
	 * Creates a Tailer for the given file, starting from the beginning, with the default delay of 1.0s.
	 * 
	 * @param file
	 *            The file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 */
	public LookbackTailer(File file, TailerListener listener, long start)
	{
		this(file, listener, DEFAULT_DELAY_MILLIS, start);
	}

	/**
	 * Creates a Tailer for the given file, with a delay other than the default 1.0s.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 */
	public LookbackTailer(File file, TailerListener listener, long delayMillis, boolean end, long start)
	{
		this(file, listener, delayMillis, end, DEFAULT_BUFSIZE, start);
	}

	/**
	 * Creates a Tailer for the given file, with a delay other than the default 1.0s.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param start
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 */
	public LookbackTailer(File file, TailerListener listener, long delayMillis, long start)
	{
		this(file, listener, delayMillis, false, start);

	}

	/**
	 * Creates a Tailer for the given file, with a delay other than the default 1.0s.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 * @param reOpen
	 *            if true, close and reopen the file between reading chunks
	 */
	public LookbackTailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, long start)
	{
		this(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE, start);
	}

	/**
	 * Creates a Tailer for the given file, with a specified buffer size.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 * @param bufSize
	 *            Buffer size
	 */
	public LookbackTailer(File file, TailerListener listener, long delayMillis, boolean end, int bufSize, long start)
	{
		this(file, listener, delayMillis, end, false, bufSize, start);
	}

	/**
	 * Creates a Tailer for the given file, with a specified buffer size.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 * @param reOpen
	 *            if true, close and reopen the file between reading chunks
	 * @param bufSize
	 *            Buffer size
	 */
	public LookbackTailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize, long start)
	{
		super(file, listener, delayMillis, end, reOpen, bufSize);
		this.file = file;
		this.delayMillis = delayMillis;
		this.end = end;
		this.start = start;
		this.inbuf = new byte[bufSize];
		// Save and prepare the listener
		this.listener = listener;
		listener.init(this);
		this.reOpen = reOpen;
	}

	/**
	 * Creates and starts a Tailer for the given file.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 * @param bufSize
	 *            buffer size.
	 * @return The new tailer
	 */
	public static LookbackTailer create(File file,
										TailerListener listener,
										long delayMillis,
										boolean end,
										int bufSize)
	{
		LookbackTailer tailer = new LookbackTailer(file, listener, delayMillis, end, bufSize);
		Thread thread = new Thread(tailer);
		thread.setDaemon(true);
		thread.start();
		return tailer;
	}

	/**
	 * Creates and starts a Tailer for the given file.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 * @param reOpen
	 *            whether to close/reopen the file between chunks
	 * @param bufSize
	 *            buffer size.
	 * @return The new tailer
	 */
	public static LookbackTailer create(File file,
										TailerListener listener,
										long delayMillis,
										boolean end,
										boolean reOpen,
										int bufSize,
										long start)
	{
		LookbackTailer tailer = new LookbackTailer(file, listener, delayMillis, end, reOpen, bufSize, start);
		Thread thread = new Thread(tailer);
		thread.setDaemon(true);
		thread.start();
		return tailer;
	}

	/**
	 * Creates and starts a Tailer for the given file with default buffer size.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 * @return The new tailer
	 */
	public static LookbackTailer create(File file,
										TailerListener listener,
										long delayMillis,
										boolean end)
	{
		return create(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
	}

	/**
	 * Creates and starts a Tailer for the given file with default buffer size.
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @param end
	 *            Set to true to tail from the end of the file, false to tail from the beginning of the file.
	 * @param reOpen
	 *            whether to close/reopen the file between chunks
	 * @return The new tailer
	 */
	public static LookbackTailer create(File file,
										TailerListener listener,
										long delayMillis,
										boolean end,
										boolean reOpen,
										long start)
	{
		return create(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE, start);
	}

	/**
	 * Creates and starts a Tailer for the given file, starting at the beginning of the file
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @param delayMillis
	 *            the delay between checks of the file for new content in milliseconds.
	 * @return The new tailer
	 */
	public static LookbackTailer create(File file,
										TailerListener listener,
										long delayMillis)
	{
		return create(file, listener, delayMillis, false);
	}

	/**
	 * Creates and starts a Tailer for the given file, starting at the beginning of the file
	 * with the default delay of 1.0s
	 * 
	 * @param file
	 *            the file to follow.
	 * @param listener
	 *            the TailerListener to use.
	 * @return The new tailer
	 */
	public static LookbackTailer create(File file,
										TailerListener listener)
	{
		return create(file, listener, DEFAULT_DELAY_MILLIS, false);
	}

	/**
	 * Return the file.
	 * 
	 * @return the file
	 */
	@Override
	public File getFile()
	{
		return this.file;
	}

	/**
	 * Return the delay in milliseconds.
	 * 
	 * @return the delay in milliseconds.
	 */
	@Override
	public long getDelay()
	{
		return this.delayMillis;
	}

	/**
	 * Follows changes in the file, calling the TailerListener's handle method for each new line.
	 */
	@Override
	public void run()
	{
		RandomAccessFile reader = null;
		try
		{
			reader = this.initializeReader(reader);
			reader = this.tailFile(reader);
		}
		catch (Exception e)
		{
			this.listener.handle(e);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}
	}

	/**
	 * Allows the tailer to complete its current loop and return.
	 */
	@Override
	public void stop()
	{
		super.stop();
		this.run = false;
	}

	protected RandomAccessFile initializeReader(RandomAccessFile reader) throws IOException
	{
		this.last = 0;
		this.position = 0;

		// Open the file
		while (this.run && reader == null)
		{
			try
			{
				reader = new RandomAccessFile(this.file, RAF_MODE);
			}
			catch (FileNotFoundException e)
			{
				this.listener.fileNotFound();
			}

			if (reader == null)
			{
				try
				{
					Thread.sleep(this.delayMillis);
				}
				catch (InterruptedException e)
				{
				}
			}
			else
			{
				// The current position in the file
				if (this.end)
				{
					LOG.debug("tail from the end.");
					this.position = this.file.length();
				}
				else
				{
					this.position = this.start > 0 ? this.getOffset(this.start, this.file) : 0;
					LOG.debug("tail -n {} -f {} : position {} ", new Object[] { this.start, this.file, this.position });
				}
				this.last = System.currentTimeMillis();
				reader.seek(this.position);
			}
		}

		return reader;
	}

	protected long getOffset(	long lines,
								File file) throws IOException
	{
		FileInputStream inputStream = new FileInputStream(file);
		FileChannel fileChannel = inputStream.getChannel();
		long size = fileChannel.size();
		ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
		IOUtils.closeQuietly(inputStream);

		long cnt = 0;
		long offset = 0;
		for (offset = size - 1; offset >= 0; offset--)
		{
			if (buffer.get((int) offset) == '\n')
			{
				cnt++;
				if (cnt == lines + 1)
				{
					break;
				}
			}
		}
		return offset + 1;
	}

	protected RandomAccessFile tailFile(RandomAccessFile reader) throws IOException, FileNotFoundException
	{
		while (this.run)
		{

			boolean newer = FileUtils.isFileNewer(this.file, this.last); // IO-279, must be done first

			// Check the file length to see if it was rotated
			long length = this.file.length();

			if (length < this.position)
			{

				// File was rotated
				this.listener.fileRotated();

				// Reopen the reader after rotation
				try
				{
					// Ensure that the old file is closed iff we re-open it successfully
					RandomAccessFile save = reader;
					reader = new RandomAccessFile(this.file, RAF_MODE);
					this.position = 0;
					// close old file explicitly rather than relying on GC picking up previous RAF
					IOUtils.closeQuietly(save);
				}
				catch (FileNotFoundException e)
				{
					// in this case we continue to use the previous reader and position values
					this.listener.fileNotFound();
				}
				continue;
			}
			else
			{

				// File was not rotated

				// See if the file needs to be read again
				if (length > this.position)
				{

					// The file has more content than it did last time
					this.position = this.readLines(reader);
					this.last = System.currentTimeMillis();

				}
				else if (newer)
				{

					/*
					 * This can happen if the file is truncated or overwritten with the exact same length of
					 * information. In cases like this, the file position needs to be reset
					 */
					this.position = 0;
					reader.seek(this.position); // cannot be null here

					// Now we can read new lines
					this.position = this.readLines(reader);
					this.last = System.currentTimeMillis();
				}
			}
			if (this.reOpen)
			{
				IOUtils.closeQuietly(reader);
			}
			try
			{
				Thread.sleep(this.delayMillis);
			}
			catch (InterruptedException e)
			{
			}
			if (this.run && this.reOpen)
			{
				reader = new RandomAccessFile(this.file, RAF_MODE);
				reader.seek(this.position);
			}
		}
		return reader;
	}

	/**
	 * Read new lines.
	 * 
	 * @param reader
	 *            The file to read
	 * @return The new position after the lines have been read
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 */
	protected long readLines(RandomAccessFile reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		long pos = reader.getFilePointer();
		long rePos = pos; // position to re-read

		int num;
		boolean seenCR = false;
		while (this.run && (num = reader.read(this.inbuf)) != -1)
		{
			for (int i = 0; i < num; i++)
			{
				byte ch = this.inbuf[i];
				switch (ch)
				{
				case '\n':
					seenCR = false; // swallow CR before LF
					this.listener.handle(sb.toString());
					sb.setLength(0);
					rePos = pos + i + 1;
					break;
				case '\r':
					if (seenCR)
					{
						sb.append('\r');
					}
					seenCR = true;
					break;
				default:
					if (seenCR)
					{
						seenCR = false; // swallow final CR
						this.listener.handle(sb.toString());
						sb.setLength(0);
						rePos = pos + i + 1;
					}
					sb.append((char) ch); // add character, not its ascii value
				}
			}

			pos = reader.getFilePointer();
		}

		reader.seek(rePos); // Ensure we can re-read if necessary
		return rePos;
	}

}
