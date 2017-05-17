package org.exite.obj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Tickets 
{
	@XmlAttribute
	public String mode;
	@Override
	public String toString() {
		return "Tickets [mode=" + mode + "]";
	}	
}
