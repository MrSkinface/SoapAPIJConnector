package org.exite.beans.tickets;

import org.exite.sos.docs.dpizvpol.*;
import org.exite.sos.docs.tpls.DocumentData;
import org.exite.sos.docs.tpls.Fio;
import org.exite.sos.docs.tpls.Signer;

import java.text.SimpleDateFormat;

/**
 * Created by levitskym on 18.09.19
 */
public abstract class IzvpolTicket {

    protected String fileName;

    public String getFileName() {
        return fileName;
    }

    public DPIZVPOL createIzvpol(final String fileName, final String baseFileId, final DpizvpolEDIMember ediMember,
                                 final DocumentData documentData, final DpizvpolSender sender, final TicketGeneratorData data) {
        DPIZVPOL dpizvpol = new DPIZVPOL();
        dpizvpol.program_version = "SelgsosAPI v2.0";
        dpizvpol.format_version = "1.02";
        dpizvpol.file_id = fileName;
        dpizvpol.document = new DpizvpolDocument();
        dpizvpol.document.knd_form_code = "1115110";

        dpizvpol.document.edi_member = ediMember;

        dpizvpol.document.info = new DpizvpolReceiveNotificationInfo();
        dpizvpol.document.info.date = new SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis());
        dpizvpol.document.info.time = new SimpleDateFormat("HH.mm.ss").format(System.currentTimeMillis());
        dpizvpol.document.info.file_info = new DpizvpolReceiveNotificationFileInfo();
        dpizvpol.document.info.file_info.file_name = baseFileId;
        dpizvpol.document.info.file_info.base64_Sign.add(data.getBaseTicketSign());

        if (documentData != null) {
            dpizvpol.document.info.file_info.recipient = documentData;
        }

        dpizvpol.document.sender = sender;

        dpizvpol.document.signer = new Signer();
        dpizvpol.document.signer.position = data.getSigner().getPosition();
        dpizvpol.document.signer.fio = new Fio();
        dpizvpol.document.signer.fio.first_name = data.getSigner().getFirstName();
        dpizvpol.document.signer.fio.last_name = data.getSigner().getLastName();


        return dpizvpol;
    }
}
