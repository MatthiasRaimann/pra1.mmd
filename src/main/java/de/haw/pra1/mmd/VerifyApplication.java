package de.haw.pra1.mmd;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class VerifyApplication implements JavaDelegate {
    private final Logger LOGGER = Logger.getLogger(LoggerDelegate.class.getName());

    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info(execution.getVariable("DatumBescheid")+"jijoijijlij");



//        System.out.println( );
    }

}