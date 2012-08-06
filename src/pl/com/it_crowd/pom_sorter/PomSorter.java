package pl.com.it_crowd.pom_sorter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.psi.xml.XmlText;
import org.apache.commons.collections.comparators.NullComparator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class PomSorter implements ProjectComponent {
// ------------------------------ FIELDS ------------------------------

    private static final Map<String, Integer> BUILD_CHILDREN_PRIORITY = new HashMap<String, Integer>();

    private static final Map<String, Integer> DEPENDENCY_CHILDREN_PRIORITY = new HashMap<String, Integer>();

    private static final Map<String, Integer> EXECUTION_CHILDREN_PRIORITY = new HashMap<String, Integer>();

    private static final Map<String, Integer> PLUGIN_CHILDREN_PRIORITY = new HashMap<String, Integer>();

    private static final Map<String, Integer> PROFILE_CHILDREN_PRIORITY = new HashMap<String, Integer>();

    private static final Map<String, Integer> PROJECT_CHILDREN_PRIORITY = new HashMap<String, Integer>();

    private Comparator<XmlTag> artifactComparator = new ArtifactComparator();

    private FixedOrderComparator buildChildrenComparator = new FixedOrderComparator(BUILD_CHILDREN_PRIORITY);

    private FixedOrderComparator dependencyChildrenComparator = new FixedOrderComparator(DEPENDENCY_CHILDREN_PRIORITY);

    private FixedOrderComparator executionChildrenComparator = new FixedOrderComparator(EXECUTION_CHILDREN_PRIORITY);

    private FixedOrderComparator pluginChildrenComparator = new FixedOrderComparator(PLUGIN_CHILDREN_PRIORITY);

    private FixedOrderComparator profileChildrenComparator = new FixedOrderComparator(PROFILE_CHILDREN_PRIORITY);

    private Project project;

    private FixedOrderComparator projectChildrenComparator = new FixedOrderComparator(PROJECT_CHILDREN_PRIORITY);

    private TagNameComparator tagNameComparator = new TagNameComparator();

// -------------------------- STATIC METHODS --------------------------

    static {
        int i = 0;
        PROJECT_CHILDREN_PRIORITY.put("modelVersion", i++);
        PROJECT_CHILDREN_PRIORITY.put("parent", i++);
        PROJECT_CHILDREN_PRIORITY.put("groupId", i++);
        PROJECT_CHILDREN_PRIORITY.put("artifactId", i++);
        PROJECT_CHILDREN_PRIORITY.put("version", i++);
        PROJECT_CHILDREN_PRIORITY.put("packaging", i++);
        PROJECT_CHILDREN_PRIORITY.put("name", i++);
        PROJECT_CHILDREN_PRIORITY.put("description", i++);
        PROJECT_CHILDREN_PRIORITY.put("url", i++);
        PROJECT_CHILDREN_PRIORITY.put("inceptionYear", i++);
        PROJECT_CHILDREN_PRIORITY.put("licenses", i++);
        PROJECT_CHILDREN_PRIORITY.put("organization", i++);
        PROJECT_CHILDREN_PRIORITY.put("properties", i++);
        PROJECT_CHILDREN_PRIORITY.put("modules", i++);
        PROJECT_CHILDREN_PRIORITY.put("dependencyManagement", i++);
        PROJECT_CHILDREN_PRIORITY.put("dependencies", i++);
        PROJECT_CHILDREN_PRIORITY.put("build", i++);
        PROJECT_CHILDREN_PRIORITY.put("reporting", i++);
        PROJECT_CHILDREN_PRIORITY.put("issueManagement", i++);
        PROJECT_CHILDREN_PRIORITY.put("ciManagement", i++);
        PROJECT_CHILDREN_PRIORITY.put("mailingLists", i++);
        PROJECT_CHILDREN_PRIORITY.put("scm", i++);
        PROJECT_CHILDREN_PRIORITY.put("prerequisites", i++);
        PROJECT_CHILDREN_PRIORITY.put("repositories", i++);
        PROJECT_CHILDREN_PRIORITY.put("pluginRepositories", i++);
        PROJECT_CHILDREN_PRIORITY.put("distributionManagement", i++);
        PROJECT_CHILDREN_PRIORITY.put("profiles", i++);
        PROJECT_CHILDREN_PRIORITY.put("developers", i++);
        PROJECT_CHILDREN_PRIORITY.put("contributors", i++);
        PROJECT_CHILDREN_PRIORITY.put(null, i);
        i = 0;
        DEPENDENCY_CHILDREN_PRIORITY.put("groupId", i++);
        DEPENDENCY_CHILDREN_PRIORITY.put("artifactId", i++);
        DEPENDENCY_CHILDREN_PRIORITY.put("version", i++);
        DEPENDENCY_CHILDREN_PRIORITY.put("scope", i++);
        DEPENDENCY_CHILDREN_PRIORITY.put(null, i);
        PLUGIN_CHILDREN_PRIORITY.putAll(DEPENDENCY_CHILDREN_PRIORITY);
        i = 0;
        BUILD_CHILDREN_PRIORITY.put("finalName", i++);
        BUILD_CHILDREN_PRIORITY.put("resources", i++);
        BUILD_CHILDREN_PRIORITY.put("testResources", i++);
        BUILD_CHILDREN_PRIORITY.put("filters", i++);
        BUILD_CHILDREN_PRIORITY.put("pluginManagement", i++);
        BUILD_CHILDREN_PRIORITY.put("plugins", i++);
        BUILD_CHILDREN_PRIORITY.put(null, i);
        i = 0;
        PROFILE_CHILDREN_PRIORITY.put("id", i++);
        PROFILE_CHILDREN_PRIORITY.put(null, i);
        i = 0;
        EXECUTION_CHILDREN_PRIORITY.put("id", i++);
        EXECUTION_CHILDREN_PRIORITY.put("phase", i++);
        EXECUTION_CHILDREN_PRIORITY.put(null, i);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public PomSorter(Project project)
    {
        this.project = project;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BaseComponent ---------------------

    public void initComponent()
    {
    }

    public void disposeComponent()
    {
    }

// --------------------- Interface NamedComponent ---------------------

    @NotNull
    public String getComponentName()
    {
        return "PomDependencySorter";
    }

// --------------------- Interface ProjectComponent ---------------------

    public void projectOpened()
    {
    }

    public void projectClosed()
    {
    }

// -------------------------- OTHER METHODS --------------------------

    public void sortFile(final XmlFile xmlFile, final Document document)
    {
        final XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag != null) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run()
                {
                    final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                    psiDocumentManager.commitDocument(document);
                    xmlFile.accept(new PomSortVisitor());
                    CodeStyleManager.getInstance(rootTag.getProject()).reformat(rootTag);
                    psiDocumentManager.commitDocument(document);
                }
            });
        }
    }

    private void sortBuild(XmlTag tag)
    {
        sortChildren(tag, buildChildrenComparator);
    }

    private void sortByChildTagName(XmlTag tag)
    {
        sortChildren(tag, tagNameComparator);
    }

    private void sortChildren(XmlTag tag, Comparator<XmlTag> comparator)
    {
        if (tag == null) {
            return;
        }
        final TreeSet<XmlTag> xmlTags = new TreeSet<XmlTag>(comparator);
        final XmlTag[] subTags = tag.getSubTags();
        Collections.addAll(xmlTags, subTags);
        final XmlTagValue xmlTagValue = tag.getValue();
        final XmlTagChild[] children = xmlTagValue.getChildren();
        if (children.length > 0) {
            tag.deleteChildRange(children[0], children[children.length - 1]);
        }
        final StringBuilder stringBuilder = new StringBuilder();
        for (XmlText xmlText : xmlTagValue.getTextElements()) {
            stringBuilder.append(xmlText.getText().trim());
        }
        tag.getValue().setText(stringBuilder.toString());
        for (XmlTag childTag : xmlTags) {
            tag.add(tag.createChildTag(childTag.getName(), null, childTag.getValue().getText(), false));
        }
    }

    private void sortDependencies(XmlTag dependenciesTag)
    {
        sortChildren(dependenciesTag, artifactComparator);
    }

    private void sortDependency(XmlTag tag)
    {
        sortChildren(tag, dependencyChildrenComparator);
    }

    private void sortExecution(XmlTag tag)

    {
        sortChildren(tag, executionChildrenComparator);
    }

    private void sortPlugin(XmlTag tag)
    {
        sortChildren(tag, pluginChildrenComparator);
    }

    private void sortPlugins(XmlTag pluginsTag)
    {
        sortChildren(pluginsTag, artifactComparator);
    }

    private void sortProfile(XmlTag tag)
    {
        sortChildren(tag, profileChildrenComparator);
    }

    private void sortProject(XmlTag tag)
    {
        sortChildren(tag, projectChildrenComparator);
    }

