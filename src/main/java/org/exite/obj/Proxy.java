package org.exite.obj;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Proxy 
{
	@XmlElement
	public String host;
	@XmlElement
	public int port;
	@Override
	public String toString() {
		return "Proxy [host=" + host + ", port=" + port + "]";
	}	
}
