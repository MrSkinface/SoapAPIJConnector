package org.exite.objects.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Cryptex {

	@XmlElement
	public String alias;
	@XmlElement
	public String password;
	@XmlElement
	public Signer signer;
}
