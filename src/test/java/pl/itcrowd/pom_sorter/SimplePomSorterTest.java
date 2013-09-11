package pl.itcrowd.pom_sorter;

import junit.framework.TestCase;

import java.util.List;

public class SimplePomSorterTest extends TestCase {

    public void testStateOrder() throws Exception
    {
//        Given
        final PomSorter pomSorter = new PomSorter(null);
        final PomSorter.State state = pomSorter.getState();
        assertNotNull(state);
        final List<PomSorter.TagSortingSetting> expectedOrder = state.order;

//        When
        for (int i = 0; i < 10000; i++) {
            final PomSorter.State currentState = pomSorter.getState();
            assertNotNull(currentState);
            assertEquals(expectedOrder, currentState.order);
            assertNotSame(expectedOrder, currentState.order);
            pomSorter.loadState(currentState);
        }
    }
}
