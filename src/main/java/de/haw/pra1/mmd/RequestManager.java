package de.haw.pra1.mmd;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class RequestManager implements JavaDelegate {
    private final Logger LOGGER = Logger.getLogger(LoggerDelegate.class.getName());

    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("Generate PDF for: " + execution.getBusinessKey());
        LOGGER.info("File objection for: " + execution.getBusinessKey());
    }
}
