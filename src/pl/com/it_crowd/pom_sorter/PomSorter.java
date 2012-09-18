package pl.com.it_crowd.pom_sorter;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlComment;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.util.XmlUtil;
import org.apache.commons.collections.comparators.NullComparator;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@com.intellij.openapi.components.State(
    name = PomSorter.COMPONENT_NAME,
    storages = {@Storage(
        file = "$PROJECT_FILE$")})
public class PomSorter implements ProjectComponent, PersistentStateComponent<PomSorter.State> {
// ------------------------------ FIELDS ------------------------------

    public static final String COMPONENT_NAME = "PomSorter";

    private static final Key<Collection<XmlComment>> COMMENT_KEY = Key.create("comment");

    private static final List<String> DEFAULT_BUILD_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_DEPENDENCY_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_EXECUTION_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PLUGIN_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PROFILE_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PROJECT_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final Key<Collection<XmlComment>> INTERNAL_COMMENT_KEY = Key.create("internalComment");

    private Comparator<XmlTag> artifactComparator = new ArtifactComparator();

    private SortMode defaultSortMode = SortMode.ALPHABETIC;

    private FixedOrderComparator fixedOrderComparator = new FixedOrderComparator();

    private final Map<String, TagSortingSetting> order = new HashMap<String, TagSortingSetting>();

    private Project project;

    private TagNameComparator tagNameComparator = new TagNameComparator();

// -------------------------- STATIC METHODS --------------------------

