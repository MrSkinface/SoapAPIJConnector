package org.exite.beans.tickets;

import org.exite.service.tickets.TicketGenerator;
import org.exite.sos.docs.DocTypes;
import org.exite.sos.docs.SosDocument;
import org.exite.sos.docs.dpizvpol.DPIZVPOL;
import org.exite.sos.docs.dpizvpol.DpizvpolEDIMember;
import org.exite.sos.docs.dpizvpol.DpizvpolSender;
import org.exite.sos.docs.dppdotpr.DPPDOTPR;
import org.exite.sos.docs.tpls.EDIOperator;

/**
 * Created by levitskym on 18.09.19
 */
public class IzvpolForPdotpr extends IzvpolTicket implements TicketGenerator {

    @Override
    public byte[] generate(TicketGeneratorData data) throws Exception {

        final SosDocument sosDoc = new SosDocument("cp1251");
        final DPPDOTPR dppdotpr = (DPPDOTPR) sosDoc.fromXML(data.getBaseTicketBody(), DocTypes.DP_PDOTPR);
        final String ticketRecipientGuid = dppdotpr.getFileId().split("_")[2];
        super.fileName = getFileName(DocTypes.DP_IZVPOL.name(), "2LD", ticketRecipientGuid);

        final DpizvpolEDIMember ediMember = new DpizvpolEDIMember();
        ediMember.id = ticketRecipientGuid;
        if (dppdotpr.getSenderUUID().equals(ticketRecipientGuid)) {
            if (dppdotpr.getSenderCompany() != null)
                ediMember.company = dppdotpr.getSenderCompany();
            else
                ediMember.person = dppdotpr.getSenderPerson();
        } else {
            if (dppdotpr.getRecipientCompany() != null)
                ediMember.company = dppdotpr.getRecipientCompany();
            else
                ediMember.person = dppdotpr.getRecipientPerson();
        }

        final DpizvpolSender sender = new DpizvpolSender();
        sender.id = "2LD";
        sender.edi_operator = new EDIOperator();
        sender.edi_operator.name = "ООО \"Э-КОМ\"";
        sender.edi_operator.inn = "9715218298";
        sender.edi_operator.id = "2LD";

        final DPIZVPOL dpizvpol = createIzvpol(fileName, dppdotpr.file_id, ediMember, null, sender, data);

        return sosDoc.toSXML(dpizvpol);
    }
}
