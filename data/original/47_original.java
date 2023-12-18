void checkClass(Class<?> clazz) {
    List<Throwable> errors = TestHelper.trackPluginErrors();
    try {
        StringBuilder b = new StringBuilder();
        int fail = 0;

        outer:
        for (Method m : clazz.getMethods()) {
            if (m.getDeclaringClass() != clazz) {
                continue;
            }

            String key = clazz.getName() + " " + m.getName();

            List<ParamIgnore> ignoreList = ignores.get(key);
            if (ignoreList != null) {
                for (ParamIgnore e : ignoreList) {
                    if (Arrays.equals(e.arguments, m.getParameterTypes())) {
                        System.out.println("CheckClass - ignore: " + m);
                        continue outer;
                    }
                }
            }

            List<ParamOverride> overrideList = overrides.get(key);

            List<Object> baseObjects = new ArrayList<>();

            if ((m.getModifiers() & Modifier.STATIC) != 0) {
                baseObjects.add(null);
                baseObjects.add("NULL");
            } else {
                List<Object> defaultInstancesList = defaultInstances.get(clazz);
                if (defaultInstancesList == null) {
                    b.append("\r\nNo default instances for " + clazz);
                    fail++;
                    continue outer;
                }
                baseObjects.addAll(defaultInstancesList);
            }

            for (int ii = 0; ii < baseObjects.size(); ii += 2) {
                Object baseObject = baseObjects.get(ii);
                Object tag = baseObjects.get(ii + 1);
                Class<?>[] params = m.getParameterTypes();
                int n = params.length;

                for (int i = 0; i < n; i++) {
                    ParamOverride overrideEntry = null;
                    if (overrideList != null) {
                        for (ParamOverride e : overrideList) {
                            if (e.index == i && Arrays.equals(e.arguments, params)) {
                                overrideEntry = e;
                                break;
                            }
                        }
                    }

                    Class<?> entryClass = params[i];

                    Object[] callParams = new Object[n];

                    for (int j = 0; j < n; j++) {
                        if (j != i) {
                            if (params[j].isPrimitive()) {
                                ParamOverride overrideParam = null;
                                if (overrideList != null) {
                                    for (ParamOverride e : overrideList) {
                                        if (e.index == j && Arrays.equals(e.arguments, params)) {
                                            overrideParam = e;
                                            break;
                                        }
                                    }
                                }
                                Object def = defaultPrimitive(params[j], overrideParam);
                                if (def == null) {
                                    b.append("\r\nMissing default non-null value for " + m + " # " + j + " (" + params[j] + ")");
                                    fail++;
                                    continue outer;
                                }
                                callParams[j] = def;
                            } else {
                                Object def = defaultValues.get(params[j]);
                                if (def == null) {
                                    b.append("\r\nMissing default non-null value for " + m + " # " + j + " (" + params[j] + ")");
                                    fail++;
                                    continue outer;
                                }
                                callParams[j] = def;
                            }
                        }
                    }

                    List<Object> entryValues = new ArrayList<>();

                    if (entryClass.isPrimitive()) {
                        addCheckPrimitive(params[i], overrideEntry, entryValues);
                    } else {
                        entryValues.add(null);
                        entryValues.add(overrideEntry != null && overrideEntry.mode == ParamMode.ANY);

                        Object def = defaultValues.get(params[i]);
                        if (def == null) {
                            b.append("\r\nMissing default non-null value for " + m + " # " + i + " (" + params[i] + ")");
                            fail++;
                            continue outer;
                        }
                        entryValues.add(def);
                        entryValues.add(true);
                    }

                    for (int k = 0; k < entryValues.size(); k += 2) {
                        Object[] callParams2 = callParams.clone();

                        Object p = entryValues.get(k);
                        callParams2[i] = p;
                        boolean shouldSucceed = (Boolean)entryValues.get(k + 1);

                        boolean success = false;
                        Throwable error = null;
                        errors.clear();
                        try {
                            m.invoke(baseObject, callParams2);
                            success = true;
                        } catch (Throwable ex) {
                            // let it fail
                            error = ex;
                        }

                        if (!success && error.getCause() instanceof NullPointerException) {
                            if (!error.getCause().toString().contains("is null")) {
                                fail++;
                                b.append("\r\nNPEs should indicate which argument failed: " + m + " # " + i + " = " + p + ", tag = " + tag + ", params = " + Arrays.toString(callParams2));
                            }
                        }
                        if (success != shouldSucceed) {
                            fail++;
                            if (shouldSucceed) {
                                b.append("\r\nFailed (should have succeeded): " + m + " # " + i + " = " + p + ", tag = " + tag + ", params = " + Arrays.toString(callParams2));
                                b.append("\r\n    ").append(error);
                                if (error.getCause() != null) {
                                    b.append("\r\n    ").append(error.getCause());
                                }
                            } else {
                                b.append("\r\nNo failure (should have failed): " + m + " # " + i + " = " + p + ", tag = " + tag + ", params = " + Arrays.toString(callParams2));
                            }
                            continue outer;
                        }
                        if (!errors.isEmpty()) {
                            fail++;
                            b.append("\r\nUndeliverable errors:");
                            for (Throwable err : errors) {
                                b.append("\r\n    ").append(err);
                                if (err.getCause() != null) {
                                    b.append("\r\n    ").append(err.getCause());
                                }
                            }
                            continue outer;
                        }
                    }
                }
            }
        }

        if (fail != 0) {
            throw new AssertionError("Parameter validation problems: " + fail + b.toString());
        }
    } finally {
        RxJavaPlugins.reset();
    }
}