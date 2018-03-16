package org.exite.obj;

import org.exite.crypt.ECertificate;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement(name="config")
public class Config 
{
    public transient boolean execute = true;

	@XmlElement
	public long sleepTime;
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

    public boolean isAutoConfirm(){
        try{
            return Boolean.valueOf(tickets.confirm.equalsIgnoreCase("auto"));
        }catch (Exception e){
            return false;
        }
    }

	public void setupSigner(ECertificate cert){
		getSigner().checkFromCertSigner(cert);
	}

	private Signer getSigner(){
		return cryptex.signer;
	}

    public char[] getCertStorePass(){
        try{
            return cryptex.password.toCharArray();
        }catch (Exception e){
            return null;
        }
    }

	@Override
	public String toString() {
		return "Config [soap=" + soap + ", rest=" + rest + ", cryptex=" + cryptex + ", proxy=" + proxy + ", tickets="
				+ tickets + ", logger=" + logger + "]";
	}			
}
