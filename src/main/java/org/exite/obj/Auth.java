package org.exite.obj;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Auth 
{
	@XmlElement
	public String login;
	@XmlElement
	public String password;

	@Override
	public String toString() {
		return "Auth [login=" + login + ", password=" + password + "]";
	}		
}
