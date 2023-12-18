public void testExecuteFailedActionTransform() throws Exception {
    Watch watch = mock(Watch.class);
    when(watch.id()).thenReturn("_id");
    GetResponse getResponse = mock(GetResponse.class);
    when(getResponse.isExists()).thenReturn(true);
    mockGetWatchResponse(client, "_id", getResponse);
    when(parser.parseWithSecrets(eq(watch.id()), eq(true), any(), any(), any(), anyLong(), anyLong())).thenReturn(watch);

    ZonedDateTime now = clock.instant().atZone(ZoneOffset.UTC);
    ScheduleTriggerEvent event = new ScheduleTriggerEvent("_id", now, now);
    TriggeredExecutionContext context = new TriggeredExecutionContext(watch.id(), now, event, timeValueSeconds(5));

    Condition.Result conditionResult = InternalAlwaysCondition.RESULT_INSTANCE;
    ExecutableCondition condition = mock(ExecutableCondition.class);
    when(condition.execute(any(WatchExecutionContext.class))).thenReturn(conditionResult);

    // watch level transform
    Transform.Result watchTransformResult = mock(Transform.Result.class);
    when(watchTransformResult.status()).thenReturn(Transform.Result.Status.SUCCESS);
    when(watchTransformResult.payload()).thenReturn(payload);
    ExecutableTransform watchTransform = mock(ExecutableTransform.class);
    when(watchTransform.execute(context, payload)).thenReturn(watchTransformResult);

    // action throttler
    Throttler.Result throttleResult = mock(Throttler.Result.class);
    when(throttleResult.throttle()).thenReturn(false);
    ActionThrottler throttler = mock(ActionThrottler.class);
    when(throttler.throttle("_action", context)).thenReturn(throttleResult);

    // action level condition
    ExecutableCondition actionCondition = null;
    Condition.Result actionConditionResult = null;

    if (randomBoolean()) {
        Tuple<ExecutableCondition, Condition.Result> pair = whenCondition(context);

        actionCondition = pair.v1();
        actionConditionResult = pair.v2();
    }

    // action level transform
    Transform.Result actionTransformResult = mock(Transform.Result.class);
    when(actionTransformResult.status()).thenReturn(Transform.Result.Status.FAILURE);
    when(actionTransformResult.reason()).thenReturn("_reason");
    ExecutableTransform actionTransform = mock(ExecutableTransform.class);
    when(actionTransform.execute(context, payload)).thenReturn(actionTransformResult);

    // the action
    Action.Result actionResult = mock(Action.Result.class);
    when(actionResult.type()).thenReturn("_action_type");
    when(actionResult.status()).thenReturn(Action.Result.Status.SUCCESS);
    ExecutableAction action = mock(ExecutableAction.class);
    when(action.logger()).thenReturn(logger);
    when(action.execute("_action", context, payload)).thenReturn(actionResult);

    ActionWrapper actionWrapper = new ActionWrapper("_action", throttler, actionCondition, actionTransform, action, null, null);

    WatchStatus watchStatus = new WatchStatus(now, singletonMap("_action", new ActionStatus(now)));

    when(watch.input()).thenReturn(input);
    when(watch.condition()).thenReturn(condition);
    when(watch.transform()).thenReturn(watchTransform);
    when(watch.actions()).thenReturn(Arrays.asList(actionWrapper));
    when(watch.status()).thenReturn(watchStatus);

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
    // the action level transform is executed before the action itself
    verify(action, never()).execute("_action", context, payload);
}