/**
 * This method sets up the mock objects and conditions for the test.
 */
private void setupMocks(Watch watch, GetResponse getResponse, ZonedDateTime now, ScheduleTriggerEvent event, TriggeredExecutionContext context, Condition.Result conditionResult, ExecutableCondition condition, Transform.Result watchTransformResult, ExecutableTransform watchTransform, Throttler.Result throttleResult, ActionThrottler throttler, ExecutableCondition actionCondition, Condition.Result actionConditionResult, Transform.Result actionTransformResult, ExecutableTransform actionTransform, Action.Result actionResult, ExecutableAction action, ActionWrapper actionWrapper, WatchStatus watchStatus) {
    when(watch.id()).thenReturn("_id");
    when(getResponse.isExists()).thenReturn(true);
    mockGetWatchResponse(client, "_id", getResponse);
    when(parser.parseWithSecrets(eq(watch.id()), eq(true), any(), any(), any(), anyLong(), anyLong())).thenReturn(watch);
    when(condition.execute(any(WatchExecutionContext.class))).thenReturn(conditionResult);
    when(watchTransformResult.status()).thenReturn(Transform.Result.Status.SUCCESS);
    when(watchTransformResult.payload()).thenReturn(payload);
    when(watchTransform.execute(context, payload)).thenReturn(watchTransformResult);
    when(throttleResult.throttle()).thenReturn(false);
    when(throttler.throttle("_action", context)).thenReturn(throttleResult);
    if (randomBoolean()) {
        Tuple<ExecutableCondition, Condition.Result> pair = whenCondition(context);
        actionCondition = pair.v1();
        actionConditionResult = pair.v2();
    }
    when(actionTransformResult.status()).thenReturn(Transform.Result.Status.FAILURE);
    when(actionTransformResult.reason()).thenReturn("_reason");
    when(actionTransform.execute(context, payload)).thenReturn(actionTransformResult);
    when(actionResult.type()).thenReturn("_action_type");
    when(actionResult.status()).thenReturn(Action.Result.Status.SUCCESS);
    when(action.logger()).thenReturn(logger);
    when(action.execute("_action", context, payload)).thenReturn(actionResult);
    when(watch.input()).thenReturn(input);
    when(watch.condition()).thenReturn(condition);
    when(watch.transform()).thenReturn(watchTransform);
    when(watch.actions()).thenReturn(Arrays.asList(actionWrapper));
    when(watch.status()).thenReturn(watchStatus);
}
/**
 * This method executes the test and verifies the results.
 */
private void executeTestAndVerifyResults(Watch watch, TriggeredExecutionContext context, Condition.Result conditionResult, Transform.Result watchTransformResult, Condition.Result actionConditionResult, Transform.Result actionTransformResult, ExecutableAction action) {
    WatchRecord watchRecord = executionService.execute(context);
    assertThat(watchRecord.result().inputResult(), is(inputResult));
    assertThat(watchRecord.result().conditionResult(), is(conditionResult));
    assertThat(watchRecord.result().transformResult(), is(watchTransformResult));
    assertThat(watchRecord.result().actionsResults(), notNullValue());
    assertThat(watchRecord.result().actionsResults().size(), is(1));
    assertThat(watchRecord.result().actionsResults().get("_action").condition(), is(actionConditionResult));
    assertThat(watchRecord.result().actionsResults().get("_action").transform(), is(actionTransformResult));
    assertThat(watchRecord.result().actionsResults().get("_action").action().status(), is(Action.Result.Status.FAILURE));
    verify(historyStore, times(1)).put(watchRecord);
    verify(input, times(1)).execute(context, null);
    verify(condition, times(1)).execute(context);
    verify(watchTransform, times(1)).execute(context, payload);
    verify(action, never()).execute("_action", context, payload);
}
public void testExecuteFailedActionTransform() throws Exception {
    Watch watch = mock(Watch.class);
    GetResponse getResponse = mock(GetResponse.class);
    ZonedDateTime now = clock.instant().atZone(ZoneOffset.UTC);
    ScheduleTriggerEvent event = new ScheduleTriggerEvent("_id", now, now);
    TriggeredExecutionContext context = new TriggeredExecutionContext(watch.id(), now, event, timeValueSeconds(5));
    Condition.Result conditionResult = InternalAlwaysCondition.RESULT_INSTANCE;
    ExecutableCondition condition = mock(ExecutableCondition.class);
    Transform.Result watchTransformResult = mock(Transform.Result.class);
    ExecutableTransform watchTransform = mock(ExecutableTransform.class);
    Throttler.Result throttleResult = mock(Throttler.Result.class);
    ActionThrottler throttler = mock(ActionThrottler.class);
    ExecutableCondition actionCondition = null;
    Condition.Result actionConditionResult = null;
    Transform.Result actionTransformResult = mock(Transform.Result.class);
    ExecutableTransform actionTransform = mock(ExecutableTransform.class);
    Action.Result actionResult = mock(Action.Result.class);
    ExecutableAction action = mock(ExecutableAction.class);
    ActionWrapper actionWrapper = new ActionWrapper("_action", throttler, actionCondition, actionTransform, action, null, null);
    WatchStatus watchStatus = new WatchStatus(now, singletonMap("_action", new ActionStatus(now)));
    setupMocks(watch, getResponse, now, event, context, conditionResult, condition, watchTransformResult, watchTransform, throttleResult, throttler, actionCondition, actionConditionResult, actionTransformResult, actionTransform, actionResult, action, actionWrapper, watchStatus);
    executeTestAndVerifyResults(watch, context, conditionResult, watchTransformResult, actionConditionResult, actionTransformResult, action);
}
