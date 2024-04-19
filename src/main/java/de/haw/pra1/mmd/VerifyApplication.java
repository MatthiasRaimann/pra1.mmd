package de.haw.pra1.mmd;

    import org.camunda.bpm.engine.delegate.DelegateExecution;
    import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class VerifyApplication implements JavaDelegate {
    private final Logger LOGGER = Logger.getLogger(LoggerDelegate.class.getName());

    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info(execution.getVariable("DatumBescheid").getClass().getName()+"Test");
        System.out.println(execution.getVariable("DatumBescheid").getClass().getName()+"Test");
        long bescheidDatum = ((Date) execution.getVariable("DatumBescheid")).getTime();
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Boolean verification = new Date().getTime() > bescheidDatum && calendar.getTime().getTime() < bescheidDatum;
        execution.setVariable("DatumBescheidIsValid", verification);
    }

}