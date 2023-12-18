public void completableDocRefersToCompletableTypes() throws Exception {
    List<RxMethod> list = BaseTypeParser.parse(TestHelper.findSource("Completable"), "Completable");

    assertFalse(list.isEmpty());

    StringBuilder e = new StringBuilder();

    for (RxMethod m : list) {
        int jdx;
        if (m.javadoc != null) {
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("onNext", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("Publisher")
                            && !m.signature.contains("Flowable")
                            && !m.signature.contains("Observable")
                            && !m.signature.contains("ObservableSource")) {
                        e.append("java.lang.RuntimeException: Completable doc mentions onNext but no Flowable/Observable in signature\r\n at io.reactivex.rxjava3.core.")
                        .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                    }

                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("Subscriber", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("Publisher")
                            && !m.signature.contains("Flowable")
                            && !m.signature.contains("TestSubscriber")) {
                        e.append("java.lang.RuntimeException: Completable doc mentions Subscriber but not using Flowable\r\n at io.reactivex.rxjava3.core.")
                        .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                    }

                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf(" Subscription", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("Flowable")
                            && !m.signature.contains("Publisher")
                    ) {
                        e.append("java.lang.RuntimeException: Completable doc mentions Subscription but not using Flowable\r\n at io.reactivex.rxjava3.core.")
                        .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                    }

                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("Observer", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("ObservableSource")
                            && !m.signature.contains("Observable")
                            && !m.signature.contains("TestObserver")) {

                        if (idx < 11 || !m.javadoc.substring(idx - 11, idx + 8).equals("CompletableObserver")) {
                            e.append("java.lang.RuntimeException: Completable doc mentions Observer but not using Observable\r\n at io.reactivex.rxjava3.core.")
                            .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                        }
                    }

                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("Publisher", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("Publisher")) {
                        if (idx == 0 || !m.javadoc.substring(idx - 1, idx + 9).equals("(Publisher")) {
                            e.append("java.lang.RuntimeException: Completable doc mentions Publisher but not in the signature\r\n at io.reactivex.rxjava3.core.")
                            .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                        }
                    }

                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("Flowable", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("Flowable")) {
                        Pattern p = Pattern.compile("@see\\s+#[A-Za-z0-9 _.,()]*Flowable");
                        if (!p.matcher(m.javadoc).find()) {
                            e.append("java.lang.RuntimeException: Completable doc mentions Flowable but not in the signature\r\n at io.reactivex.rxjava3.core.")
                            .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                        }
                    }
                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("Single", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("Single")) {
                        Pattern p = Pattern.compile("@see\\s+#[A-Za-z0-9 _.,()]*Single");
                        if (!p.matcher(m.javadoc).find()) {
                            e.append("java.lang.RuntimeException: Completable doc mentions Single but not in the signature\r\n at io.reactivex.rxjava3.core.")
                            .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                        }
                    }
                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("SingleSource", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("SingleSource")) {
                        Pattern p = Pattern.compile("@see\\s+#[A-Za-z0-9 _.,()]*SingleSource");
                        if (!p.matcher(m.javadoc).find()) {
                            e.append("java.lang.RuntimeException: Completable doc mentions SingleSource but not in the signature\r\n at io.reactivex.rxjava3.core.")
                            .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                        }
                    }
                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf(" Observable", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("Observable")) {
                        Pattern p = Pattern.compile("@see\\s+#[A-Za-z0-9 _.,()]*Observable");
                        if (!p.matcher(m.javadoc).find()) {
                            e.append("java.lang.RuntimeException: Completable doc mentions Observable but not in the signature\r\n at io.reactivex.rxjava3.core.")
                            .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                        }
                    }
                    jdx = idx + 6;
                } else {
                    break;
                }
            }
            jdx = 0;
            for (;;) {
                int idx = m.javadoc.indexOf("ObservableSource", jdx);
                if (idx >= 0) {
                    if (!m.signature.contains("ObservableSource")) {
                        Pattern p = Pattern.compile("@see\\s+#[A-Za-z0-9 _.,()]*ObservableSource");
                        if (!p.matcher(m.javadoc).find()) {
                            e.append("java.lang.RuntimeException: Completable doc mentions ObservableSource but not in the signature\r\n at io.reactivex.rxjava3.core.")
                            .append("Completable.method(Completable.java:").append(m.javadocLine + lineNumber(m.javadoc, idx) - 1).append(")\r\n\r\n");
                        }
                    }
                    jdx = idx + 6;
                } else {
                    break;
                }
            }

            checkAtReturnAndSignatureMatch("Completable", m, e, "Flowable", "Observable", "Maybe", "Single", "Completable", "Disposable", "Iterable", "Stream", "Future", "CompletionStage");

            aOrAn(e, m, "Completable");
            missingClosingDD(e, m, "Completable", "io.reactivex.rxjava3.core");
            backpressureMentionedWithoutAnnotation(e, m, "Completable");
        }
    }

    if (e.length() != 0) {
        System.out.println(e);

        fail(e.toString());
    }
}