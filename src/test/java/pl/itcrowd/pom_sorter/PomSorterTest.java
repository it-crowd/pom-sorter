package pl.itcrowd.pom_sorter;

import com.intellij.lang.StdLanguages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.PsiTestCase;

import java.util.ArrayList;
import java.util.Collections;

public class PomSorterTest extends PsiTestCase {

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
}
