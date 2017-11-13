/*
 * Copyright 2000-2013 Namics AG. All rights reserved.
 */

package com.namics.oss.spring.support.logging.web.binding;

/**
 * LoggerBean.
 *
 * @author aschaefer, Namics AG
 * @since 06.02.2013
 */
public class LoggerBean implements Comparable<LoggerBean>
{
	private String name;

	private String parent;

	private String level;

	/**
	 * Getter for name. @return the name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Setter for name. @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Getter for parent. @return the parent
	 */
	public String getParent()
	{
		return this.parent;
	}

	/**
	 * Setter for parent. @param parent the parent to set
	 */
	public void setParent(String parent)
	{
		this.parent = parent;
	}

	/**
	 * Getter for level. @return the level
	 */
	public String getLevel()
	{
		return this.level;
	}

	/**
	 * Setter for level. @param level the level to set
	 */
	public void setLevel(String level)
	{
		this.level = level;
	}

	public LoggerBean name(String name)
	{
		this.setName(name);
		return this;
	}

	public LoggerBean parent(String parent)
	{
		this.setParent(parent);
		return this;
	}

	public LoggerBean level(String level)
	{
		this.setLevel(level);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof LoggerBean))
		{
			return false;
		}
		LoggerBean other = (LoggerBean) obj;
		if (this.name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!this.name.equals(other.name))
		{
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(LoggerBean o)
	{
		String thisName = this.getName() != null ? this.getName() : "";
		String thatName = o != null && o.getName() != null ? o.getName() : "";
		return thisName.compareToIgnoreCase(thatName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("LoggerBean [name=");
		builder.append(this.name);
		builder.append(", parent=");
		builder.append(this.parent);
		builder.append(", level=");
		builder.append(this.level);
		builder.append("]");
		return builder.toString();
	}

}
