package org.exite;

import org.exite.RestExAPI.IRestExAPI;
import org.exite.RestExAPI.RestExAPI;
import org.exite.SoapExAPI.ISoapExAPI;
import org.exite.SoapExAPI.SoapExAPI;

public class Connector 
{

	public static void main(String[] args) throws Exception
	{
		IRestExAPI api=new RestExAPI();
        ISoapExAPI soap=new SoapExAPI("login", "pass");

	}

}
