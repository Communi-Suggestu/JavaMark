package com.communi.suggestu.javamark.doclet.utils;

import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.BlockTagTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EscapeTree;
import com.sun.source.doctree.InlineTagTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ProvidesTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.SpecTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UsesTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.util.SimpleDocTreeVisitor;
import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor14;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.Types;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.sun.source.doctree.DocTree.Kind.ERRONEOUS;
import static com.sun.source.doctree.DocTree.Kind.HIDDEN;
import static com.sun.source.doctree.DocTree.Kind.PARAM;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.ElementKind.STATIC_INIT;
import static javax.lang.model.element.ElementKind.TYPE_PARAMETER;
import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * TypeUniverse provides utilities to navigate and introspect Java types using TypeMirror and related APIs.
 */
public class TypeUniverse {
    private final DocletEnvironment docletEnvironment;
    private final Elements elementUtils;
    private final Types typeUtils;
    private final Set<TypeElement> knownTypes;
    private final Set<PackageElement> knownPackages;
    // New fields for supertype and subtype tracking
    private final Map<TypeElement, TypeElement> directSuperTypeMap;
    private final Map<TypeElement, Set<TypeElement>> directSubTypesMap;

    public TypeUniverse(final DocletEnvironment docletEnvironment, Elements elementUtils, Types typeUtils, final Set<TypeElement> knownTypes, final Set<PackageElement> knownPackages) {
        this.docletEnvironment = docletEnvironment;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.knownTypes = knownTypes;
        this.knownPackages = knownPackages;
        this.directSuperTypeMap = new HashMap<>();
        this.directSubTypesMap = new HashMap<>();
        buildSuperAndSubTypeMaps();
    }

    // Build the supertype and subtype maps
    private void buildSuperAndSubTypeMaps() {
        // First, initialize all sets
        for (TypeElement type : knownTypes) {
            directSubTypesMap.put(type, new HashSet<>());
        }
        // Now, for each type, find its direct supertype and update both maps
        for (TypeElement type : knownTypes) {
            TypeMirror superTypeMirror = type.getSuperclass();
            TypeElement superType = asTypeElement(superTypeMirror);
            if (superType != null && knownTypes.contains(superType)) {
                directSuperTypeMap.put(type, superType);
                directSubTypesMap.get(superType).add(type);
            }
        }
    }

    /**
     * Returns the TypeElement for a given TypeMirror.
     */
    public TypeElement asTypeElement(TypeMirror typeMirror) {
        var element = typeUtils.asElement(typeMirror);
        if (element instanceof TypeElement typeElement)
            return typeElement;

        return null;
    }

    /**
     * Returns the TypeElement for a given TypeMirror.
     */
    public Element asElement(TypeMirror typeMirror) {
        return typeUtils.asElement(typeMirror);
    }

    /**
     * Returns the package hierarchy for a given TypeMirror.
     */
    public List<String> getPackageHierarchy(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return Collections.emptyList();
        PackageElement pkg = elementUtils.getPackageOf(typeElement);
        return getPackageHierarchy(pkg);
    }

    public List<String> getPackageHierarchy(PackageElement pkg)
    {
        List<String> hierarchy = new ArrayList<>();
        while (pkg != null && !pkg.isUnnamed()) {
            hierarchy.add(0, pkg.getSimpleName().toString());
            Element enclosing = pkg.getEnclosingElement();
            if (enclosing instanceof PackageElement) {
                pkg = (PackageElement) enclosing;
            } else {
                break;
            }
        }
        return hierarchy;
    }

    /**
     * Returns the direct superclass of the given type, or null if none.
     */
    public TypeMirror getSuperType(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return null;
        return typeElement.getSuperclass();
    }

    /**
     * Returns the interfaces implemented or extended by the given type.
     */
    public List<TypeMirror> getInterfaces(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return Collections.emptyList();
        return new ArrayList<>(typeElement.getInterfaces());
    }

