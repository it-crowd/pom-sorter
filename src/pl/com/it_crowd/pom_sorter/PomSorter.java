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
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class PomSorter implements ProjectComponent {
// ------------------------------ FIELDS ------------------------------

    private static final Map<String, Integer> TAG_PRIORITY = new HashMap<String, Integer>();

    private Comparator<XmlTag> artifactComparator = new ArtifactComparator();

    private Project project;

    private ProjectChildrenComparator projectChildrenComparator = new ProjectChildrenComparator();

    private TagNameComparator tagNameComparator = new TagNameComparator();

// -------------------------- STATIC METHODS --------------------------

    static {
        int i = 0;
        TAG_PRIORITY.put("modelVersion", i++);
        TAG_PRIORITY.put("parent", i++);
        TAG_PRIORITY.put("artifactId", i++);
        TAG_PRIORITY.put("groupId", i++);
        TAG_PRIORITY.put("versionId", i++);
        TAG_PRIORITY.put("packaging", i++);
        TAG_PRIORITY.put("name", i++);
        TAG_PRIORITY.put("description", i++);
        TAG_PRIORITY.put("url", i++);
        TAG_PRIORITY.put("inceptionYear", i++);
        TAG_PRIORITY.put("licenses", i++);
        TAG_PRIORITY.put("organization", i++);
        TAG_PRIORITY.put("properties", i++);
        TAG_PRIORITY.put("modules", i++);
        TAG_PRIORITY.put("dependencyManagement", i++);
        TAG_PRIORITY.put("dependencies", i++);
        TAG_PRIORITY.put("build", i++);
        TAG_PRIORITY.put("reporting", i++);
        TAG_PRIORITY.put("issueManagement", i++);
        TAG_PRIORITY.put("ciManagement", i++);
        TAG_PRIORITY.put("mailingLists", i++);
        TAG_PRIORITY.put("scm", i++);
        TAG_PRIORITY.put("prerequisites", i++);
        TAG_PRIORITY.put("repositories", i++);
        TAG_PRIORITY.put("pluginRepositories", i++);
        TAG_PRIORITY.put("distributionManagement", i++);
        TAG_PRIORITY.put("profiles", i++);
        TAG_PRIORITY.put("developers", i++);
        TAG_PRIORITY.put("contributors", i++);
        TAG_PRIORITY.put(null, i);
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
                    xmlFile.accept(new PomSortVisitor());
                    final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                    psiDocumentManager.commitDocument(document);
                    final XmlTag dependencyManagement = rootTag.findFirstSubTag("dependencyManagement");
                    if (dependencyManagement != null) {
                        sortDependencies(dependencyManagement.findFirstSubTag("dependencies"));
                    }
                    sortDependencies(rootTag.findFirstSubTag("dependencies"));
                    CodeStyleManager.getInstance(rootTag.getProject()).reformat(rootTag);
                    psiDocumentManager.commitDocument(document);
                }
            });
        }
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
        final XmlTag[] dependencies = tag.getSubTags();
        Collections.addAll(xmlTags, dependencies);
        final XmlTagValue xmlTagValue = tag.getValue();
        final XmlTagChild[] children = xmlTagValue.getChildren();
        if (children.length > 0) {
            tag.deleteChildRange(children[0], children[children.length - 1]);
        }
        for (XmlTag childTag : xmlTags) {
            tag.add(tag.createChildTag(childTag.getName(), null, childTag.getValue().getText(), false));
        }
    }

    private void sortDependencies(final XmlTag dependenciesTag)
    {
        sortChildren(dependenciesTag, artifactComparator);
    }

    private void sortPlugins(final XmlTag pluginsTag)
    {
        sortChildren(pluginsTag, artifactComparator);
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
            } else if ("properties".equals(tagName)) {
                sortByChildTagName(tag);
            }
        }
    }

    private class ProjectChildrenComparator implements Comparator<XmlTag> {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Comparator ---------------------

        @Override
        public int compare(XmlTag a, XmlTag b)
        {
            Integer priorityA = TAG_PRIORITY.get(a.getName());
            if (priorityA == null) {
                priorityA = TAG_PRIORITY.get(null);
            }
            Integer priorityB = TAG_PRIORITY.get(b.getName());
            if (priorityB == null) {
                priorityB = TAG_PRIORITY.get(null);
            }
            return priorityA.compareTo(priorityB);
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
