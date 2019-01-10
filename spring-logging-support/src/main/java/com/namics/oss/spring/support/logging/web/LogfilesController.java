/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.web;

import com.namics.oss.spring.support.logging.service.TailService;
import com.namics.oss.spring.support.logging.utils.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * LogfilesController.
 *
 * @author aschaefer, Namics AG
 * @since 06.02.2013
 */
@Controller("nxlogLogfilesController")
@RequestMapping("/logs")
public class LogfilesController
{

	private static final Logger LOG = LoggerFactory.getLogger(LogfilesController.class);

	private Resource basePath;

	private TailService tailService;

	private int pathOffset = 0;

	private String zipFileName = "logfiles";

	private String tailUrl = "logs/tail?filename=";


	public LogfilesController(Resource basePath, TailService tailService) throws IOException
	{
		this.basePath = basePath;
		this.tailService = tailService;
		this.pathOffset = basePath.getFile().getAbsolutePath().length() + 1;
	}

	@PostConstruct
	public void init() throws Exception
	{
		this.pathOffset = this.basePath.getFile().getAbsolutePath().length() + 1;
	}

	@RequestMapping(value = "/config", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Map<String, Object> getConfig() throws Exception
	{
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("url", this.tailUrl);
		return result;
	}

	@RequestMapping(value = "/files", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Set<String> getAvailableLogFiles() throws Exception
	{
		LOG.debug("list logfiles");
		Set<String> fileNames = new TreeSet<String>(Collections.reverseOrder());
		File baseDir = this.basePath.getFile();
		Collection<File> files = FileUtils.listFiles(baseDir, null, true);
		for (File file : files)
		{
			String filename = file.getAbsolutePath();
			filename = filename.substring(this.pathOffset);
			fileNames.add(filename);
		}
		return fileNames;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public void downloadLogfiles(@RequestParam(required = false, value = "filename") String filename,
	                             OutputStream out,
	                             HttpServletResponse response) throws Exception
	{
		if (StringUtils.hasText(filename))
		{
			LOG.debug("download {}", filename);
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			File file = new File(this.basePath.getFile() + "/" + filename);
			FileUtils.copyFile(file, out);
		}
		else
		{
			String zipname = this.zipFileName + "-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".zip\"";
			LOG.debug("download {}", zipname);
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + zipname);
			ZipUtil.zipFiles(this.basePath.getFile(), out);
		}
	}

	/**
	 * Asyncron method which returns a array of messages every time the for the filename is updated.
	 *
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/tail", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public DeferredResult<Collection<String>> tail(@RequestParam final String filename,
	                                               @RequestParam(required = false, defaultValue = "0") long start) throws Exception
	{
		LOG.debug("tail -{}f {}", start, filename);
		return this.tailService.tail(filename, start);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception e)
	{
		String message = e.toString();
		LOG.warn(message);
		LOG.debug(message, e);
		ResponseEntity<String> result = new ResponseEntity<String>(message, HttpStatus.INTERNAL_SERVER_ERROR);
		return result;
	}

	/**
	 * Setter for zipFileName. @param zipFileName the zipFileName to set
	 */
	public LogfilesController setZipFileName(String zipFileName)
	{
		this.zipFileName = zipFileName;
		return this;
	}

}
