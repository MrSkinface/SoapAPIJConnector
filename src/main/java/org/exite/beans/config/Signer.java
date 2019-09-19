package org.exite.beans.config;

import org.exite.crypt.ECertificate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Signer {

	@XmlAttribute
	public String using;
	@XmlElement
	public String signer_org_inn;
	@XmlElement
	public String signer_name;
	@XmlElement
	public String signer_surName;
	@XmlElement
	public String signer_orgUnit;

	public void checkFromCertSigner(ECertificate cert) {
		if(using.equals("cert")) {
			try {
				signer_org_inn=parseInnFromCertInfo(cert.getInn());
				signer_name=cert.getGivenname().split(" ")[0];
				signer_surName=cert.getGivenname().split(" ")[1];
				signer_orgUnit=cert.getTitle();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}

	private String parseInnFromCertInfo(String fromCertInn) {
		String s=fromCertInn.replace("#120C", "");
		char[]chars=s.toCharArray();
		StringBuilder sb=new StringBuilder();		
		for(int i=0;i<chars.length;i++) {
			if(i%2==1)
				sb.append(chars[i]);
		}
		String res=sb.toString();
		if(res.substring(0, 2).equals("00"))
			return res.substring(2, res.length());
		return res;
	}
}
