package org.exite.service;

import java.util.List;

/**
 * Created by levitskym on 18.09.19
 */
public interface ISoapService {

    List<String> list(String[]filters, String extension) throws SoapException;

    void remove(String fileName, boolean withSign) throws SoapException;

    boolean send(String fileName, byte[]content) throws SoapException;

    byte[] body(String fileName) throws SoapException;
}
