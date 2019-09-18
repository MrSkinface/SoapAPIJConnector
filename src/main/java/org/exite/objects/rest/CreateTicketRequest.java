package org.exite.objects.rest;

import lombok.ToString;

/**
 * Created by levitsky on 11.02.19
 */
@ToString
public class CreateTicketRequest extends Request {
    private String identifier;
    private String signer_fname;
    private String signer_sname;
    private String signer_position;
    private String signer_inn;

    public CreateTicketRequest(String varToken, String identifier, String signer_fname, String signer_sname, String signer_position, String signer_inn) {
        super(varToken);
        this.identifier = identifier;
        this.signer_fname = signer_fname;
        this.signer_sname = signer_sname;
        this.signer_position = signer_position;
        this.signer_inn = signer_inn;
    }
}