    static {
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("modelVersion");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("parent");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("version");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("packaging");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("name");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("description");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("url");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("inceptionYear");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("licenses");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("organization");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("properties");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("modules");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("dependencyManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("dependencies");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("build");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("reporting");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("issueManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("ciManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("mailingLists");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("scm");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("prerequisites");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("repositories");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("pluginRepositories");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("distributionManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("profiles");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("developers");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("contributors");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("version");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("scope");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.addAll(DEFAULT_DEPENDENCY_CHILDREN_PRIORITY);

        DEFAULT_BUILD_CHILDREN_PRIORITY.add("finalName");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("resources");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("testResources");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("filters");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("pluginManagement");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("plugins");

        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("id");

        DEFAULT_EXECUTION_CHILDREN_PRIORITY.add("id");
        DEFAULT_EXECUTION_CHILDREN_PRIORITY.add("phase");
    }

    @NotNull
    private static XmlComment createComment(Project project, String commentText) throws IncorrectOperationException
    {
        final XmlTag element = XmlElementFactory.getInstance(project).createTagFromText("<foo><!--" + commentText + "--></foo>", XMLLanguage.INSTANCE);
        final XmlComment newComment = PsiTreeUtil.getChildOfType(element, XmlComment.class);
        assert newComment != null;
        return newComment;
    }

    @NotNull
    private static XmlText createText(Project project, String text) throws IncorrectOperationException
    {
        final XmlTag element = XmlElementFactory.getInstance(project).createTagFromText("<foo>" + text + "</foo>", XMLLanguage.INSTANCE);
        final XmlText newText = PsiTreeUtil.getChildOfType(element, XmlText.class);
        assert newText != null;
        return newText;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public PomSorter(Project project)
    {
        this.project = project;
        if (order.isEmpty()) {
            order.put("project", new TagSortingSetting("project", SortMode.FIXED, DEFAULT_PROJECT_CHILDREN_PRIORITY));
            order.put("dependency", new TagSortingSetting("dependency", SortMode.FIXED, DEFAULT_DEPENDENCY_CHILDREN_PRIORITY));
            order.put("build", new TagSortingSetting("build", SortMode.FIXED, DEFAULT_BUILD_CHILDREN_PRIORITY));
            order.put("profile", new TagSortingSetting("profile", SortMode.FIXED, DEFAULT_PROFILE_CHILDREN_PRIORITY));
            order.put("execution", new TagSortingSetting("execution", SortMode.FIXED, DEFAULT_EXECUTION_CHILDREN_PRIORITY));
            order.put("plugin", new TagSortingSetting("plugin", SortMode.FIXED, DEFAULT_PLUGIN_CHILDREN_PRIORITY));
        }
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public SortMode getDefaultSortMode()
    {
        return defaultSortMode;
    }

    public void setDefaultSortMode(SortMode defaultSortMode)
    {
        this.defaultSortMode = defaultSortMode;
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
        return COMPONENT_NAME;
    }

// --------------------- Interface PersistentStateComponent ---------------------

    @Override
    public State getState()
    {
        return new State(order.values(), getDefaultSortMode());
    }

    @Override
    public void loadState(State state)
    {
        order.clear();
        for (TagSortingSetting setting : state.order) {
            order.put(setting.getName(), setting);
        }
    }

// --------------------- Interface ProjectComponent ---------------------

    public void projectOpened()
    {
    }

    public void projectClosed()
    {
    }

// -------------------------- OTHER METHODS --------------------------

    public Map<String, TagSortingSetting> getTagSortingSettings()
    {
        return order;
    }

    public void sortFile(final XmlFile xmlFile)
    {
        final XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag != null) {
            new WriteCommandAction(project, "Sort POM", xmlFile) {
                @Override
                protected void run(Result result) throws Throwable
                {
                    xmlFile.accept(new PomSortVisitor());
                    CodeStyleManager.getInstance(rootTag.getProject()).reformat(rootTag);
                }
            }.execute();
        }
    }

    private PsiElement appendCommentsIfPresent(XmlTag tag, PsiElement previousPsiElement, Collection<XmlComment> comments)
    {
        if (comments != null) {
            for (XmlComment comment : comments) {
                if (!(previousPsiElement instanceof XmlText && previousPsiElement.getText().contains("\n"))) {
                    previousPsiElement = tag.addAfter(createText(project, "\n"), previousPsiElement);
                }
                previousPsiElement = tag.addAfter(createComment(project, XmlUtil.getCommentText(comment)), previousPsiElement);
                previousPsiElement = tag.addAfter(createText(project, "\n"), previousPsiElement);
            }
        }
        return previousPsiElement;
    }

    private void assignComment(XmlTag tagToAssignComment, XmlComment comment, Key<Collection<XmlComment>> commentKey)
    {
        if (tagToAssignComment == null) {
            return;
        }
        Collection<XmlComment> comments = tagToAssignComment.getUserData(commentKey);
        if (comments == null) {
            comments = new ArrayList<XmlComment>();
            tagToAssignComment.putUserData(commentKey, comments);
        }
        comments.add(comment);
    }

    private void sortChildren(XmlTag tag, Comparator<XmlTag> comparator)
    {
        if (tag == null) {
            return;
        }
        final List<XmlTag> xmlTags = new ArrayList<XmlTag>();
        final XmlTag[] subTags = tag.getSubTags();
        Collections.addAll(xmlTags, subTags);
        Collections.sort(xmlTags, comparator);
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
        PsiElement previousPsiElement = null;
        for (XmlTag childTag : xmlTags) {
            previousPsiElement = appendCommentsIfPresent(tag, previousPsiElement, childTag.getUserData(COMMENT_KEY));
            final XmlTag xmlTag = tag.createChildTag(childTag.getName(), null, childTag.getValue().getText(), false);
            previousPsiElement = tag.addAfter(xmlTag, previousPsiElement);
        }
        appendCommentsIfPresent(tag, previousPsiElement, tag.getUserData(INTERNAL_COMMENT_KEY));
    }

// -------------------------- ENUMERATIONS --------------------------

    public static enum SortMode {
        NONE,
        ALPHABETIC,
        ARTIFACT,
        FIXED
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

        private FixedOrderComparator()
        {
            this.priority = new HashMap<String, Integer>();
        }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Comparator ---------------------

        @Override
        public int compare(XmlTag a, XmlTag b)
        {
            Integer priorityA = priority.get(a.getName());
            if (priorityA == null) {
                priorityA = priority.size();
            }
            Integer priorityB = priority.get(b.getName());
            if (priorityB == null) {
                priorityB = priority.size();
            }
            if (priorityA.equals(priorityB)) {
                return nullComparator.compare(a.getName(), b.getName());
            } else {
                return priorityA.compareTo(priorityB);
            }
        }

// -------------------------- OTHER METHODS --------------------------

        public void setOrder(List<String> order)
        {
            priority.clear();
            for (int i = 0, orderSize = order.size(); i < orderSize; i++) {
                String value = order.get(i);
                priority.put(value, i);
            }
        }
    }

    private class PomSortVisitor extends XmlRecursiveElementVisitor {
// -------------------------- OTHER METHODS --------------------------

        @Override
        public void visitXmlComment(XmlComment comment)
        {
            super.visitXmlComment(comment);
            XmlTag tagToAssignComment = null;
            PsiElement sibling = comment;
            do {
                sibling = sibling.getNextSibling();
                if (sibling instanceof XmlTag) {
                    tagToAssignComment = (XmlTag) sibling;
                    break;
                }
            } while (sibling != null);
            if (tagToAssignComment == null) {
                assignComment(comment.getParentTag(), comment, INTERNAL_COMMENT_KEY);
            } else {
                assignComment(tagToAssignComment, comment, COMMENT_KEY);
            }
        }

        @Override
        public void visitXmlTag(XmlTag tag)
        {
            super.visitXmlTag(tag);
            final String tagName = tag.getName();
            TagSortingSetting tagSortingSetting = order.get(tagName);
            SortMode mode = tagSortingSetting == null ? getDefaultSortMode() : tagSortingSetting.getMode();
            if (SortMode.ALPHABETIC.equals(mode)) {
                sortChildren(tag, tagNameComparator);
            } else if (SortMode.FIXED.equals(mode)) {
                fixedOrderComparator.setOrder(tagSortingSetting == null ? Collections.<String>emptyList() : tagSortingSetting.getOrder());
                sortChildren(tag, fixedOrderComparator);
            } else if (SortMode.ARTIFACT.equals(mode)) {
                sortChildren(tag, artifactComparator);
            }
        }
    }

    public static class State {
// ------------------------------ FIELDS ------------------------------

        public SortMode defaultSortMode;

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        public List<TagSortingSetting> order = new ArrayList<TagSortingSetting>();

// --------------------------- CONSTRUCTORS ---------------------------

        public State()
        {
        }

        public State(Collection<TagSortingSetting> order, SortMode defaultSortMode)
        {
            this();
            this.order.clear();
            this.order.addAll(order);
            this.defaultSortMode = defaultSortMode;
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

    public static class TagSortingSetting implements Serializable {
// ------------------------------ FIELDS ------------------------------

        private SortMode mode;

        private String name;

        private List<String> order;

// --------------------------- CONSTRUCTORS ---------------------------

        public TagSortingSetting()
        {
        }

        public TagSortingSetting(String name, SortMode mode, Collection<String> order)
        {
            this.name = name;
            this.mode = mode;
            this.order = new ArrayList<String>(order);
        }

// --------------------- GETTER / SETTER METHODS ---------------------

        public SortMode getMode()
        {
            if (mode == null) {
                mode = SortMode.ALPHABETIC;
            }
            return mode;
        }

        public void setMode(SortMode mode)
        {
            this.mode = mode;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public List<String> getOrder()
        {
            if (order == null) {
                order = new ArrayList<String>();
            }
            return order;
        }

        public void setOrder(List<String> order)
        {
            this.order = order;
        }
    }
}
