/**
 * This method checks the class for any errors.
 * @param clazz The class to be checked.
 */
void checkClass(Class<?> clazz) {
    List<Throwable> errors = TestHelper.trackPluginErrors();
    try {
        StringBuilder b = new StringBuilder();
        int fail = 0;
        for (Method m : clazz.getMethods()) {
            if (m.getDeclaringClass() != clazz) {
                continue;
            }
            String key = clazz.getName() + " " + m.getName();
            fail += checkMethod(clazz, m, key, b, errors);
        }
        if (fail != 0) {
            throw new AssertionError("Parameter validation problems: " + fail + b.toString());
        }
    } finally {
        RxJavaPlugins.reset();
    }
}
/**
 * This method checks a method for any errors.
 * @param clazz The class the method belongs to.
 * @param m The method to be checked.
 * @param key The key used to retrieve ignore and override lists.
 * @param b The StringBuilder used to append error messages.
 * @param errors The list of errors.
 * @return The number of failures.
 */
int checkMethod(Class<?> clazz, Method m, String key, StringBuilder b, List<Throwable> errors) {
    int fail = 0;
    List<ParamIgnore> ignoreList = ignores.get(key);
    if (ignoreList != null) {
        for (ParamIgnore e : ignoreList) {
            if (Arrays.equals(e.arguments, m.getParameterTypes())) {
                System.out.println("CheckClass - ignore: " + m);
                return fail;
            }
        }
    }
    List<ParamOverride> overrideList = overrides.get(key);
    List<Object> baseObjects = getBaseObjects(clazz, m, b);
    if (baseObjects == null) {
        fail++;
        return fail;
    }
    for (int ii = 0; ii < baseObjects.size(); ii += 2) {
        fail += checkBaseObject(m, baseObjects, ii, overrideList, b, errors);
    }
    return fail;
}
/**
 * This method gets the base objects for a method.
 * @param clazz The class the method belongs to.
 * @param m The method to get base objects for.
 * @param b The StringBuilder used to append error messages.
 * @return The list of base objects.
 */
List<Object> getBaseObjects(Class<?> clazz, Method m, StringBuilder b) {
    List<Object> baseObjects = new ArrayList<>();
    if ((m.getModifiers() & Modifier.STATIC) != 0) {
        baseObjects.add(null);
        baseObjects.add("NULL");
    } else {
        List<Object> defaultInstancesList = defaultInstances.get(clazz);
        if (defaultInstancesList == null) {
            b.append("\r\nNo default instances for " + clazz);
            return null;
        }
        baseObjects.addAll(defaultInstancesList);
    }
    return baseObjects;
}
/**
 * This method checks a base object for any errors.
 * @param m The method the base object belongs to.
 * @param baseObjects The list of base objects.
 * @param ii The index of the base object in the list.
 * @param overrideList The list of overrides.
 * @param b The StringBuilder used to append error messages.
 * @param errors The list of errors.
 * @return The number of failures.
 */
int checkBaseObject(Method m, List<Object> baseObjects, int ii, List<ParamOverride> overrideList, StringBuilder b, List<Throwable> errors) {
    int fail = 0;
    Object baseObject = baseObjects.get(ii);
    Object tag = baseObjects.get(ii + 1);
    Class<?>[] params = m.getParameterTypes();
    int n = params.length;
    for (int i = 0; i < n; i++) {
        fail += checkParam(m, baseObject, tag, params, i, overrideList, b, errors);
    }
    return fail;
}
// Additional methods checkParam, defaultPrimitive, addCheckPrimitive, etc. would be defined here.
