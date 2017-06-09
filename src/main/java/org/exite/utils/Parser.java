package org.exite.utils;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Parser 
{
	public static Object fromXml(byte[]body,Class<? extends Object> c)
	{
		try 
		{
			JAXBContext jc=JAXBContext.newInstance(c);
			Unmarshaller unm=jc.createUnmarshaller();
			return unm.unmarshal(new ByteArrayInputStream(body));
		} catch (JAXBException e) 
		{		
			e.printStackTrace();
			return null;
		}				
	}
}
