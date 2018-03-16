package org.exite;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.exite.obj.Config;
import org.exite.utils.Parser;
import org.exite.workers.*;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

public class Connector implements Runnable {

	private static final Logger log=Logger.getLogger(Connector.class);

    private Config conf;
	private Controller controller;

    private final String[] upd_types = new String[]{"ON_SCHFDOPPR","ON_KORSCHFDOPPR"};
    private final String[] doc_types = new String[]{"DP_PDOTPR"};
    private final String[] to_delete_types = new String[]{"DP_IZVPOL"};
    private final String[] edo_status_types = new String[]{"EDOSTATUS"};
	
	public Connector() {
        try{

            conf=getConfig();
            controller=new Controller(conf);
            registerShutdownHook();
            new Thread(this).start();

        } catch (Exception e){
            log.error(e);
        }
	}

	public Connector(String option) {
        try{
            conf=getConfig();
            controller=new Controller(conf);
            switch(option){
                case "-help":
                    controller.usageHelp();
                    break;
                case "-testconnection":
                    controller.testConnection();
                    break;
                case "-testcrypto":
                    controller.testCrypto();
                    break;
                default:
                    System.out.println("wrong usage . See [-help] option");
                    break;
            }
        } catch (Exception e){
            log.error(e);
        }
	}

	@Override
	public void run() {

        try{
            controller.checkCryptexAlive();
            conf.setupSigner(controller.getCert(conf.cryptex.alias));
            QWorker worker = new QWorker(conf, controller);

            new Thread(new InQueueCreator(worker, !conf.isAutoConfirm() ? Queues.TO_REMOVE : Queues.INBOUND_UPD, upd_types, ".zip")).start();
            new Thread(new InQueueCreator(worker, Queues.INBOUND_DOCS, doc_types, ".xml")).start();
			new Thread(new InQueueCreator(worker, Queues.TO_REMOVE, to_delete_types, ".xml")).start();

            if (!conf.isAutoConfirm()){
                new Thread(new InQueueCreator(worker, Queues.INBOUND_DOCS, edo_status_types, ".xml")).start();
            }

            new Thread(new InWorker(worker, Queues.INBOUND_UPD, Queues.TO_SIGN_UPD)).start();
            new Thread(new InWorker(worker, Queues.INBOUND_DOCS, Queues.TO_SIGN_DOCS)).start();

            new Thread(new SignWorker(worker, Queues.TO_SIGN_UPD, Queues.OUTBOUND_UPD, Queues.OUTBOUND_TICKETS)).start();
            new Thread(new SignWorker(worker, Queues.TO_SIGN_DOCS, null, Queues.OUTBOUND_TICKETS)).start();

            new Thread(new OutWorker(worker, Queues.OUTBOUND_UPD, Queues.TO_REMOVE)).start();
            new Thread(new OutWorker(worker, Queues.OUTBOUND_TICKETS, Queues.TO_REMOVE)).start();

            new Thread(new RemoveWorker(worker, Queues.TO_REMOVE)).start();

        } catch (Exception e){
            log.error(e);
        }
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                conf.execute = false;
                log.info("Received EXIT_SIGNAL");
            }
        });
	}

	public static void main(String[] args) throws Exception {
		if(args.length>0)
			new Connector(args[0]);
		else
			new Connector();
	}

	private Config getConfig() {
		try {
			Config conf=(Config)Parser.fromXml(Files.readAllBytes(Paths.get(System.getProperty("user.dir")).resolve("config").resolve("config.xml")), Config.class);
            return conf;
		} catch (Exception e) {
            e.printStackTrace();
			log.error(e);
		}
		return null;
	}
}
