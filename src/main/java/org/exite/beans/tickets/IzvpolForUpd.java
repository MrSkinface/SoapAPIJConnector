package org.exite.beans.tickets;

import org.exite.service.tickets.TicketGenerator;
import org.exite.sos.docs.DocTypes;
import org.exite.sos.docs.SosDocument;
import org.exite.sos.docs.dpizvpol.DPIZVPOL;
import org.exite.sos.docs.dpizvpol.DpizvpolEDIMember;
import org.exite.sos.docs.dpizvpol.DpizvpolSender;
import org.exite.sos.docs.tpls.DocumentData;
import org.exite.sos.docs.upd.types.AbstractUpd;

/**
 * Created by levitskym on 18.09.19
 */
public class IzvpolForUpd extends IzvpolTicket implements TicketGenerator {

    private final DocTypes type;

    public IzvpolForUpd(final String fileName) {
        if(fileName.startsWith("ON_KORSCHFDOPPR") || fileName.startsWith("ON_NKORSCHFDOPPR"))
            type = DocTypes.UPD_KORSCHFDIS;
        else
            type = DocTypes.UPD_SCHFDOP;
    }

    @Override
    public byte[] generate(final TicketGeneratorData data) throws Exception {
        final SosDocument sosDoc = new SosDocument("cp1251");
        final AbstractUpd upd = (AbstractUpd) sosDoc.fromXML(data.getBaseTicketBody(), type);
        super.fileName = getFileName(DocTypes.DP_IZVPOL.name(), upd.memberinfo.idotpr, upd.memberinfo.idpol);

        final DpizvpolEDIMember ediMember = new DpizvpolEDIMember();
        ediMember.id = upd.memberinfo.idpol;
        if (upd.getBuyerInfo().idsv.company != null) {
            ediMember.company = upd.getBuyerInfo().idsv.company;
        } else {
            ediMember.person = upd.getBuyerInfo().idsv.person;
        }

        final DocumentData documentData = new DocumentData();
        documentData.doc_name = upd.getIdfile();
        documentData.doc_number = upd.getNumber();
        documentData.doc_date = upd.getDocDate();

        final DpizvpolSender sender = new DpizvpolSender();
        sender.id = upd.memberinfo.idotpr;
        if (upd.getSellerInfo().idsv.company != null) {
            sender.company = upd.getSellerInfo().idsv.company;
        } else {
            sender.person = upd.getSellerInfo().idsv.person;
        }

        final DPIZVPOL dpizvpol = createIzvpol(fileName, upd.getIdfile(), ediMember, documentData, sender, data);

        return sosDoc.toSXML(dpizvpol);
    }
}
