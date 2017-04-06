package org.exite.obj;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Logger 
{
	@XmlElement
	public String logger;
	@Override
	public String toString() {
		return "Logger [logger=" + logger + "]";
	}	
}