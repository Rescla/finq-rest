package nl.finan.finq.embeder;


import nl.eernie.jmoribus.model.*;
import nl.eernie.jmoribus.model.Scenario;
import nl.eernie.jmoribus.model.Step;
import nl.eernie.jmoribus.model.Story;
import nl.eernie.jmoribus.reporter.Reporter;
import nl.finan.finq.dao.RunningStoriesDao;
import nl.finan.finq.dao.StoryDao;
import nl.finan.finq.entities.*;
import nl.finan.finq.service.ReportService;
import nl.finan.finq.websocket.StatusType;
import nl.finan.finq.websocket.StatusWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.transaction.Transactional;

@Stateless
@Transactional
public class WebStoryReporter implements Reporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebStoryReporter.class);

    @EJB
    private RunningStoriesDao runningStoriesDao;

    @EJB
    private StoryDao storyDao;

    @EJB
    private ReportService reportService;

    @EJB
    private StatusWebSocket statusWebSocket;


    @Override
    @Transactional
    public void beforeStory(Story story) {
        Long reportId = getReportId(story.getUniqueIdentifier());
        RunningStories runningStories = runningStoriesDao.find(reportId);

        nl.finan.finq.entities.Story storyModel = storyDao.find(getStoryId(story.getUniqueIdentifier()));

        StoryLog storyLog = reportService.createStoryLog(storyModel, runningStories);
        statusWebSocket.sendStatus(reportId, storyLog, StatusType.BEFORE_STORY);
    }

    @Override
    public void beforeScenario(Scenario beforeScenario) {
        int scenarioIndex = beforeScenario.getStory().getScenarios().indexOf(beforeScenario);
        Long reportId = getReportId(beforeScenario.getStory().getUniqueIdentifier());
        RunningStories runningStories = runningStoriesDao.find(reportId);

        StoryLog storyLog = runningStories.getLogs().get(runningStories.getLogs().size()-1);
        nl.finan.finq.entities.Scenario scenario = storyLog.getStory().getScenarios().get(scenarioIndex);

        ScenarioLog scenarioLog = reportService.createScenarioLog(scenario, storyLog);
        statusWebSocket.sendStatus(reportId, scenarioLog, StatusType.BEFORE_SCENARIO);
    }

    @Override
    public void beforeStep(Step runningStep) {

    }

    @Override
    public void successStep(Step runningStep) {
        Scenario scenario = (Scenario) runningStep.getStepContainer();
        int scenarioIndex = scenario.getStory().getScenarios().indexOf(scenario);
        Long reportId = getReportId(scenario.getStory().getUniqueIdentifier());
        RunningStories runningStories = runningStoriesDao.find(reportId);
        StoryLog storyLog = runningStories.getLogs().get(runningStories.getLogs().size()-1);
        ScenarioLog scenarioLog = storyLog.getScenarioLogs().get(scenarioIndex);

        int index = runningStep.getStepContainer().getSteps().indexOf(runningStep);
        nl.finan.finq.entities.Step step = scenarioLog.getScenario().getSteps().get(index);
        StepLog stepLog = reportService.createStepLog(step, scenarioLog, LogStatus.SUCCESS);
        statusWebSocket.sendStatus(reportId, stepLog, StatusType.SUCCESSFUL_STEP);

    }

    @Override
    public void pendingStep(Step runningStep) {
        Scenario scenario = (Scenario) runningStep.getStepContainer();
        int scenarioIndex = scenario.getStory().getScenarios().indexOf(scenario);
        Long reportId = getReportId(scenario.getStory().getUniqueIdentifier());
        RunningStories runningStories = runningStoriesDao.find(reportId);
        StoryLog storyLog = runningStories.getLogs().get(runningStories.getLogs().size()-1);
        ScenarioLog scenarioLog = storyLog.getScenarioLogs().get(scenarioIndex);

        int index = runningStep.getStepContainer().getSteps().indexOf(runningStep);
        nl.finan.finq.entities.Step step = scenarioLog.getScenario().getSteps().get(index);
        StepLog stepLog = reportService.createStepLog(step, scenarioLog, LogStatus.PENDING);
        statusWebSocket.sendStatus(reportId, stepLog, StatusType.PENDING_STEP);
    }

    @Override
    public void afterScenario(Scenario scenario) {
        int scenarioIndex = scenario.getStory().getScenarios().indexOf(scenario);
        Long reportId = getReportId(scenario.getStory().getUniqueIdentifier());
        RunningStories runningStories = runningStoriesDao.find(reportId);
        StoryLog storyLog = runningStories.getLogs().get(runningStories.getLogs().size()-1);
        ScenarioLog scenarioLog = storyLog.getScenarioLogs().get(scenarioIndex);
        if (scenarioLog.getStatus().equals(LogStatus.RUNNING)) {
            scenarioLog.setStatus(LogStatus.SUCCESS);
            statusWebSocket.sendStatus(reportId, scenarioLog, StatusType.AFTER_SCENARIO);

        }
    }

    @Override
    public void afterStory(Story story) {

        Long reportId = getReportId(story.getUniqueIdentifier());
        RunningStories runningStories = runningStoriesDao.find(reportId);
        StoryLog storyLog = runningStories.getLogs().get(runningStories.getLogs().size()-1);
        if (storyLog.getStatus().equals(LogStatus.RUNNING)) {
            storyLog.setStatus(LogStatus.SUCCESS);
            statusWebSocket.sendStatus(reportId, storyLog, StatusType.AFTER_STORY);
        }
    }

    @Override
    public void failedStep(Step runningStep, AssertionError e) {
        Scenario scenario = (Scenario) runningStep.getStepContainer();
        int scenarioIndex = scenario.getStory().getScenarios().indexOf(scenario);
        Long reportId = getReportId(scenario.getStory().getUniqueIdentifier());
        RunningStories runningStories = runningStoriesDao.find(reportId);
        StoryLog storyLog = runningStories.getLogs().get(runningStories.getLogs().size()-1);
        ScenarioLog scenarioLog = storyLog.getScenarioLogs().get(scenarioIndex);

        int index = runningStep.getStepContainer().getSteps().indexOf(runningStep);
        nl.finan.finq.entities.Step step = scenarioLog.getScenario().getSteps().get(index);

        StepLog stepLog = reportService.createStepLog(step, scenarioLog, LogStatus.FAILED);
        stepLog.setLog(e.getMessage());
        scenarioLog.getStoryLog().setStatus(LogStatus.FAILED);
        scenarioLog.setStatus(LogStatus.FAILED);
        scenarioLog.getStoryLog().getRunningStory().setStatus(LogStatus.FAILED);
        statusWebSocket.sendStatus(reportId, stepLog, StatusType.FAILED_STEP);
    }

    @Override
    public void errorStep(Step step, Throwable e) {

    }

    @Override
    public void errorStep(Step step, String cause) {

    }

    @Override
    public void feature(Feature feature) {

    }

    @Override
    public void beforePrologue(Prologue prologue) {

    }

    @Override
    public void afterPrologue(Prologue prologue) {

    }

    @Override
    public void beforeReferringScenario(StepContainer stepContainer, Scenario scenario) {

    }

    @Override
    public void afterReferringScenario(StepContainer stepContainer, Scenario scenario) {

    }

    private Long getReportId(String uniqueIdentifier){
        return Long.valueOf(uniqueIdentifier.split("-")[1]);
    }

    private Long getStoryId(String uniqueIdentifier){
        return Long.valueOf(uniqueIdentifier.split("-")[0]);
    }
}
