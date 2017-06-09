package org.exite.obj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class RunAsDaemon 
{
	@XmlAttribute
	public boolean enabled;
	@XmlValue
	public long value;
}
