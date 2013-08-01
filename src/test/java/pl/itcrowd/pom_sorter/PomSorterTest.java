package pl.itcrowd.pom_sorter;

import com.intellij.lang.StdLanguages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.PsiTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PomSorterTest extends PsiTestCase {
// -------------------------- OTHER METHODS --------------------------

    /**
     * Check if multiple sorting run gets same result.
     */
    public void testMultipleSort()
    {
//        Given
        final String pomContents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n" + "    <target>\n"
                + "        <echo message=\"bla\">a</echo>\n" + "        <other>\n" + "            <some>1</some>\n" + "            <asa>2</asa>\n"
                + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n" + "            <!--Other comment-->\n" + "        </other>\n"
                + "        <echo message=\"bla2\"/>\n" + "    </target>\n" + "    <!--<echo message=\"rox\">asa1</echo>-->\n"
                + "    <!--<echo message=\"rox\">asa2</echo>-->\n" + "    <!--<echo message=\"rox\">asa3</echo>-->\n" + "</project>\n";
        final String expectedContents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n" + "    <target>\n"
                + "        <echo message=\"bla\">a</echo>\n" + "        <other>\n" + "            <asa>2</asa>\n" + "            <some>1</some>\n"
                + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n" + "            <!--Other comment-->\n" + "        </other>\n"
                + "        <echo message=\"bla2\"/>\n" + "    </target>\n" + "    <!--<echo message=\"rox\">asa1</echo>-->\n"
                + "    <!--<echo message=\"rox\">asa2</echo>-->\n" + "    <!--<echo message=\"rox\">asa3</echo>-->\n" + "</project>\n";
        final PomSorter pomSorter = new PomSorter(getProject());
        final ArrayList<PomSorter.TagSortingSetting> order = new ArrayList<PomSorter.TagSortingSetting>();
        order.add(new PomSorter.TagSortingSetting("target", PomSorter.SortMode.NONE, Collections.<String>emptyList()));
        pomSorter.loadState(new PomSorter.State(order, PomSorter.SortMode.ALPHABETIC));
        final PsiFile psiFile = super.createDummyFile("pom.xml", pomContents);
        final XmlFile xmlFile = (XmlFile) psiFile.getViewProvider().getPsi(StdLanguages.XML);

//        When
        pomSorter.sortFile(xmlFile);
        pomSorter.sortFile(xmlFile);
        pomSorter.sortFile(xmlFile);

//        Then
        assertEquals(expectedContents, xmlFile.getText());
    }

    public void testPattern()
    {
//        Given
        final String pomContents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n" + "    <target>\n"
                + "        <echo message=\"bla\">a</echo>\n" + "        <other>\n" + "            <some>1</some>\n" + "            <asa>2</asa>\n"
                + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n" + "            <!--Other comment-->\n" + "        </other>\n"
                + "        <echo message=\"bla2\"/>\n" + "    </target>\n" + "    <!--<echo message=\"rox\">asa1</echo>-->\n"
                + "    <!--<echo message=\"rox\">asa2</echo>-->\n" + "    <!--<echo message=\"rox\">asa3</echo>-->\n" + "    <plugin>\n"
                + "        <groupId>org.apache.maven.plugins</groupId>\n" + "        <artifactId>maven-antrun-plugin</artifactId>\n" + "        <executions>\n"
                + "            <execution>\n" + "                <id>20-restart-tomcat</id>\n" + "                <phase>install</phase>\n"
                + "                <goals>\n" + "                    <goal>run</goal>\n" + "                </goals>\n" + "                <configuration>\n"
                + "                    <target>\n" + "                        <echo message=\"Stopping server ...\"/>\n"
                + "                        <sshexec host=\"${tomcat.host}\" command=\"${tomcat.script.shutdown}\" username=\"${ssh.user}\" keyfile=\"${user.home}/.ssh/id_dsa\"\n"
                + "                                 trust=\"true\"/>\n" + "                        <echo message=\"Starting server ...\"/>\n"
                + "                        <sshexec host=\"${tomcat.host}\" command=\"${tomcat.script.startup}\" username=\"${ssh.user}\" keyfile=\"${user.home}/.ssh/id_dsa\"\n"
                + "                                 trust=\"true\"/>\n" + "                    </target>\n" + "                </configuration>\n"
                + "            </execution>\n" + "        </executions>\n" + "    </plugin>\n" + "    <!--<echo message=\"rox\">asa4</echo>-->\n"
                + "    <!--<echo message=\"rox\">asa5</echo>-->\n" + "</project>";
        final String contentsWithTargetNotSortedAndRestSortedAlphabetically =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa1</echo>-->\n"
                + "    <!--<echo message=\"rox\">asa2</echo>-->\n" + "    <!--<echo message=\"rox\">asa3</echo>-->\n" + "    <plugin>\n"
                + "        <artifactId>maven-antrun-plugin</artifactId>\n" + "        <executions>\n" + "            <execution>\n"
                + "                <configuration>\n" + "                    <target>\n" + "                        <echo message=\"Stopping server ...\"/>\n"
                + "                        <sshexec host=\"${tomcat.host}\" command=\"${tomcat.script.shutdown}\" username=\"${ssh.user}\"\n"
                + "                                 keyfile=\"${user.home}/.ssh/id_dsa\"\n" + "                                 trust=\"true\"/>\n"
                + "                        <echo message=\"Starting server ...\"/>\n"
                + "                        <sshexec host=\"${tomcat.host}\" command=\"${tomcat.script.startup}\" username=\"${ssh.user}\"\n"
                + "                                 keyfile=\"${user.home}/.ssh/id_dsa\"\n" + "                                 trust=\"true\"/>\n"
                + "                    </target>\n" + "                </configuration>\n" + "                <goals>\n"
                + "                    <goal>run</goal>\n" + "                </goals>\n" + "                <id>20-restart-tomcat</id>\n"
                + "                <phase>install</phase>\n" + "            </execution>\n" + "        </executions>\n"
                + "        <groupId>org.apache.maven.plugins</groupId>\n" + "    </plugin>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n"
                + "    <target>\n" + "        <echo message=\"bla\">a</echo>\n" + "        <other>\n" + "            <asa>2</asa>\n"
                + "            <some>1</some>\n" + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n"
                + "            <!--Other comment-->\n" + "        </other>\n" + "        <echo message=\"bla2\"/>\n" + "    </target>\n"
                + "    <!--<echo message=\"rox\">asa4</echo>-->\n" + "    <!--<echo message=\"rox\">asa5</echo>-->\n" + "</project>";
        final String contentsForB =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n" + "    <target>\n"
                + "        <echo message=\"bla\">a</echo>\n" + "        <other>\n" + "            <some>1</some>\n" + "            <asa>2</asa>\n"
                + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n" + "            <!--Other comment-->\n" + "        </other>\n"
                + "        <echo message=\"bla2\"/>\n" + "    </target>\n" + "    <!--<echo message=\"rox\">asa1</echo>-->\n"
                + "    <!--<echo message=\"rox\">asa2</echo>-->\n" + "    <!--<echo message=\"rox\">asa3</echo>-->\n" + "    <plugin>\n"
                + "        <groupId>org.apache.maven.plugins</groupId>\n" + "        <artifactId>maven-antrun-plugin</artifactId>\n" + "        <executions>\n"
                + "            <execution>\n" + "                <id>20-restart-tomcat</id>\n" + "                <phase>install</phase>\n"
                + "                <goals>\n" + "                    <goal>run</goal>\n" + "                </goals>\n" + "                <configuration>\n"
                + "                    <target>\n" + "                        <echo message=\"Stopping server ...\"/>\n"
                + "                        <sshexec host=\"${tomcat.host}\" command=\"${tomcat.script.shutdown}\" username=\"${ssh.user}\"\n"
                + "                                 keyfile=\"${user.home}/.ssh/id_dsa\"\n" + "                                 trust=\"true\"/>\n"
                + "                        <echo message=\"Starting server ...\"/>\n"
                + "                        <sshexec host=\"${tomcat.host}\" command=\"${tomcat.script.startup}\" username=\"${ssh.user}\"\n"
                + "                                 keyfile=\"${user.home}/.ssh/id_dsa\"\n" + "                                 trust=\"true\"/>\n"
                + "                    </target>\n" + "                </configuration>\n" + "            </execution>\n" + "        </executions>\n"
                + "    </plugin>\n" + "    <!--<echo message=\"rox\">asa4</echo>-->\n" + "    <!--<echo message=\"rox\">asa5</echo>-->\n" + "</project>";
        final String contentsWithTargetSortedAlphabetically =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n" + "    <target>\n"
                + "        <echo message=\"bla\">a</echo>\n" + "        <echo message=\"bla2\"/>\n" + "        <other>\n" + "            <some>1</some>\n"
                + "            <asa>2</asa>\n" + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n" + "            <!--Other comment-->\n"
                + "        </other>\n" + "    </target>\n" + "    <!--<echo message=\"rox\">asa1</echo>-->\n" + "    <!--<echo message=\"rox\">asa2</echo>-->\n"
                + "    <!--<echo message=\"rox\">asa3</echo>-->\n" + "    <plugin>\n" + "        <groupId>org.apache.maven.plugins</groupId>\n"
                + "        <artifactId>maven-antrun-plugin</artifactId>\n" + "        <executions>\n" + "            <execution>\n"
                + "                <id>20-restart-tomcat</id>\n" + "                <phase>install</phase>\n" + "                <goals>\n"
                + "                    <goal>run</goal>\n" + "                </goals>\n" + "                <configuration>\n"
                + "                    <target>\n" + "                        <echo message=\"Stopping server ...\"/>\n"
                + "                        <echo message=\"Starting server ...\"/>\n"
                + "                        <sshexec host=\"${tomcat.host}\" command=\"${tomcat.script.shutdown}\" username=\"${ssh.user}\"\n"
                + "                                 keyfile=\"${user.home}/.ssh/id_dsa\" trust=\"true\"/>\n"
                + "                        <sshexec command=\"${tomcat.script.startup}\" host=\"${tomcat.host}\"\n"
                + "                                 keyfile=\"${user.home}/.ssh/id_dsa\" trust=\"true\" username=\"${ssh.user}\"/>\n"
                + "                    </target>\n" + "                </configuration>\n" + "            </execution>\n" + "        </executions>\n"
                + "    </plugin>\n" + "    <!--<echo message=\"rox\">asa4</echo>-->\n" + "    <!--<echo message=\"rox\">asa5</echo>-->\n" + "</project>";
        final PomSorter pomSorterA = createPomSorter("target", PomSorter.SortMode.NONE, PomSorter.SortMode.ALPHABETIC);
        final PomSorter pomSorterB = createPomSorter("pro/target", PomSorter.SortMode.ALPHABETIC, PomSorter.SortMode.NONE);
        final PomSorter pomSorterC = createPomSorter("/project/target", PomSorter.SortMode.ALPHABETIC, PomSorter.SortMode.NONE);
        pomSorterC.getTagSortingSettings()
            .put("target", new PomSorter.TagSortingSetting("target", PomSorter.SortMode.FIXED, Arrays.asList("some", "asa", "echo")));
        final XmlFile xmlFileA = getFile(pomContents);
        final XmlFile xmlFileB = getFile(pomContents);
        final XmlFile xmlFileC = getFile(pomContents);

//        When
        pomSorterA.sortFile(xmlFileA);
        pomSorterB.sortFile(xmlFileB);
        pomSorterC.sortFile(xmlFileC);

//        Then
        assertEquals(contentsWithTargetNotSortedAndRestSortedAlphabetically, xmlFileA.getText());
        assertEquals(contentsForB, xmlFileB.getText());
        assertEquals(contentsWithTargetSortedAlphabetically, xmlFileC.getText());
    }

    /**
     * Check proper handling of empty comments.
     * Initial and expected contents:
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?>
     * &lt;project>
     *     &lt;!---->
     *     &lt;target/>
     * &lt;/project>
     * </pre>
     */
    public void testSortWithEmptyComment()
    {
//        Given
        final String pomContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!---->\n" + "    <target/>\n" + "</project>\n";
        final PomSorter pomSorter = new PomSorter(getProject());
        final PsiFile psiFile = super.createDummyFile("pom.xml", pomContents);
        final XmlFile xmlFile = (XmlFile) psiFile.getViewProvider().getPsi(StdLanguages.XML);

//        When
        pomSorter.sortFile(xmlFile);

//        Then
        assertEquals(pomContents, xmlFile.getText());
    }

    /**
     * Default sort mode is alphabetic and "target" should not be sorted.
     * Expected result is that target remains unsorted, but "other" gets sorted.
     * Initial contents:
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?>
     *     &lt;project>
     *         &lt;!--&lt;echo message="rox">asa&lt;/echo>-->
     *         &lt;target>
     *             &lt;echo message="bla">a&lt;/echo>
     *             &lt;other>
     *                 &lt;some>1&lt;/some>
     *                 &lt;asa>2&lt;/asa>
     *                 &lt;!--Jojo dla some 2-->
     *                 &lt;some>2&lt;/some>
     *                 &lt;!--Other comment-->
     *             &lt;/other>
     *             &lt;echo message="bla2"/>
     *         &lt;/target>
     * &lt;/project>
     * </pre>
     */
    public void testSortWithUnsortableTargtAndEmptyTag()
    {
//        Given
        final String pomContents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n" + "    <target>\n"
                + "        <echo message=\"bla\">a</echo>\n" + "        <other>\n" + "            <some>1</some>\n" + "            <asa>2</asa>\n"
                + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n" + "            <!--Other comment-->\n" + "        </other>\n"
                + "        <echo message=\"bla2\"/>\n" + "    </target>\n" + "</project>\n";
        final String expectedContents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "    <!--<echo message=\"rox\">asa</echo>-->\n" + "    <target>\n"
                + "        <echo message=\"bla\">a</echo>\n" + "        <other>\n" + "            <asa>2</asa>\n" + "            <some>1</some>\n"
                + "            <!--Jojo dla some 2-->\n" + "            <some>2</some>\n" + "            <!--Other comment-->\n" + "        </other>\n"
                + "        <echo message=\"bla2\"/>\n" + "    </target>\n" + "</project>\n";
        final PomSorter pomSorter = new PomSorter(getProject());
        final ArrayList<PomSorter.TagSortingSetting> order = new ArrayList<PomSorter.TagSortingSetting>();
        order.add(new PomSorter.TagSortingSetting("target", PomSorter.SortMode.NONE, Collections.<String>emptyList()));
        pomSorter.loadState(new PomSorter.State(order, PomSorter.SortMode.ALPHABETIC));
        final PsiFile psiFile = super.createDummyFile("pom.xml", pomContents);
        final XmlFile xmlFile = (XmlFile) psiFile.getViewProvider().getPsi(StdLanguages.XML);

//        When
        pomSorter.sortFile(xmlFile);

//        Then
        assertEquals(expectedContents, xmlFile.getText());
    }

    public void testSortWithChildTag()
    {
//        Givne
        final String pomContents =
            "<pluginRepositories>\n" + "   <pluginRepository>\n" + "       <id>jojo</id>\n" + "   </pluginRepository>\n" + "   <pluginRepository>\n"
                + "       <id>arni</id>\n" + "   </pluginRepository>\n" + "</pluginRepositories>";
        final String expectedContents =
            "<pluginRepositories>\n" + "   <pluginRepository>\n" + "       <id>arni</id>\n" + "   </pluginRepository>\n" + "   <pluginRepository>\n"
                + "       <id>jojo</id>\n" + "   </pluginRepository>\n" + "</pluginRepositories>";
        final PomSorter pomSorter = new PomSorter(getProject());
        final ArrayList<PomSorter.TagSortingSetting> order = new ArrayList<PomSorter.TagSortingSetting>();
        order.add(new PomSorter.TagSortingSetting("pluginRepositories", PomSorter.SortMode.SUBTAG, "id"));
        pomSorter.loadState(new PomSorter.State(order, PomSorter.SortMode.ALPHABETIC));
        final PsiFile psiFile = super.createDummyFile("pom.xml", pomContents);
        final XmlFile xmlFile = (XmlFile) psiFile.getViewProvider().getPsi(StdLanguages.XML);
//        When
        pomSorter.sortFile(xmlFile);

//        Then
        assertEquals(expectedContents, xmlFile.getText());
    }

    public void testSortWithAttribute()
    {
//        Givne
        final String pomContents = "<profile kind=\"CodeFormatterProfile\" version=\"11\">\n" + "    <setting id=\"paren_in_if\" value=\"do not insert\"/>\n"
            + "    <setting id=\"colon_in_assert\" value=\"insert\"/>\n" + "    <setting id=\"enum_constant\" value=\"end_of_line\"/>\n"
            + "    <setting id=\"before_semicolon\" value=\"do not insert\"/>\n" + "</profile>";
        final String expectedContents =
            "<profile kind=\"CodeFormatterProfile\" version=\"11\">\n" + "    <setting id=\"before_semicolon\" value=\"do not insert\"/>\n"
                + "    <setting id=\"colon_in_assert\" value=\"insert\"/>\n" + "    <setting id=\"enum_constant\" value=\"end_of_line\"/>\n"
                + "    <setting id=\"paren_in_if\" value=\"do not insert\"/>\n" + "</profile>";
        final PomSorter pomSorter = new PomSorter(getProject());
        final ArrayList<PomSorter.TagSortingSetting> order = new ArrayList<PomSorter.TagSortingSetting>();
        order.add(new PomSorter.TagSortingSetting("profile", PomSorter.SortMode.ATTRIBUTE, "id"));
        pomSorter.loadState(new PomSorter.State(order, PomSorter.SortMode.ALPHABETIC));
        final PsiFile psiFile = super.createDummyFile("pom.xml", pomContents);
        final XmlFile xmlFile = (XmlFile) psiFile.getViewProvider().getPsi(StdLanguages.XML);
//        When
        pomSorter.sortFile(xmlFile);

//        Then
        assertEquals(expectedContents, xmlFile.getText());
    }

    private PomSorter createPomSorter(String tagPattern, PomSorter.SortMode tagSortMode, PomSorter.SortMode defaultSortMode)
    {
        final PomSorter pomSorter = new PomSorter(getProject());
        final ArrayList<PomSorter.TagSortingSetting> order = new ArrayList<PomSorter.TagSortingSetting>();
        order.add(new PomSorter.TagSortingSetting(tagPattern, tagSortMode, Collections.<String>emptyList()));
        pomSorter.loadState(new PomSorter.State(order, defaultSortMode));
        return pomSorter;
    }

    private XmlFile getFile(String contents)
    {
        final PsiFile psiFile = createDummyFile("pom.xml", contents);
        return (XmlFile) psiFile.getViewProvider().getPsi(StdLanguages.XML);
    }
}
