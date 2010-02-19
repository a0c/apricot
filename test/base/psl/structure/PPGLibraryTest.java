package base.psl.structure;

import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import static org.junit.Assert.*;
import io.PPGLibraryReader;

import java.io.File;

import helpers.PSLProperties;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 17.09.2008
 * <br>Time: 15:39:31
 */
public class PPGLibraryTest {
    private static final String PPG_LIBRARY_PATH = "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\PSL\\ppg.lib2"; //todo: change back to ppg_future.lib

    private PPGLibrary library;

    @Before
    public void initLibrary() throws Exception {
        library = createLibrary();
    }

    public static PPGLibrary createLibrary() throws Exception {
        PPGLibraryReader libraryReader = new PPGLibraryReader(new File(PPG_LIBRARY_PATH));
        return libraryReader.getPpgLibrary();
    }

    @Test
    public void libraryReadCorrectly() {
        /* Check number of operators */
        assertEquals("Number of read operators is incorrect", 14, library.pslOperators.length);
        /* Check each operator */
        for (int i = 0; i < library.pslOperators.length; i++) {
            PSLOperator pslOperator = library.pslOperators[i];
            /* Check non-null models of operators */
            assertNotNull("Operator " + pslOperator.getName() + " misses model", pslOperator.getModel());
        }
    }

    @Test
//    @Ignore
    public void correctOperatorExtracted() {

        for (int i = 0; i < PSLProperties.exampleOperatorArrayOld.length; i++){
            assertEquals(PSLProperties.exampleOperatorArrayOld[i][1], library.extractOperator(PSLProperties.exampleOperatorArrayOld[i][0]).getName());
            i+=0;
        }
        
    }
}
