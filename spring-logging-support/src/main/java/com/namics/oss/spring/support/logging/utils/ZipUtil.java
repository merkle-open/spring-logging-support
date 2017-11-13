/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZipUtil.
 * 
 * @author aschaefer, Namics AG
 * @since 06.02.2013
 */
public abstract class ZipUtil
{
	private static final Logger LOG = LoggerFactory.getLogger(ZipUtil.class);

	/**
	 * Compress the given directory with all its files.
	 */
	public static void zipFiles(File directory,
								OutputStream output) throws IOException
	{
		LOG.debug("Create zip archive of directory {} for files {}", directory);
		int offset = directory.getAbsolutePath().length() + 1;
		Collection<File> files = FileUtils.listFiles(directory, null, true);
		ZipOutputStream zos = new ZipOutputStream(output);
		for (File file : files)
		{
			FileInputStream fis = new FileInputStream(file);
			zos.putNextEntry(new ZipEntry(file.getAbsolutePath().substring(offset)));
			IOUtils.copy(fis, zos);
			IOUtils.closeQuietly(fis);
			zos.closeEntry();
		}
		zos.close();
	}
}