    /**
     * Returns all methods declared in the given type (excluding inherited methods).
     */
    public List<ExecutableElement> getDeclaredMethods(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return Collections.emptyList();
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());
    }

    /**
     * Returns the parameters of a given method.
     */
    public List<? extends VariableElement> getMethodParameters(ExecutableElement method) {
        return method.getParameters();
    }

    /**
     * Returns all supertypes (superclass and interfaces, recursively) for the given type.
     */
    public Set<TypeMirror> getAllSuperTypes(TypeMirror typeMirror) {
        Set<TypeMirror> result = new java.util.HashSet<>();
        collectSuperTypes(typeMirror, result);
        return result;
    }

    private void collectSuperTypes(TypeMirror typeMirror, Set<TypeMirror> result) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return;
        TypeMirror superClass = typeElement.getSuperclass();
        if (superClass != null && typeUtils.asElement(superClass) != null) {
            if (result.add(superClass)) {
                collectSuperTypes(superClass, result);
            }
        }
        for (TypeMirror iface : typeElement.getInterfaces()) {
            if (result.add(iface)) {
                collectSuperTypes(iface, result);
            }
        }
    }

    public List<TypeElement> getTypesInPackage(PackageElement pkg) {
        if (pkg == null) return Collections.emptyList();
        return knownTypes.stream()
            .filter(t -> {
                PackageElement enclosing = com.communi.suggestu.javamark.doclet.utils.ElementUtils.getEnclosingPackage(t);
                return enclosing != null && enclosing.equals(pkg);
            })
            .collect(Collectors.toList());
    }

    public PackageElement getParentOf(PackageElement pkg) {
        if (!(pkg.getEnclosingElement() instanceof PackageElement packageElement))
            return null;

        return packageElement;
    }

    public List<PackageElement> getChildPackages(PackageElement pkg) {
        return knownPackages.stream()
            .filter(p -> isDirectChild(pkg, p)).toList();
    }

    private static boolean isDirectChild(PackageElement parent, PackageElement child) {
        String parentName = parent.getQualifiedName().toString();
        String childName = child.getQualifiedName().toString();
        if (!childName.startsWith(parentName + ".")) return false;
        return childName.substring(parentName.length() + 1).indexOf('.') < 0;
    }

    public boolean contains(final TypeMirror targetType)
    {
        return knownTypes.contains(asTypeElement(targetType));
    }

    /**
     * Returns the direct supertype (TypeElement) of the given type, or null if none.
     */
    public TypeElement getDirectSuperType(TypeElement type) {
        return directSuperTypeMap.get(type);
    }

    /**
     * Returns the set of direct subtypes (TypeElement) of the given type.
     */
    public Set<TypeElement> getDirectSubTypes(TypeElement type) {
        return directSubTypesMap.getOrDefault(type, Collections.emptySet());
    }

    public String getTypeName(TypeMirror t, boolean fullyQualified) {
        return new SimpleTypeVisitor14<String, Void>() {

            @Override
            public String visitArray(ArrayType t, Void p) {
                return visit(t.getComponentType());
            }

            @Override
            public String visitDeclared(DeclaredType t, Void p) {
                TypeElement te = asTypeElement(t);
                return fullyQualified
                    ? te.getQualifiedName().toString()
                    : getSimpleName(te);
            }

            @Override
            public String visitExecutable(ExecutableType t, Void p) {
                return t.toString();
            }

            @Override
            public String visitPrimitive(PrimitiveType t, Void p) {
                return t.toString();
            }

            @Override
            public String visitTypeVariable(javax.lang.model.type.TypeVariable t, Void p) {
                return getSimpleName(t.asElement());
            }

            @Override
            public String visitWildcard(javax.lang.model.type.WildcardType t, Void p) {
                return t.toString();
            }

            @Override
            protected String defaultAction(TypeMirror e, Void p) {
                return e.toString();
            }
        }.visit(t);
    }

    /**
     * Returns the name of the element after the last dot of the package name.
     * This emulates the behavior of the old doclet.
     * @param e an element whose name is required
     * @return the name
     */
    public String getSimpleName(Element e) {
        return getSimpleName0(e);
    }

    // If `e` is a static nested class, this method will return e's simple name
    // preceded by `.` and an outer type; this is not how JLS defines "simple
    // name". See "Simple Name", "Qualified Name", "Fully Qualified Name".
    private String getSimpleName0(Element e) {
        SimpleElementVisitor14<String, Void> visitor = new SimpleElementVisitor14<>() {
            @Override
            public String visitModule(ModuleElement e, Void p) {
                return e.getQualifiedName().toString();  // temp fix for 8182736
            }

            @Override
            public String visitType(TypeElement e, Void p) {
                StringBuilder sb = new StringBuilder(e.getSimpleName().toString());
                Element enclosed = e.getEnclosingElement();
                while (enclosed != null
                    && (enclosed.getKind().isDeclaredType())) {
                    sb.insert(0, enclosed.getSimpleName() + ".");
                    enclosed = enclosed.getEnclosingElement();
                }
                return sb.toString();
            }

            @Override
            public String visitExecutable(ExecutableElement e, Void p) {
                if (e.getKind() == CONSTRUCTOR || e.getKind() == STATIC_INIT) {
                    return e.getEnclosingElement().getSimpleName().toString();
                }
                return e.getSimpleName().toString();
            }

            @Override
            protected String defaultAction(Element e, Void p) {
                return e.getSimpleName().toString();
            }
        };

        return visitor.visit(e);
    }

    public boolean isAnnotated(TypeMirror e) {
        return !e.getAnnotationMirrors().isEmpty();
    }

    /**
     * Given an annotation, return true if it should be documented and false
     * otherwise.
     *
     * @param annotation the annotation to check.
     *
     * @return true return true if it should be documented and false otherwise.
     */
    public boolean isDocumentedAnnotation(TypeElement annotation) {
        for (AnnotationMirror anno : annotation.getAnnotationMirrors()) {
            if (getFullyQualifiedName(anno.getAnnotationType().asElement()).equals(
                Documented.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * A generic utility which returns the fully qualified names of an entity,
     * if the entity is not qualifiable then its enclosing entity, it is up to
     * the caller to add the elements name as required.
     * @param e the element to get FQN for.
     * @return the name
     */
    public String getFullyQualifiedName(Element e) {
        return getFullyQualifiedName(e, true);
    }

    public String getFullyQualifiedName(Element e, final boolean outer) {
        return new SimpleElementVisitor14<String, Void>() {
            @Override
            public String visitModule(ModuleElement e, Void p) {
                return e.getQualifiedName().toString();
            }

            @Override
            public String visitPackage(PackageElement e, Void p) {
                return e.getQualifiedName().toString();
            }

            @Override
            public String visitType(TypeElement e, Void p) {
                return e.getQualifiedName().toString();
            }

            @Override
            protected String defaultAction(Element e, Void p) {
                return outer ? visit(e.getEnclosingElement()) : e.getSimpleName().toString();
            }
        }.visit(e);
    }

    public boolean isMandated(AnnotationMirror aDesc) {
        return elementUtils.getOrigin(null, aDesc) == Elements.Origin.MANDATED;
    }

    public boolean isIncluded(Element e) {
        return docletEnvironment.isIncluded(e);
    }

    /**
     * Return the type's dimension information, as a string.
     * <p>
     * For example, a two dimensional array of String returns "{@code [][]}".
     *
     * @return the type's dimension information as a string.
     */
    public String getDimension(TypeMirror t) {
        return new SimpleTypeVisitor14<String, Void>() {
            StringBuilder dimension = new StringBuilder();
            @Override
            public String visitArray(ArrayType t, Void p) {
                dimension.append("[]");
                return visit(t.getComponentType());
            }

            @Override
            protected String defaultAction(TypeMirror e, Void p) {
                return dimension.toString();
            }

        }.visit(t);
    }


    public boolean isArrayType(TypeMirror t) {
        return t.getKind() == ARRAY;
    }

    public boolean isDeclaredType(TypeMirror t) {
        return t.getKind() == DECLARED;
    }

    public boolean isTypeParameterElement(Element e) {
        return e.getKind() == TYPE_PARAMETER;
    }

    public boolean isTypeVariable(TypeMirror t) {
        return t.getKind() == TYPEVAR;
    }

    public boolean isVoid(TypeMirror t) {
        return t.getKind() == VOID;
    }

    public boolean isPublic(Element e) {
        return e.getModifiers().contains(Modifier.PUBLIC);
    }

    /**
     * Returns true if this class is linkable and false if we can't link to it.
     *
     * <p>
     * <b>NOTE:</b>  You can only link to external classes if they are public or
     * protected.
     *
     * @return true if this class is linkable and false if we can't link to the
     * desired class.
     */
    public boolean isLinkable(TypeElement typeElem) {
        return
            typeElem != null &&
                isIncluded(typeElem);
    }

    public List<? extends TypeMirror> getBounds(final TypeParameterElement tpe)
    {
        List<? extends TypeMirror> bounds = tpe.getBounds();
        if (!bounds.isEmpty()) {
            TypeMirror upperBound = bounds.get(bounds.size() - 1);
            if (ignoreBounds(upperBound)) {
                return List.of();
            }
        }
        return bounds;
    }

    public boolean ignoreBounds(TypeMirror bound) {
        return typeUtils.isSameType(bound, getObjectType()) && !isAnnotated(bound);
    }


    // our own little symbol table
    private final Map<String, TypeMirror> symtab = new HashMap<>();
    public TypeMirror getSymbol(String signature) {
        return symtab.computeIfAbsent(signature, s -> {
            var typeElement = elementUtils.getTypeElement(s);
            return typeElement == null ? null : typeElement.asType();
        });
    }

    public TypeMirror getObjectType() {
        return getSymbol("java.lang.Object");
    }

    public boolean isSameType(final TypeMirror bound, final TypeMirror objectType)
    {
        return typeUtils.isSameType(bound, objectType);
    }

    public TypeMirror getComponentType(TypeMirror t) {
        while (isArrayType(t)) {
            t = ((ArrayType) t).getComponentType();
        }
        return t;
    }

    /**
     * Returns true if {@code type} or any of its enclosing types has non-empty type arguments.
     * @param type the type
     * @return {@code true} if type arguments were found
     */
    public boolean isGenericType(TypeMirror type) {
        while (type instanceof DeclaredType dt) {
            if (!dt.getTypeArguments().isEmpty()) {
                return true;
            }
            type = dt.getEnclosingType();
        }
        return false;
    }

    public PackageElement getPackageOf(TypeMirror target)
    {
        var element = asElement(target);
        while(element.getEnclosingElement() != null && element.getEnclosingElement().getKind() != PACKAGE) {
            element = element.getEnclosingElement();
        }

        if (element.getEnclosingElement() == null)
            return null;

        return (PackageElement) element.getEnclosingElement();
    }

    public List<TypeMirror> getSuperTypeHierarchy(TypeMirror type)
    {
        List<TypeMirror> result = new ArrayList<>();
        TypeMirror sup;
        do {
            sup = getFirstVisibleSuperClass(type);
            if (sup != null)
                result.add(sup);
            type = sup;
        } while (sup != null);

        return result.reversed();
    }


    /**
     * Given a class, return the closest visible superclass.
     * @param type the TypeMirror to be interrogated
     * @return  the closest visible superclass.  Return null if it cannot
     *          be found.
     */
    public TypeMirror getFirstVisibleSuperClass(TypeMirror type) {
        // TODO: this computation should be eventually delegated to VisibleMemberTable
        Set<TypeElement> alreadySeen = null;
        // create a set iff assertions are enabled, to assert that no class
        // appears more than once in a superclass hierarchy
        assert (alreadySeen = new HashSet<>()) != null;
        for (var t = type; ;) {
            var supertypes = typeUtils.directSupertypes(t);
            if (supertypes.isEmpty()) { // end of hierarchy
                return null;
            }
            t = supertypes.get(0); // if non-empty, the first element is always the superclass
            var te = asTypeElement(t);
            assert alreadySeen.add(te); // it should be the first time we see `te`
            if (!hasHiddenTag(te) && (isPublic(te) || isLinkable(te))) {
                return t;
            }
        }
    }

    /**
     * Returns true if the element is included or selected, contains &#64;hidden tag,
     * or if javafx flag is present and element contains &#64;treatAsPrivate
     * tag.
     * @param e the queried element
     * @return true if it exists, false otherwise
     */
    public boolean hasHiddenTag(Element e) {
        // Non-included elements may still be visible via "transclusion" from undocumented enclosures,
        // but we don't want to run doclint on them, possibly causing warnings or errors.
        if (!isIncluded(e)) {
            return hasBlockTagUnchecked(e, HIDDEN);
        }
        if (hasBlockTag(e, DocTree.Kind.UNKNOWN_BLOCK_TAG, "treatAsPrivate")) {
            return true;
        }
        return hasBlockTag(e, DocTree.Kind.HIDDEN);
    }

    /*
     * Tests whether an element's doc comment contains a block tag without caching it or
     * running doclint on it. This is done by using getDocCommentInfo(Element) to retrieve
     * the doc comment info.
     */
    private boolean hasBlockTagUnchecked(Element element, DocTree.Kind kind) {
        DocCommentTree dcTree = docletEnvironment.getDocTrees().getDocCommentTree(element);
        if (dcTree != null) {
            for (DocTree dt : getBlockTags(dcTree)) {
                if (dt.getKind() == kind) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<? extends DocTree> getBlockTags(DocCommentTree dcTree) {
        return dcTree == null ? List.of() : dcTree.getBlockTags();
    }

    public List<? extends DocTree> getBlockTags(Element element) {
        return getBlockTags(docletEnvironment.getDocTrees().getDocCommentTree(element));
    }

    public <T extends DocTree> List<T> getBlockTags(Element element, Predicate<DocTree> filter, Class<T> tClass) {
        return getBlockTags(element).stream()
            .filter(t -> t.getKind() != ERRONEOUS)
            .filter(filter)
            .map(tClass::cast)
            .toList();
    }

    public boolean hasBlockTag(Element element, DocTree.Kind kind) {
        return hasBlockTag(element, kind, null);
    }

    public boolean hasBlockTag(Element element, DocTree.Kind kind, final String tagName) {
        if (hasDocCommentTree(element)) {
            for (DocTree dt : getBlockTags(docletEnvironment.getDocTrees().getDocCommentTree(element))) {
                if (dt.getKind() == kind && (tagName == null || getTagName(dt).equals(tagName))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether an element has an associated doc comment.
     * @param element the element
     * @return {@code true} if the element has a comment, and false otherwise
     */
    public boolean hasDocCommentTree(Element element) {
        return docletEnvironment.getDocTrees().getDocCommentTree(element) != null;
    }

    public String getTagName(DocTree dtree) {
        return switch (dtree.getKind()) {
            case AUTHOR, DEPRECATED, PARAM, PROVIDES, RETURN, SEE, SERIAL_DATA, SERIAL_FIELD,
                 THROWS, UNKNOWN_BLOCK_TAG, USES, VERSION ->
                ((BlockTagTree) dtree).getTagName();
            case UNKNOWN_INLINE_TAG ->
                ((InlineTagTree) dtree).getTagName();
            case ERRONEOUS ->
                "erroneous";
            default ->
                dtree.getKind().tagName;
        };
    }

    public List<ParamTree> getTypeParamTrees(Element element) {
        return getParamTrees(element, true);
    }

    public List<ParamTree> getParamTrees(Element element) {
        return getParamTrees(element, false);
    }

    private  List<ParamTree> getParamTrees(Element element, boolean isTypeParameters) {
        return getBlockTags(element,
            t -> t.getKind() == PARAM && ((ParamTree) t).isTypeParameter() == isTypeParameters,
            ParamTree.class);
    }

    public Map<String, Integer> mapNameToPosition(List<? extends Element> params) {
        Map<String, Integer> result = new HashMap<>();
        int position = 0;
        for (Element e : params) {
            String name = isTypeParameterElement(e)
                ? getTypeName(e.asType(), false)
                : getSimpleName(e);
            result.put(name, position);
            position++;
        }
        return result;
    }

    public List<? extends DocTree> getTags(DocTree docTree) {
        return new SimpleDocTreeVisitor<List<? extends DocTree>, Void>() {

            private List<DocTree> asList(String content) {
                return List.of(
                    docletEnvironment.getDocTrees().getDocTreeFactory().newTextTree(content)
                );
            }

            @Override
            public List<? extends DocTree> visitAuthor(AuthorTree node, Void p) {
                return node.getName();
            }

            @Override
            public List<? extends DocTree> visitComment(CommentTree node, Void p) {
                return asList(node.getBody());
            }

            @Override
            public List<? extends DocTree> visitDeprecated(DeprecatedTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitDocComment(DocCommentTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitEscape(EscapeTree node, Void p) {
                return asList(node.getBody());
            }

            @Override
            public List<? extends DocTree> visitLiteral(LiteralTree node, Void p) {
                return asList(node.getBody().getBody());
            }

            @Override
            public List<? extends DocTree> visitProvides(ProvidesTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSince(SinceTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitText(TextTree node, Void p) {
                return asList(node.getBody());
            }

            @Override
            public List<? extends DocTree> visitVersion(VersionTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitParam(ParamTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitReturn(ReturnTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSee(SeeTree node, Void p) {
                return node.getReference();
            }

            @Override
            public List<? extends DocTree> visitSerial(SerialTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSerialData(SerialDataTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSerialField(SerialFieldTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSpec(SpecTree node, Void p) {
                return node.getTitle();
            }

            @Override
            public List<? extends DocTree> visitThrows(ThrowsTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
                return node.getContent();
            }

            @Override
            public List<? extends DocTree> visitUses(UsesTree node, Void p) {
                return node.getDescription();
            }

            @Override
            protected List<? extends DocTree> defaultAction(DocTree node, Void p) {
                return List.of();
            }
        }.visit(docTree, null);
    }
}
