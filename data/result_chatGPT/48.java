/**
 * This method checks if the Completable doc refers to Completable types.
 * @throws Exception
 */
public void completableDocRefersToCompletableTypes() throws Exception {
    List<RxMethod> list = BaseTypeParser.parse(TestHelper.findSource("Completable"), "Completable");
    assertFalse(list.isEmpty());
    StringBuilder e = new StringBuilder();
    for (RxMethod m : list) {
        if (m.javadoc != null) {
            checkJavadocForCompletableTypes(m, e);
        }
    }
    if (e.length() != 0) {
        System.out.println(e);
        fail(e.toString());
    }
}
/**
 * This method checks the Javadoc for Completable types.
 * @param m RxMethod
 * @param e StringBuilder
 */
private void checkJavadocForCompletableTypes(RxMethod m, StringBuilder e) {
    checkJavadocForType(m, e, "onNext", "Publisher", "Flowable", "Observable", "ObservableSource");
    checkJavadocForType(m, e, "Subscriber", "Publisher", "Flowable", "TestSubscriber");
    checkJavadocForType(m, e, " Subscription", "Flowable", "Publisher");
    checkJavadocForType(m, e, "Observer", "ObservableSource", "Observable", "TestObserver");
    checkJavadocForType(m, e, "Publisher", "Publisher");
    checkJavadocForTypeWithPattern(m, e, "Flowable", "Flowable", "@see\\s+#[A-Za-z0-9 _.,()]*Flowable");
    checkJavadocForTypeWithPattern(m, e, "Single", "Single", "@see\\s+#[A-Za-z0-9 _.,()]*Single");
    checkJavadocForTypeWithPattern(m, e, "SingleSource", "SingleSource", "@see\\s+#[A-Za-z0-9 _.,()]*SingleSource");
    checkJavadocForTypeWithPattern(m, e, " Observable", "Observable", "@see\\s+#[A-Za-z0-9 _.,()]*Observable");
    checkJavadocForTypeWithPattern(m, e, "ObservableSource", "ObservableSource", "@see\\s+#[A-Za-z0-9 _.,()]*ObservableSource");
    checkAtReturnAndSignatureMatch("Completable", m, e, "Flowable", "Observable", "Maybe", "Single", "Completable", "Disposable", "Iterable", "Stream", "Future", "CompletionStage");
    aOrAn(e, m, "Completable");
    missingClosingDD(e, m, "Completable", "io.reactivex.rxjava3.core");
    backpressureMentionedWithoutAnnotation(e, m, "Completable");
}
/**
 * This method checks the Javadoc for a specific type.
 * @param m RxMethod
 * @param e StringBuilder
 * @param type String
 * @param types String...
 */
private void checkJavadocForType(RxMethod m, StringBuilder e, String type, String... types) {
    int jdx = 0;
    for (;;) {
        int idx = m.javadoc.indexOf(type, jdx);
        if (idx >= 0) {
            boolean contains = false;
            for (String t : types) {
                if (m.signature.contains(t)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                e.append("java.lang.RuntimeException: Completable doc mentions ").append(type).append(" but not using ").append(Arrays.toString(types)).append("\r\n at io.reactivex.rxjava3.core.")
                .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
            }
            jdx = idx + 6;
        } else {
            break;
        }
    }
}
/**
 * This method checks the Javadoc for a specific type with a pattern.
 * @param m RxMethod
 * @param e StringBuilder
 * @param type String
 * @param signatureType String
 * @param pattern String
 */
private void checkJavadocForTypeWithPattern(RxMethod m, StringBuilder e, String type, String signatureType, String pattern) {
    int jdx = 0;
    for (;;) {
        int idx = m.javadoc.indexOf(type, jdx);
        if (idx >= 0) {
            if (!m.signature.contains(signatureType)) {
                Pattern p = Pattern.compile(pattern);
                if (!p.matcher(m.javadoc).find()) {
                    e.append("java.lang.RuntimeException: Completable doc mentions ").append(type).append(" but not in the signature\r\n at io.reactivex.rxjava3.core.")
                    .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                }
            }
            jdx = idx + 6;
        } else {
            break;
        }
    }
}
