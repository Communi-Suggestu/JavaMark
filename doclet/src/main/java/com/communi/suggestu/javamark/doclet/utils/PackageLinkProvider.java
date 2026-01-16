package com.communi.suggestu.javamark.doclet.utils;

import javax.lang.model.element.PackageElement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Provides relative path links between packages, only for packages known to a given TypeUniverse.
 */
public class PackageLinkProvider {
    private final Set<PackageElement> knownPackages;

    public PackageLinkProvider(Set<PackageElement> knownPackages) {
        this.knownPackages = new HashSet<>(knownPackages);
    }

    /**
     * Generates a relative path from sourcePackage to targetPackage, or null if either is not in the known set.
     * The path is based on package hierarchy and ends with package-summary.md.
     */
    public String getRelativeLink(PackageElement sourcePackage, PackageElement targetPackage) {
        if (!isKnown(sourcePackage) || !isKnown(targetPackage)) {
            return null;
        }
        if (sourcePackage.getQualifiedName().contentEquals(targetPackage.getQualifiedName())) {
            return "index.md";
        }
        List<String> sourceHierarchy = getPackageHierarchy(sourcePackage);
        List<String> targetHierarchy = getPackageHierarchy(targetPackage);
        int common = 0;
        while (common < sourceHierarchy.size() && common < targetHierarchy.size() && Objects.equals(sourceHierarchy.get(common), targetHierarchy.get(common))) {
            common++;
        }
        StringBuilder rel = new StringBuilder();
        for (int i = common; i < sourceHierarchy.size(); i++) {
            rel.append("../");
        }
        for (int i = common; i < targetHierarchy.size(); i++) {
            rel.append(targetHierarchy.get(i)).append("/");
        }
        rel.append("index.md");
        return rel.toString();
    }

    private boolean isKnown(PackageElement pkg) {
        for (PackageElement known : knownPackages) {
            if (known.getQualifiedName().contentEquals(pkg.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    private List<String> getPackageHierarchy(PackageElement pkg) {
        String[] parts = pkg.getQualifiedName().toString().split("\\.");
        return java.util.Arrays.asList(parts);
    }
}
