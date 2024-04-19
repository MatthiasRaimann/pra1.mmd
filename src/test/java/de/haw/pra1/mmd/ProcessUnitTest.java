package de.haw.pra1.mmd;

import org.assertj.core.api.Condition;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Calendar;
import java.util.Date;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessUnitTest {

  @Test
  @Deployment(resources = "process.bpmn")
  public void testHappyPath() {
    TaskService taskService = processEngine().getTaskService();


    ProcessInstance processInstance = processEngine().getRuntimeService()
        .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    processEngine().getRuntimeService().createMessageCorrelation("AkteneinsichtGewährt")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();


    Task t = taskService.createTaskQuery().active().taskName("Begruendung schreiben").singleResult();
    taskService.complete(t.getId());

    processEngine().getRuntimeService().createMessageCorrelation("Behoerdenantwort")
            .processInstanceBusinessKey("myBusinessKey")
            .setVariable("WiderspruchAbgeholfen", true)
            .correlate();

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testDateNotValid() {
    Calendar calendar= Calendar.getInstance();
    calendar.add(Calendar.MONTH, 2);

    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", calendar.getTime())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testFirstRejectRequest() throws Exception {
    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    processEngine().getRuntimeService().createMessageCorrelation("Kunde Abbruchwunsch")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testSecondRejectRequest() {
    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    processEngine().getRuntimeService().createMessageCorrelation("AkteneinsichtGewährt")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();


    TaskService taskService = processEngine().getTaskService();

    processEngine().getRuntimeService().createMessageCorrelation("Kunde Abbruchwunsch")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testThirdRejectRequest() {
    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    processEngine().getRuntimeService().createMessageCorrelation("AkteneinsichtGewährt")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();


    TaskService taskService = processEngine().getTaskService();
    Task t = taskService.createTaskQuery().active().taskName("Begruendung schreiben").singleResult();
    taskService.complete(t.getId());

    processEngine().getRuntimeService().createMessageCorrelation("Kunde Abbruchwunsch")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();

    assertThat(taskService.createTaskQuery().active().list().get(0))
            .is(new Condition<>(task -> task.getName().equals("Verfahren beenden und Rechnung schreiben"), "Richtige Activity!"));

    Task task = taskService.createTaskQuery().active().taskName("Verfahren beenden und Rechnung schreiben").singleResult();
    taskService.complete(task.getId());

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testFirstTimerExceeded() {
    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    Job timer = managementService().createJobQuery().active().timers().singleResult();
    managementService().executeJob(timer.getId());

    TaskService taskService = processEngine().getTaskService();
    Task t = taskService.createTaskQuery().active().taskName("Klage auf Akteneinsicht einlegen").singleResult();

    taskService.complete(t.getId());

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testSecondTimerExceeded() {
    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    processEngine().getRuntimeService().createMessageCorrelation("AkteneinsichtGewährt")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();

    TaskService taskService = processEngine().getTaskService();
    Task t = taskService.createTaskQuery().active().taskName("Begruendung schreiben").singleResult();
    taskService.complete(t.getId());

    Job timer = managementService().createJobQuery().active().timers().singleResult();
    managementService().executeJob(timer.getId());

    Task t1 = taskService.createTaskQuery().active().taskName("Ungültigkeitsklage einlegen").singleResult();
    taskService.complete(t1.getId());

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testObjectionRefused() {
    TaskService taskService = processEngine().getTaskService();


    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Antragseingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("myBusinessKey")
            .correlateStartMessage();

    processEngine().getRuntimeService().createMessageCorrelation("AkteneinsichtGewährt")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();


    Task t = taskService.createTaskQuery().active().taskName("Begruendung schreiben").singleResult();
    taskService.complete(t.getId());

    processEngine().getRuntimeService().createMessageCorrelation("Behoerdenantwort")
            .processInstanceBusinessKey("myBusinessKey")
            .setVariable("WiderspruchAbgeholfen", false)
            .correlate();

    processEngine().getRuntimeService().createMessageCorrelation("AkteneinsichtGewährt")
            .processInstanceBusinessKey("myBusinessKey")
            .correlate();

    assertThat(taskService.createTaskQuery().active().list().get(0))
            .is(new Condition<>(
                    task -> task.getName().equals("Begruendung schreiben"),
                    "Es wird wieder auf die 'Begruendung schreiben' gewartet")
            );

    assertThat(processInstance).isNotEnded();
  }
}
