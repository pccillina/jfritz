package de.moonflower.jfritz.utils;

public class LogSeverity {

	private int id;
	private String name;
	private String prefix;

	public LogSeverity(int id, String name, String prefix)
	{
		this.id = id;
		this.name = name;
		this.prefix = prefix;
	}

	public String toString() {
		return name;
	}

	public int getId()
	{
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}
}
