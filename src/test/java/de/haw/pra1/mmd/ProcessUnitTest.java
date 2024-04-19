package de.haw.pra1.mmd;

import org.assertj.core.api.Condition;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessUnitTest {

  @Test
  @Deployment(resources = "process.bpmn")
  public void testHappyPath() {
    // Drive the process by API and assert correct behavior by camunda-bpm-assert

    ProcessInstance processInstance = processEngine().getRuntimeService()
            .createMessageCorrelation("Bescheideingang")
            .setVariable("DatumBescheid", new Date())
            .processInstanceBusinessKey("swag")
            .correlateStartMessage();

    List<Task> list = processEngine().getTaskService().createTaskQuery().active().list();
    Task x = list.get(0);

    assertThat(processInstance).hasBusinessKey("swag");
    assertThat(processInstance).hasVariables("DatumBescheid");

    processEngine().getTaskService().complete(x.getId());

//    assertThat(processInstance).task().has(new Condition<>((x) -> x.getName().equals("Antrag erstellen"), "Task \"Antrag stellen\""));







  }

}

