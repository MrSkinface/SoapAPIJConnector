package org.exite.rest;

import org.exite.exception.RestException;
import org.exite.objects.rest.Entity;

/**
 * Created by levitsky on 11.02.19
 */
public interface RestAPI {

    /**
     *
     * @param login
     * 			Exite login
     * @param pass
     * 			Exite pass
     * @return
     * 			string authorize token
     * @throws RestException
     * 			if authorization fails
     */
    String authorize(String login, String pass) throws RestException;

    /**
     *
     * @param authToken
     * 			authorize token
     * @param docId
     * 			docId
     * @return
     * 			Entity
     * @throws RestException
     * 			if something goes wrong [bad 'authToken', bad 'docId']
     */
    Entity getContent(String authToken, String docId) throws RestException;

    /**
     *
     * @param authToken
     * 			authorize token
     * @param identifier
     * 			identifier
     * @param signer_fname
     * 			signer_fname
     * @param signer_sname
     * 			signer_sname
     * @param signer_position
     * 			signer_position
     * @param signer_inn
     * 			signer_inn
     * @return
     * 			base64 string of xml-ticket
     * @throws RestException
     * 			if something goes wrong [bad 'authToken', bad 'identifier']
     */
    String generateTicket(String authToken, String identifier, String signer_fname, String signer_sname,
                          String signer_position, String signer_inn) throws RestException;

}
