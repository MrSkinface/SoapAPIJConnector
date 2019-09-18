package org.exite.rest;

import lombok.extern.slf4j.Slf4j;
import org.exite.exception.RestException;
import org.exite.objects.rest.*;

/**
 * Created by levitsky on 11.02.19
 */
@Slf4j
public class ExiteRestAPI implements RestAPI {

    private static final String URL = "https://e-vo.ru/Api/Dixy/";

    @Override
    public String authorize(String login, String pass) throws RestException {
        AuthorizeRequest req = AuthorizeRequest.builder().varLogin(login).varPassword(pass).build();
        AuthorizeResponse resp =(AuthorizeResponse)Http.post(URL + "Index/Authorize", req , AuthorizeResponse.class);
        if(resp.getIntCode() != 200)
            throw new RestException(resp.getVarMessage());
        return resp.getVarToken();
    }

    @Override
    public Entity getContent(String authToken, String docId) throws RestException {
        GetContentRequest req = new GetContentRequest(authToken, docId);
        GetContentResponse resp = (GetContentResponse)Http.post(URL + "Content/GetDocWithSignContent", req, GetContentResponse.class);
        if(resp.getIntCode() != 200)
            throw new RestException(resp.getVarMessage());
        return Entity.builder().body(resp.getBody()).sign(resp.getSign().get(0).getBody()).build();
    }

    @Override
    public String generateTicket(String authToken, String identifier, String signer_fname, String signer_sname,
                                 String signer_position, String signer_inn) throws RestException {
        CreateTicketRequest req = new CreateTicketRequest(authToken, identifier, signer_fname, signer_sname, signer_position, signer_inn);
        CreateTicketResponse resp = (CreateTicketResponse)Http.post(URL + "Ticket/Generate", req, CreateTicketResponse.class);
        if(resp.getIntCode() != 200)
            throw new RestException(resp.getVarMessage());
        return resp.getContent();
    }
}
