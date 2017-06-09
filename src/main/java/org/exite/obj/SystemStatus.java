package org.exite.obj;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement(name="ESTATUS")
public class SystemStatus 
{
	@XmlElement
	public String COMMENT;
	@XmlElement
	public String STATUSCODE;
	@XmlElement
	public String DOCID;
	
	@Override
	public String toString() {
		return "SystemStatus [COMMENT=" + COMMENT + ", STATUSCODE=" + STATUSCODE + ", DOCID=" + DOCID + "]";
	}	
}
