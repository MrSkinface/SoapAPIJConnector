package org.exite.beans.tickets;

import lombok.Builder;
import lombok.Data;

/**
 * Created by levitskym on 18.09.19
 */
@Data
@Builder
public class Signer {

    private String firstName;
    private String lastName;
    private String middleName;
    private String position;
    private String inn;
    private String oblPoln;
    private String osnPoln;
}
