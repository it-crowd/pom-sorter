package pl.itcrowd.pom_sorter;

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
import com.intellij.psi.xml.XmlAttribute;
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
import java.util.regex.PatternSyntaxException;

@com.intellij.openapi.components.State(
    name = PomSorter.COMPONENT_NAME,
    storages = {@Storage(
        file = "$PROJECT_FILE$")})
public class PomSorter implements ProjectComponent, PersistentStateComponent<PomSorter.State> {

    public static final String COMPONENT_NAME = "PomSorter";

    private static final Key<Collection<XmlComment>> COMMENT_KEY = Key.create("comment");

    private static final List<String> DEFAULT_ACTIVATION_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_ACTIVATION_FILE_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_ACTIVATION_OS_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_ACTIVATION_PROPERTY_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_BUILD_BASE_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_BUILD_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_CI_MANAGEMENT_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_DEPENDENCY_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_DEVELOPER_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_EXCLUSION_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_EXTENSION_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_ISSUE_MANAGEMENT_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_LICENSE_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_MAILING_LIST_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_NOTIFIER_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_ORGANIZATION_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PARENT_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PLUGIN_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PLUGIN_EXECUTION_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PLUGIN_REPOSITORY_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PROFILE_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_PROJECT_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_RELOCATION_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_REPORTING_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_REPORT_SETS_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_REPOSITORY_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_REPOSITORY_POLICY_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_RESOURCE_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_SCM_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final List<String> DEFAULT_SITE_CHILDREN_PRIORITY = new ArrayList<String>();

    private static final Key<Collection<XmlComment>> INTERNAL_COMMENT_KEY = Key.create("internalComment");

    private final Map<String, TagSortingSetting> order = new HashMap<String, TagSortingSetting>();

    private Comparator<XmlTag> artifactComparator = new ArtifactComparator();

    private Map<String, AttributeComparator> attributeComparators = new HashMap<String, AttributeComparator>();

    private SortMode defaultSortMode = SortMode.ALPHABETIC;

    private FixedOrderComparator fixedOrderComparator = new FixedOrderComparator();

    private Project project;

    private Map<String, SubtagComparator> subtagComparators = new HashMap<String, SubtagComparator>();

    private TagNameComparator tagNameComparator = new TagNameComparator();

