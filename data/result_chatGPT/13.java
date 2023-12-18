/**
 * This method is used to get the type for factory method.
 * @param beanName the name of the bean.
 * @param mbd the root bean definition.
 * @param typesToMatch the types to match.
 * @return the class type.
 */
protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
    ResolvableType cachedReturnType = mbd.factoryMethodReturnType;
    if (cachedReturnType != null) {
        return cachedReturnType.resolve();
    }
    Class<?> commonType = null;
    Method uniqueCandidate = mbd.factoryMethodToIntrospect;
    if (uniqueCandidate == null) {
        Class<?> factoryClass = getFactoryClass(beanName, mbd, typesToMatch);
        if (factoryClass == null) {
            return null;
        }
        commonType = getCommonType(mbd, factoryClass, commonType);
        if (commonType == null) {
            return null;
        }
        mbd.factoryMethodToIntrospect = uniqueCandidate;
    }
    return getReturnType(mbd, uniqueCandidate, commonType);
}
/**
 * This method is used to get the factory class.
 * @param beanName the name of the bean.
 * @param mbd the root bean definition.
 * @param typesToMatch the types to match.
 * @return the factory class.
 */
private Class<?> getFactoryClass(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
    Class<?> factoryClass;
    boolean isStatic = true;
    String factoryBeanName = mbd.getFactoryBeanName();
    if (factoryBeanName != null) {
        if (factoryBeanName.equals(beanName)) {
            throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
                    "factory-bean reference points back to the same bean definition");
        }
        factoryClass = getType(factoryBeanName);
        isStatic = false;
    } else {
        factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
    }
    return factoryClass != null ? ClassUtils.getUserClass(factoryClass) : null;
}
/**
 * This method is used to get the common type.
 * @param mbd the root bean definition.
 * @param factoryClass the factory class.
 * @param commonType the common type.
 * @return the common type.
 */
private Class<?> getCommonType(RootBeanDefinition mbd, Class<?> factoryClass, Class<?> commonType) {
    int minNrOfArgs = (mbd.hasConstructorArgumentValues() ? mbd.getConstructorArgumentValues().getArgumentCount() : 0);
    Method[] candidates = this.factoryMethodCandidateCache.computeIfAbsent(factoryClass,
            clazz -> ReflectionUtils.getUniqueDeclaredMethods(clazz, ReflectionUtils.USER_DECLARED_METHODS));
    for (Method candidate : candidates) {
        if (isCandidateEligible(mbd, factoryClass, candidate, minNrOfArgs)) {
            commonType = determineCommonType(mbd, commonType, candidate);
            if (commonType == null) {
                return null;
            }
        }
    }
    return commonType;
}
/**
 * This method is used to check if the candidate is eligible.
 * @param mbd the root bean definition.
 * @param factoryClass the factory class.
 * @param candidate the candidate method.
 * @param minNrOfArgs the minimum number of arguments.
 * @return true if the candidate is eligible, false otherwise.
 */
private boolean isCandidateEligible(RootBeanDefinition mbd, Class<?> factoryClass, Method candidate, int minNrOfArgs) {
    return Modifier.isStatic(candidate.getModifiers()) == factoryClass.isInterface() && mbd.isFactoryMethod(candidate) &&
            candidate.getParameterCount() >= minNrOfArgs;
}
/**
 * This method is used to determine the common type.
 * @param mbd the root bean definition.
 * @param commonType the common type.
 * @param candidate the candidate method.
 * @return the common type.
 */
private Class<?> determineCommonType(RootBeanDefinition mbd, Class<?> commonType, Method candidate) {
    if (candidate.getTypeParameters().length > 0) {
        try {
            Class<?> returnType = resolveReturnTypeForFactoryMethod(mbd, candidate);
            commonType = ClassUtils.determineCommonAncestor(returnType, commonType);
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to resolve generic return type for factory method: " + ex);
            }
        }
    } else {
        commonType = ClassUtils.determineCommonAncestor(candidate.getReturnType(), commonType);
    }
    return commonType;
}
/**
 * This method is used to resolve the return type for factory method.
 * @param mbd the root bean definition.
 * @param candidate the candidate method.
 * @return the return type.
 */
private Class<?> resolveReturnTypeForFactoryMethod(RootBeanDefinition mbd, Method candidate) {
    ConstructorArgumentValues cav = mbd.getConstructorArgumentValues();
    Class<?>[] paramTypes = candidate.getParameterTypes();
    String[] paramNames = null;
    if (cav.containsNamedArgument()) {
        ParameterNameDiscoverer pnd = getParameterNameDiscoverer();
        if (pnd != null) {
            paramNames = pnd.getParameterNames(candidate);
        }
    }
    Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = new HashSet<>(paramTypes.length);
    Object[] args = new Object[paramTypes.length];
    for (int i = 0; i < args.length; i++) {
        ConstructorArgumentValues.ValueHolder valueHolder = cav.getArgumentValue(
                i, paramTypes[i], (paramNames != null ? paramNames[i] : null), usedValueHolders);
        if (valueHolder == null) {
            valueHolder = cav.getGenericArgumentValue(null, null, usedValueHolders);
        }
        if (valueHolder != null) {
            args[i] = valueHolder.getValue();
            usedValueHolders.add(valueHolder);
        }
    }
    return AutowireUtils.resolveReturnTypeForFactoryMethod(candidate, args, getBeanClassLoader());
}
/**
 * This method is used to get the return type.
 * @param mbd the root bean definition.
 * @param uniqueCandidate the unique candidate method.
 * @param commonType the common type.
 * @return the return type.
 */
private Class<?> getReturnType(RootBeanDefinition mbd, Method uniqueCandidate, Class<?> commonType) {
    ResolvableType cachedReturnType = (uniqueCandidate != null ?
            ResolvableType.forMethodReturnType(uniqueCandidate) : ResolvableType.forClass(commonType));
    mbd.factoryMethodReturnType = cachedReturnType;
    return cachedReturnType.resolve();
}
