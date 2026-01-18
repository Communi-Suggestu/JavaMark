package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.utils.TypeLinkProvider;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnotationBuilder
{

    private final TypeUniverse typeUniverse;
    private final TypeLinkProvider typeLinkProvider;
    private final boolean lineBreak;

    private final StringBuilder results = new StringBuilder();

    private boolean isAnnotationDocumented = false;
    private boolean isContainerDocumented = false;

    public AnnotationBuilder(final TypeUniverse typeUniverse, final TypeLinkProvider typeLinkProvider)
    {
        this.typeUniverse = typeUniverse;
        this.typeLinkProvider = typeLinkProvider;
        this.lineBreak = false;
    }

    private AnnotationBuilder(final TypeUniverse typeUniverse, final TypeLinkProvider typeLinkProvider, final boolean lineBreak) {
        this.typeUniverse = typeUniverse;
        this.typeLinkProvider = typeLinkProvider;
        this.lineBreak = lineBreak;
    }

    private AnnotationBuilder withLineBreak(boolean lineBreak) {
        if (this.lineBreak == lineBreak)
            return this;

        return new AnnotationBuilder(typeUniverse, typeLinkProvider, lineBreak);
    }

    public AnnotationBuilder withLineBreak() {
        return withLineBreak(true);
    }

    public AnnotationBuilder withoutLineBreak() {
        return withLineBreak(false);
    }

    public AnnotationBuilder from(List<? extends AnnotationMirror> descList) {
        StringBuilder annotation;
        for (AnnotationMirror aDesc : descList) {
            TypeElement annotationElement = (TypeElement)aDesc.getAnnotationType().asElement();
            // If an annotation is not documented, do not add it to the list. If
            // the annotation is of a repeatable type, and if it is not documented
            // and also if its container annotation is not documented, do not add it
            // to the list. If an annotation of a repeatable type is not documented
            // but its container is documented, it will be added to the list.
            if (!typeUniverse.isDocumentedAnnotation(annotationElement) &&
                (!isAnnotationDocumented && !isContainerDocumented)) {
                continue;
            }

            annotation = new StringBuilder();
            isAnnotationDocumented = false;
            Map<? extends ExecutableElement, ? extends AnnotationValue> pairs = aDesc.getElementValues();
            // If the annotation is mandated, do not print the container.
            if (typeUniverse.isMandated(aDesc)) {
                for (ExecutableElement ee : pairs.keySet()) {
                    AnnotationValue annotationValue = pairs.get(ee);
                    List<AnnotationValue> annotationTypeValues = new ArrayList<>();

                    new SimpleAnnotationValueVisitor9<Void, List<AnnotationValue>>() {
                        @Override
                        public Void visitArray(List<? extends AnnotationValue> vals, List<AnnotationValue> p) {
                            p.addAll(vals);
                            return null;
                        }

                        @Override
                        protected Void defaultAction(Object o, List<AnnotationValue> p) {
                            p.add(annotationValue);
                            return null;
                        }
                    }.visit(annotationValue, annotationTypeValues);

                    String sep = "";
                    for (AnnotationValue av : annotationTypeValues) {
                        annotation.append(sep);
                        annotation.append(annotationValueToContent(av));
                        sep = " ";
                    }
                }
            } else if (isAnnotationArray(pairs)) {
                // If the container has 1 or more value defined and if the
                // repeatable type annotation is not documented, do not print
                // the container.
                if (pairs.size() == 1 && isAnnotationDocumented) {
                    List<AnnotationValue> annotationTypeValues = new ArrayList<>();
                    for (AnnotationValue a :  pairs.values()) {
                        new SimpleAnnotationValueVisitor9<Void, List<AnnotationValue>>() {
                            @Override
                            public Void visitArray(List<? extends AnnotationValue> vals, List<AnnotationValue> annotationTypeValues) {
                                annotationTypeValues.addAll(vals);
                                return null;
                            }
                        }.visit(a, annotationTypeValues);
                    }
                    String sep = "";
                    for (AnnotationValue av : annotationTypeValues) {
                        annotation.append(sep);
                        annotation.append(annotationValueToContent(av));
                        sep = " ";
                    }
                }
                // If the container has 1 or more value defined and if the
                // repeatable type annotation is not documented, print the container.
                else {
                    addAnnotations(annotationElement, annotation, pairs, false);
                }
            }
            else {
                addAnnotations(annotationElement, annotation, pairs, this.lineBreak);
            }
            annotation.append(lineBreak ? Constants.NEW_LINE : "");
            results.append(annotation);
        }

        return this;
    }

    private void addAnnotations(
        TypeElement annotationDoc,
        StringBuilder annotation,
        Map<? extends ExecutableElement, ? extends AnnotationValue> map,
        boolean lineBreak) {
        //TODO: Figure out what goes here and why it needs a link.
        annotation.append("<BLAH>");
        if (!map.isEmpty()) {
            annotation.append("(");
            boolean isFirst = true;
            Set<? extends ExecutableElement> keys = map.keySet();
            boolean multipleValues = keys.size() > 1;
            for (ExecutableElement element : keys) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    annotation.append(",");
                    if (lineBreak) {
                        annotation.append(Constants.NEW_LINE);
                        int spaces = annotationDoc.getSimpleName().length() + 2;
                        for (int k = 0; k < (spaces); k++) {
                            annotation.append(" ");
                        }
                    }
                }
                String simpleName = element.getSimpleName().toString();
                if (multipleValues || !"value".equals(simpleName)) { // Omit "value=" where unnecessary
                    //TODO: Consider providing a link to a property
                    annotation.append(simpleName);
                    annotation.append("=");
                }
                AnnotationValue annotationValue = map.get(element);
                List<AnnotationValue> annotationTypeValues = new ArrayList<>();
                new SimpleAnnotationValueVisitor9<Void, AnnotationValue>() {
                    @Override
                    public Void visitArray(List<? extends AnnotationValue> vals, AnnotationValue p) {
                        annotationTypeValues.addAll(vals);
                        return null;
                    }
                    @Override
                    protected Void defaultAction(Object o, AnnotationValue p) {
                        annotationTypeValues.add(p);
                        return null;
                    }
                }.visit(annotationValue, annotationValue);
                annotation.append(annotationTypeValues.size() == 1 ? "" : "{");
                String sep = "";
                for (AnnotationValue av : annotationTypeValues) {
                    annotation.append(sep);
                    annotation.append(annotationValueToContent(av));
                    sep = ",";
                }
                annotation.append(annotationTypeValues.size() == 1 ? "" : "}");
                isContainerDocumented = false;
            }
            annotation.append(")");
        }
    }

    private boolean isAnnotationArray(Map<? extends ExecutableElement, ? extends AnnotationValue> pairs) {
        AnnotationValue annotationValue;
        for (ExecutableElement ee : pairs.keySet()) {
            annotationValue = pairs.get(ee);
            boolean rvalue = new SimpleAnnotationValueVisitor9<Boolean, Void>() {
                @Override
                public Boolean visitArray(List<? extends AnnotationValue> vals, Void p) {
                    if (vals.size() > 1) {
                        if (vals.getFirst() instanceof AnnotationMirror) {
                            isContainerDocumented = true;
                            return new SimpleAnnotationValueVisitor9<Boolean, Void>() {
                                @Override
                                public Boolean visitAnnotation(AnnotationMirror a, Void p) {
                                    isContainerDocumented = true;
                                    Element asElement = a.getAnnotationType().asElement();
                                    if (typeUniverse.isDocumentedAnnotation((TypeElement)asElement)) {
                                        isAnnotationDocumented = true;
                                    }
                                    return true;
                                }
                                @Override
                                protected Boolean defaultAction(Object o, Void p) {
                                    return false;
                                }
                            }.visit(vals.getFirst());
                        }
                    }
                    return false;
                }

                @Override
                protected Boolean defaultAction(Object o, Void p) {
                    return false;
                }
            }.visit(annotationValue);
            if (rvalue) {
                return true;
            }
        }
        return false;
    }

    private String annotationValueToContent(AnnotationValue annotationValue) {
        return new SimpleAnnotationValueVisitor9<String, Void>() {
            @Override
            public String visitType(TypeMirror t, Void p) {
                return t + typeUniverse.getDimension(t) + ".class";
            }

            @Override
            public String visitAnnotation(AnnotationMirror a, Void p) {
                return new AnnotationBuilder(typeUniverse, typeLinkProvider, false)
                    .from(List.of(a))
                    .build();
            }

            //TODO: Enum constant visiting, needs generic URL generation to sub sections of files.

            @Override
            public String visitArray(List<? extends AnnotationValue> vals, Void p) {
                StringBuilder buf = new StringBuilder();
                String sep = "";
                for (AnnotationValue av : vals) {
                    buf.append(sep);
                    buf.append(visit(av));
                    sep = " ";
                }
                return buf.toString();
            }

            @Override
            protected String defaultAction(Object o, Void p) {
                return annotationValue.toString();
            }
        }.visit(annotationValue);
    }

    public String build() {
        return results.toString();
    }
}