    static {
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("parent");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("modelVersion");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("packaging");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("name");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("version");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("description");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("url");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("prerequisites");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("issueManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("ciManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("inceptionYear");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("mailingLists");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("mailingList");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("developers");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("developer");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("contributors");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("contributor");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("licenses");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("license");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("scm");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("organization");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("build");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("profiles");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("profile");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("modules");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("module");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("repositories");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("repository");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("pluginRepositories");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("pluginRepository");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("dependencies");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("dependency");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("reports");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("reporting");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("dependencyManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("distributionManagement");
        DEFAULT_PROJECT_CHILDREN_PRIORITY.add("properties");

        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("name");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("email");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("url");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("organization");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("organizationUrl");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("roles");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("role");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("timezone");
        DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY.add("properties");

        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("id");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("activation");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("build");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("modules");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("module");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("repositories");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("repository");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("pluginRepositories");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("pluginRepository");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("dependencies");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("dependency");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("reports");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("reporting");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("dependencyManagement");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("distributionManagement");
        DEFAULT_PROFILE_CHILDREN_PRIORITY.add("properties");

        DEFAULT_ACTIVATION_CHILDREN_PRIORITY.add("activeByDefault");
        DEFAULT_ACTIVATION_CHILDREN_PRIORITY.add("jdk");
        DEFAULT_ACTIVATION_CHILDREN_PRIORITY.add("os");
        DEFAULT_ACTIVATION_CHILDREN_PRIORITY.add("property");
        DEFAULT_ACTIVATION_CHILDREN_PRIORITY.add("file");

        DEFAULT_ACTIVATION_FILE_CHILDREN_PRIORITY.add("missing");
        DEFAULT_ACTIVATION_FILE_CHILDREN_PRIORITY.add("exists");

        DEFAULT_ACTIVATION_PROPERTY_CHILDREN_PRIORITY.add("name");
        DEFAULT_ACTIVATION_PROPERTY_CHILDREN_PRIORITY.add("value");

        DEFAULT_ACTIVATION_OS_CHILDREN_PRIORITY.add("name");
        DEFAULT_ACTIVATION_OS_CHILDREN_PRIORITY.add("family");
        DEFAULT_ACTIVATION_OS_CHILDREN_PRIORITY.add("arch");
        DEFAULT_ACTIVATION_OS_CHILDREN_PRIORITY.add("version");

        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("version");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("type");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("classifier");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("scope");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("systemPath");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("exclusions");
        DEFAULT_DEPENDENCY_CHILDREN_PRIORITY.add("optional");

        DEFAULT_EXCLUSION_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_EXCLUSION_CHILDREN_PRIORITY.add("groupId");

        DEFAULT_REPORTING_CHILDREN_PRIORITY.add("excludeDefaults");
        DEFAULT_REPORTING_CHILDREN_PRIORITY.add("outputDirectory");
        DEFAULT_REPORTING_CHILDREN_PRIORITY.add("plugins");

        DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY.add("version");
        DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY.add("inherited");
        DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY.add("configuration");
        DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY.add("reportSets");

        DEFAULT_REPORT_SETS_CHILDREN_PRIORITY.add("id");
        DEFAULT_REPORT_SETS_CHILDREN_PRIORITY.add("configuration");
        DEFAULT_REPORT_SETS_CHILDREN_PRIORITY.add("inherited");
        DEFAULT_REPORT_SETS_CHILDREN_PRIORITY.add("reports");

        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("defaultGoal");
        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("resources");
        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("testResources");
        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("directory");
        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("finalName");
        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("filters");
        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("pluginManagement");
        DEFAULT_BUILD_BASE_CHILDREN_PRIORITY.add("plugins");

        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("version");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("extensions");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("executions");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("dependencies");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("goals");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("inherited");
        DEFAULT_PLUGIN_CHILDREN_PRIORITY.add("configuration");

        DEFAULT_PLUGIN_EXECUTION_CHILDREN_PRIORITY.add("id");
        DEFAULT_PLUGIN_EXECUTION_CHILDREN_PRIORITY.add("phase");
        DEFAULT_PLUGIN_EXECUTION_CHILDREN_PRIORITY.add("goals");
        DEFAULT_PLUGIN_EXECUTION_CHILDREN_PRIORITY.add("inherited");
        DEFAULT_PLUGIN_EXECUTION_CHILDREN_PRIORITY.add("configuration");

        DEFAULT_RESOURCE_CHILDREN_PRIORITY.add("targetPath");
        DEFAULT_RESOURCE_CHILDREN_PRIORITY.add("filtering");
        DEFAULT_RESOURCE_CHILDREN_PRIORITY.add("directory");
        DEFAULT_RESOURCE_CHILDREN_PRIORITY.add("includes");
        DEFAULT_RESOURCE_CHILDREN_PRIORITY.add("excludes");

        DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY.add("repository");
        DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY.add("snapshotRepository");
        DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY.add("site");
        DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY.add("downloadUrl");
        DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY.add("relocation");
        DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY.add("status");

        DEFAULT_SITE_CHILDREN_PRIORITY.add("id");
        DEFAULT_SITE_CHILDREN_PRIORITY.add("name");
        DEFAULT_SITE_CHILDREN_PRIORITY.add("url");

        DEFAULT_RELOCATION_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_RELOCATION_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_RELOCATION_CHILDREN_PRIORITY.add("version");
        DEFAULT_RELOCATION_CHILDREN_PRIORITY.add("message");

        DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY.add("uniqueVersion");
        DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY.add("id");
        DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY.add("name");
        DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY.add("url");
        DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY.add("layout");

        DEFAULT_REPOSITORY_CHILDREN_PRIORITY.add("releases");
        DEFAULT_REPOSITORY_CHILDREN_PRIORITY.add("snapshots");
        DEFAULT_REPOSITORY_CHILDREN_PRIORITY.add("id");
        DEFAULT_REPOSITORY_CHILDREN_PRIORITY.add("name");
        DEFAULT_REPOSITORY_CHILDREN_PRIORITY.add("url");
        DEFAULT_REPOSITORY_CHILDREN_PRIORITY.add("layout");

        DEFAULT_REPOSITORY_POLICY_CHILDREN_PRIORITY.add("enabled");
        DEFAULT_REPOSITORY_POLICY_CHILDREN_PRIORITY.add("updatePolicy");
        DEFAULT_REPOSITORY_POLICY_CHILDREN_PRIORITY.add("checksumPolicy");

        DEFAULT_MAILING_LIST_CHILDREN_PRIORITY.add("name");
        DEFAULT_MAILING_LIST_CHILDREN_PRIORITY.add("subscribe");
        DEFAULT_MAILING_LIST_CHILDREN_PRIORITY.add("unsubscribe");
        DEFAULT_MAILING_LIST_CHILDREN_PRIORITY.add("post");
        DEFAULT_MAILING_LIST_CHILDREN_PRIORITY.add("archive");
        DEFAULT_MAILING_LIST_CHILDREN_PRIORITY.add("otherArchives");

        DEFAULT_PLUGIN_REPOSITORY_CHILDREN_PRIORITY.addAll(DEFAULT_REPOSITORY_CHILDREN_PRIORITY);

        DEFAULT_BUILD_CHILDREN_PRIORITY.add("sourceDirectory");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("scriptSourceDirectory");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("testSourceDirectory");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("outputDirectory");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("testOutputDirectory");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("extensions");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("defaultGoal");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("resources");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("testResources");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("directory");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("finalName");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("filters");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("pluginManagement");
        DEFAULT_BUILD_CHILDREN_PRIORITY.add("plugins");

        DEFAULT_EXTENSION_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_EXTENSION_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_EXTENSION_CHILDREN_PRIORITY.add("version");

        DEFAULT_ISSUE_MANAGEMENT_CHILDREN_PRIORITY.add("system");
        DEFAULT_ISSUE_MANAGEMENT_CHILDREN_PRIORITY.add("url");

        DEFAULT_PARENT_CHILDREN_PRIORITY.add("artifactId");
        DEFAULT_PARENT_CHILDREN_PRIORITY.add("groupId");
        DEFAULT_PARENT_CHILDREN_PRIORITY.add("version");
        DEFAULT_PARENT_CHILDREN_PRIORITY.add("relativePath");

        DEFAULT_CI_MANAGEMENT_CHILDREN_PRIORITY.add("system");
        DEFAULT_CI_MANAGEMENT_CHILDREN_PRIORITY.add("url");
        DEFAULT_CI_MANAGEMENT_CHILDREN_PRIORITY.add("notifiers");

        DEFAULT_NOTIFIER_CHILDREN_PRIORITY.add("type");
        DEFAULT_NOTIFIER_CHILDREN_PRIORITY.add("sendOnError");
        DEFAULT_NOTIFIER_CHILDREN_PRIORITY.add("sendOnFailure");
        DEFAULT_NOTIFIER_CHILDREN_PRIORITY.add("sendOnSuccess");
        DEFAULT_NOTIFIER_CHILDREN_PRIORITY.add("sendOnWarning");
        DEFAULT_NOTIFIER_CHILDREN_PRIORITY.add("address");
        DEFAULT_NOTIFIER_CHILDREN_PRIORITY.add("configuration");

        DEFAULT_LICENSE_CHILDREN_PRIORITY.add("name");
        DEFAULT_LICENSE_CHILDREN_PRIORITY.add("url");
        DEFAULT_LICENSE_CHILDREN_PRIORITY.add("distribution");
        DEFAULT_LICENSE_CHILDREN_PRIORITY.add("comments");

        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("id");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("name");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("email");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("url");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("organization");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("organizationUrl");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("roles");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("timezone");
        DEFAULT_DEVELOPER_CHILDREN_PRIORITY.add("properties");

        DEFAULT_SCM_CHILDREN_PRIORITY.add("connection");
        DEFAULT_SCM_CHILDREN_PRIORITY.add("developerConnection");
        DEFAULT_SCM_CHILDREN_PRIORITY.add("tag");
        DEFAULT_SCM_CHILDREN_PRIORITY.add("url");

        DEFAULT_ORGANIZATION_CHILDREN_PRIORITY.add("name");
        DEFAULT_ORGANIZATION_CHILDREN_PRIORITY.add("url");
    }

    public PomSorter(Project project)
    {
        this.project = project;
        if (order.isEmpty()) {
            restoreDefaults();
        }
    }

    @NotNull
    private static XmlComment createComment(Project project, String commentText) throws IncorrectOperationException
    {
        if (null == commentText) {
            commentText = "";
        }
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

    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    public SortMode getDefaultSortMode()
    {
        return defaultSortMode;
    }

    public void setDefaultSortMode(SortMode defaultSortMode)
    {
        this.defaultSortMode = defaultSortMode;
    }

    @Override
    public State getState()
    {
        return new State(order.values(), getDefaultSortMode());
    }

    public Map<String, TagSortingSetting> getTagSortingSettings()
    {
        return order;
    }

    public void disposeComponent()
    {
    }

    public void initComponent()
    {
    }

    @Override
    public void loadState(State state)
    {
        defaultSortMode = state.defaultSortMode;
        order.clear();
        for (TagSortingSetting setting : state.order) {
            order.put(setting.getName(), setting);
        }
    }

    public void projectClosed()
    {
    }

    public void projectOpened()
    {
    }

    public void restoreDefaults()
    {
        addToOrder(new TagSortingSetting("project", SortMode.FIXED, DEFAULT_PROJECT_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("contributor", SortMode.FIXED, DEFAULT_CONTRIBUTOR_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("profile", SortMode.FIXED, DEFAULT_PROFILE_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("activation", SortMode.FIXED, DEFAULT_ACTIVATION_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("**/activation/file", SortMode.FIXED, DEFAULT_ACTIVATION_FILE_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("**/activation/os", SortMode.FIXED, DEFAULT_ACTIVATION_OS_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("**/activation/property", SortMode.FIXED, DEFAULT_ACTIVATION_PROPERTY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("exclusion", SortMode.FIXED, DEFAULT_EXCLUSION_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("reporting", SortMode.FIXED, DEFAULT_REPORTING_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("**/reporting/plugins/plugin", SortMode.FIXED, DEFAULT_REPORT_PLUGIN_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("reportSets", SortMode.FIXED, DEFAULT_REPORT_SETS_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("**/profile/build", SortMode.FIXED, DEFAULT_BUILD_BASE_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("/project/build", SortMode.FIXED, DEFAULT_BUILD_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("resource", SortMode.FIXED, DEFAULT_RESOURCE_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("distributionManagement", SortMode.FIXED, DEFAULT_DISTRIBUTION_MANAGEMENT_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("dependencies", SortMode.ARTIFACT, Collections.<String>emptyList()));
        addToOrder(new TagSortingSetting("dependency", SortMode.FIXED, DEFAULT_DEPENDENCY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("execution", SortMode.FIXED, DEFAULT_PLUGIN_EXECUTION_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("plugin", SortMode.FIXED, DEFAULT_PLUGIN_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("pluginRepository", SortMode.FIXED, DEFAULT_PLUGIN_REPOSITORY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("site", SortMode.FIXED, DEFAULT_SITE_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("relocation", SortMode.FIXED, DEFAULT_RELOCATION_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("/project/dependencyManagement/dependencies", SortMode.NONE, Collections.<String>emptyList()));
        addToOrder(
            new TagSortingSetting("/project/distributionManagement/snapshotRepository", SortMode.FIXED, DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("/project/distributionManagement/repository", SortMode.FIXED, DEFAULT_DEPLOYMENT_REPOSITORY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("/project/repositories", SortMode.SUBTAG, "id"));
        addToOrder(new TagSortingSetting("/project/repositories/repository", SortMode.FIXED, DEFAULT_REPOSITORY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("releases", SortMode.FIXED, DEFAULT_REPOSITORY_POLICY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("snapshots", SortMode.FIXED, DEFAULT_REPOSITORY_POLICY_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("mailingList", SortMode.FIXED, DEFAULT_MAILING_LIST_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("extension", SortMode.FIXED, DEFAULT_EXTENSION_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("issueManagement", SortMode.FIXED, DEFAULT_ISSUE_MANAGEMENT_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("parent", SortMode.FIXED, DEFAULT_PARENT_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("ciManagement", SortMode.FIXED, DEFAULT_CI_MANAGEMENT_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("notifier", SortMode.FIXED, DEFAULT_NOTIFIER_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("license", SortMode.FIXED, DEFAULT_LICENSE_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("developers", SortMode.SUBTAG, "id"));
        addToOrder(new TagSortingSetting("developer", SortMode.FIXED, DEFAULT_DEVELOPER_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("scm", SortMode.FIXED, DEFAULT_SCM_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("organization", SortMode.FIXED, DEFAULT_ORGANIZATION_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("organization", SortMode.FIXED, DEFAULT_RESOURCE_CHILDREN_PRIORITY));
        addToOrder(new TagSortingSetting("plugins", SortMode.ARTIFACT, Collections.<String>emptyList()));
        addToOrder(new TagSortingSetting("extensions", SortMode.ARTIFACT, Collections.<String>emptyList()));
        addToOrder(new TagSortingSetting("roles", SortMode.SUBTAG, "name"));
        addToOrder(new TagSortingSetting("notifiers", SortMode.SUBTAG, "type"));
        addToOrder(new TagSortingSetting("filters", SortMode.SUBTAG, "type"));
        addToOrder(new TagSortingSetting("executions", SortMode.SUBTAG, "id"));
        addToOrder(new TagSortingSetting("resources", SortMode.SUBTAG, "directory"));
        addToOrder(new TagSortingSetting("testResources", SortMode.SUBTAG, "directory"));
        addToOrder(new TagSortingSetting("pluginRepositories", SortMode.SUBTAG, "id"));
        addToOrder(new TagSortingSetting("licenses", SortMode.SUBTAG, "name"));
        addToOrder(new TagSortingSetting("contributors", SortMode.SUBTAG, "name"));
        addToOrder(new TagSortingSetting("mailingLists", SortMode.SUBTAG, "name"));
        addToOrder(new TagSortingSetting("extensions", SortMode.ARTIFACT, Collections.<String>emptyList()));
        addToOrder(new TagSortingSetting("exclusions", SortMode.ARTIFACT, Collections.<String>emptyList()));
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

    private void addToOrder(TagSortingSetting setting)
    {
        order.put(setting.getName(), setting);
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

    private Comparator<XmlTag> getAttributeSorter(String attribute)
    {
        AttributeComparator comparator = attributeComparators.get(attribute);
        if (null == comparator) {
            comparator = new AttributeComparator(attribute);
            attributeComparators.put(attribute, comparator);
        }
        return comparator;
    }

    private SubtagComparator getSubtagSorter(String attribute)
    {
        SubtagComparator comparator = subtagComparators.get(attribute);
        if (null == comparator) {
            comparator = new SubtagComparator(attribute);
            subtagComparators.put(attribute, comparator);
        }
        return comparator;
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
            childTag.putUserData(COMMENT_KEY, null);
            final XmlTag xmlTag = tag.createChildTag(childTag.getName(), null, childTag.getValue().getText(), false);
            for (XmlAttribute attribute : childTag.getAttributes()) {
                xmlTag.setAttribute(attribute.getName(), attribute.getNamespace(), attribute.getValue());
            }
            if (xmlTag.getSubTags().length == 0 && xmlTag.getValue().getChildren().length == 0) {
                xmlTag.collapseIfEmpty();
            }
            previousPsiElement = tag.addAfter(xmlTag, previousPsiElement);
        }
        if (tag.getSubTags().length == 0 && tag.getValue().getChildren().length == 0) {
            tag.collapseIfEmpty();
        }
        appendCommentsIfPresent(tag, previousPsiElement, tag.getUserData(INTERNAL_COMMENT_KEY));
        tag.putUserData(INTERNAL_COMMENT_KEY, null);
    }

    public static enum SortMode {
        NONE,
        ALPHABETIC,
        ARTIFACT,
        ATTRIBUTE,
        SUBTAG,
        FIXED
    }

    public static class ArtifactComparator implements Comparator<XmlTag> {

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

    public static class AttributeComparator implements Comparator<XmlTag> {

        private String attribute;

        public AttributeComparator(String attribute)
        {
            this.attribute = attribute;
        }

        public int compare(XmlTag a, XmlTag b)
        {
            return toString(a).compareTo(toString(b));
        }

        private String toString(XmlTag dependency)
        {
            final String attributeValue = dependency.getAttributeValue(attribute);
            return null == attributeValue ? "" : attributeValue;
        }
    }

    public static class State {

        public SortMode defaultSortMode;

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        public List<TagSortingSetting> order = new ArrayList<TagSortingSetting>();

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

    public static class SubtagComparator implements Comparator<XmlTag> {

        private String tagName;

        public SubtagComparator(String tagName)
        {
            this.tagName = tagName;
        }

        public int compare(XmlTag a, XmlTag b)
        {
            return toString(a).compareTo(toString(b));
        }

        private String toString(XmlTag dependency)
        {
            final String subTagText = dependency.getSubTagText(tagName);
            return null == subTagText ? "" : subTagText;
        }
    }

    public static class TagSortingSetting implements Serializable {

        private String attributeName;

        private SortMode mode;

        private String name;

        private List<String> order;

        public TagSortingSetting()
        {
        }

        public TagSortingSetting(@NotNull String name, @NotNull SortMode mode, @NotNull Collection<String> order)
        {
            this.name = name;
            this.mode = mode;
            this.order = new ArrayList<String>(order);
        }

        public TagSortingSetting(@NotNull String name, @NotNull SortMode mode, @NotNull String attributeName)
        {
            this.name = name;
            this.mode = mode;
            this.attributeName = attributeName;
        }

        public String getAttributeName()
        {
            return attributeName;
        }

        public void setAttributeName(String attributeName)
        {
            this.attributeName = attributeName;
        }

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

    private class FixedOrderComparator implements Comparator<XmlTag> {

        private final NullComparator nullComparator = new NullComparator();

        private final Map<String, Integer> priority;

        private FixedOrderComparator()
        {
            this.priority = new HashMap<String, Integer>();
        }

        public void setOrder(List<String> order)
        {
            priority.clear();
            for (int i = 0, orderSize = order.size(); i < orderSize; i++) {
                String value = order.get(i);
                priority.put(value, i);
            }
        }

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
    }

    private class PomSortVisitor extends XmlRecursiveElementVisitor {

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
            TagSortingSetting tagSortingSetting = getSortingSetting(tag);
            SortMode mode = tagSortingSetting == null ? getDefaultSortMode() : tagSortingSetting.getMode();
            if (SortMode.ALPHABETIC.equals(mode)) {
                sortChildren(tag, tagNameComparator);
            } else if (SortMode.FIXED.equals(mode)) {
                fixedOrderComparator.setOrder(tagSortingSetting == null ? Collections.<String>emptyList() : tagSortingSetting.getOrder());
                sortChildren(tag, fixedOrderComparator);
            } else if (SortMode.ATTRIBUTE.equals(mode) && null != tagSortingSetting) {
                sortChildren(tag, getAttributeSorter(tagSortingSetting.getAttributeName()));
            } else if (SortMode.SUBTAG.equals(mode) && null != tagSortingSetting) {
                sortChildren(tag, getSubtagSorter(tagSortingSetting.getAttributeName()));
            } else if (SortMode.ARTIFACT.equals(mode)) {
                sortChildren(tag, artifactComparator);
            }
        }

        private String getPath(XmlTag tag)

        {
            return (tag.getParentTag() == null ? "" : getPath(tag.getParentTag())) + "/" + tag.getName();
        }

        private TagSortingSetting getSortingSetting(XmlTag tag)
        {
            TagSortingSetting tagSortingSetting = null;
            final String path = getPath(tag);
            final String tagName = tag.getName();
            for (Map.Entry<String, TagSortingSetting> entry : order.entrySet()) {
                String pattern = entry.getKey()
                    .replaceAll("^\\*/", "[^/]*/")
                    .replaceAll("/\\*/", "/[^/]*/")
                    .replaceAll("/\\*$", "/[^/]*")
                    .replaceAll("^\\*\\*/", ".*/")
                    .replaceAll("/\\*\\*/", "/.*/")
                    .replaceAll("/\\*\\*$", "/.*");
                final boolean matches;
                try {
                    matches = path.matches(pattern);
                } catch (PatternSyntaxException e) {
                    Notifications.inform(String.format("Invalid pattern '%s' for tag %s", entry.getKey(), tagName), "Please select POM.xml first", project);
                    break;
                }
                if (matches) {
                    tagSortingSetting = entry.getValue();
                }
            }
            if (null == tagSortingSetting) {
                tagSortingSetting = order.get(tagName);
            }
            return tagSortingSetting;
        }
    }

    private class TagNameComparator implements Comparator<XmlTag> {

        @Override
        public int compare(XmlTag a, XmlTag b)
        {
            return a.getName().compareTo(b.getName());
        }
    }
}