// -------------------------- INNER CLASSES --------------------------

    public static class ArtifactComparator implements Comparator<XmlTag> {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Comparator ---------------------

        public int compare(XmlTag a, XmlTag b)
        {
            return toString(a).compareTo(toString(b));
        }

        private String toString(XmlTag dependency)
        {
            String prefix = dependency.getSubTagText("scope");
            return ("test".equalsIgnoreCase(prefix) ? "b:" : "a:") + dependency.getSubTagText("groupId") + ":" + dependency.getSubTagText("artifactId");
        }
    }

    private class FixedOrderComparator implements Comparator<XmlTag> {
// ------------------------------ FIELDS ------------------------------

        private final NullComparator nullComparator = new NullComparator();

        private final Map<String, Integer> priority;

// --------------------------- CONSTRUCTORS ---------------------------

        private FixedOrderComparator(Map<String, Integer> priority)
        {
            this.priority = priority;
        }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Comparator ---------------------

        @Override
        public int compare(XmlTag a, XmlTag b)
        {
            Integer priorityA = priority.get(a.getName());
            if (priorityA == null) {
                priorityA = priority.get(null);
            }
            Integer priorityB = priority.get(b.getName());
            if (priorityB == null) {
                priorityB = priority.get(null);
            }
            if (priorityA.equals(priorityB)) {
                return nullComparator.compare(a.getName(), b.getName());
            } else {
                return priorityA.compareTo(priorityB);
            }
        }
    }

    private class PomSortVisitor extends XmlRecursiveElementVisitor {
// -------------------------- OTHER METHODS --------------------------

        @Override
        public void visitXmlTag(XmlTag tag)
        {
            super.visitXmlTag(tag);
            final String tagName = tag.getName();
            if ("project".equals(tagName)) {
                sortProject(tag);
            } else if ("dependencies".equals(tagName)) {
                sortDependencies(tag);
            } else if ("plugins".equals(tagName)) {
                sortPlugins(tag);
            } else if ("plugin".equals(tagName)) {
                sortPlugin(tag);
            } else if ("dependency".equals(tagName)) {
                sortDependency(tag);
            } else if ("build".equals(tagName)) {
                sortBuild(tag);
            } else if ("profile".equals(tagName)) {
                sortProfile(tag);
            } else if ("execution".equals(tagName)) {
                sortExecution(tag);
            } else {
                sortByChildTagName(tag);
            }
        }
    }

    private class TagNameComparator implements Comparator<XmlTag> {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Comparator ---------------------

        @Override
        public int compare(XmlTag a, XmlTag b)
        {
            return a.getName().compareTo(b.getName());
        }
    }
}
