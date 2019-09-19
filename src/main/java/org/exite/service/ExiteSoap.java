package org.exite.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.exite.edi.soap.*;

import javax.xml.ws.Service;
import java.util.*;

/**
 * Created by levitskym on 18.09.19
 */
@Slf4j
public class ExiteSoap implements ISoapService {

    private ObjectFactory factory;
    private ExiteWs soap;
    private EdiLogin user;

    public ExiteSoap(final String login, final String rawPass) {
        factory = new ObjectFactory();
        final Service srv = new ExiteWsService();
        this.user = new EdiLogin();
        user.setLogin(login);
        user.setPass(DigestUtils.md5Hex(rawPass));
        this.soap = srv.getPort(ExiteWs.class);
    }

    @Override
    public List<String> list(final String[] filters, final String extension) throws SoapException {
        return list(new HashSet<>(Arrays.asList(filters)), extension);
    }

    private List<String> list(final Set<String> filterSet, final String extension) throws SoapException {
        List<String>list=new LinkedList<>();
        try {
            for(String filter : filterSet){
                list.addAll(list(filter, extension));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return list;
    }

    private List<String> list(final String filter, final String extension) throws SoapException {
        List<String>list=new LinkedList<>();
        try {
            for (String string : getList())
                if(string.contains(filter)){
                    if(extension != null){
                        if(string.endsWith(extension)){
                            list.add(string);
                        }
                    } else {
                        list.add(string);
                    }
                }
        } catch (SoapException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
        return list;
    }

    private List<String>getList() throws SoapException {
        final GetListRequest request = factory.createGetListRequest();
        request.setUser(user);
        final GetListResponse response = soap.getList(request);
        final EdiFileList list = response.getResult();
        if(list.getErrorCode()!=0){
            throw new SoapException(list.getErrorMessage());
        }
        return  list.getList();
    }

    @Override
    public void remove(final String fileName, final boolean withSign) throws SoapException {
        remove(fileName);
        if(fileName.endsWith(".xml") && withSign){
            remove(fileName.replace(".xml",".bin"));
        }
    }

    private boolean remove(final String fileName) throws SoapException {
        try {
            final ArchiveDocRequest request = factory.createArchiveDocRequest();
            request.setUser(user);
            request.setFileName(fileName);
            final ArchiveDocResponse response = soap.archiveDoc(request);
            final EdiResponse result = response.getResult();
            if(result.getErrorCode()!=0){
                throw new SoapException(result.getErrorMessage());
            }
            log.info("[{}] removed", fileName);
            return true;
        } catch (SoapException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean send(final String fileName, final byte[] content) throws SoapException {
        try {
            final SendDocRequest request = factory.createSendDocRequest();
            request.setUser(user);
            request.setFileName(fileName);
            request.setContent(factory.createSendDocRequestContent(content));
            final SendDocResponse response = soap.sendDoc(request);
            final EdiResponse result = response.getResult();
            if(result.getErrorCode()!=0){
                throw new SoapException(result.getErrorMessage());
            }
            log.info("[{}] sent", fileName);
            return true;
        } catch (SoapException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public byte[] body(final String fileName) throws SoapException {
        final GetDocRequest request = factory.createGetDocRequest();
        request.setUser(user);
        request.setFileName(fileName);
        final GetDocResponse response = soap.getDoc(request);
        final EdiFile result = response.getResult();
        if(result.getErrorCode()!=0){
            throw new SoapException(result.getErrorMessage());
        }
        log.info("[{}] loaded", fileName);
        return result.getContent();
    }
}
