package org.exite.obj;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement(name="config")
public class Config 
{
	@XmlElement(name="run_as_daemon")
	public RunAsDaemon daemon;
	@XmlElement
	public List<Auth> soap;	
	@XmlElement
	public List<Auth> rest;
	@XmlElement
	public Cryptex cryptex;
	@XmlElement
	public Proxy proxy;
	@XmlElement
	public Tickets tickets;
	@XmlElement
	public Logger logger;
	@Override
	public String toString() {
		return "Config [soap=" + soap + ", rest=" + rest + ", cryptex=" + cryptex + ", proxy=" + proxy + ", tickets="
				+ tickets + ", logger=" + logger + "]";
	}			
}
