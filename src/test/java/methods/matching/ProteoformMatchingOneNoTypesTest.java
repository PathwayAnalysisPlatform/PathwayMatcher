package methods.matching;

import model.Proteoform;
import model.ProteoformFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class ProteoformMatchingOneNoTypesTest {
	static ProteoformFormat pf;
    static ProteoformMatching matcher;
    static Proteoform iP, rP;
    static Long margin = 0L;

    @BeforeAll
    static void setUp() {
        pf = ProteoformFormat.SIMPLE;
        matcher = new ProteoformMatchingOne(false);
        assertEquals(ProteoformMatchingOne.class, matcher.getClass());
    }

    @Test
    void matchesSameAllTest() {

        try {
            iP = pf.getProteoform("A2RUS2;");
            rP = pf.getProteoform("A2RUS2;");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("A2RUS2");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-2");
            rP = pf.getProteoform("A2RUS2-2");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            rP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;01234:12,00000:null,00046:490");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:490,00000:null");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00046:490");
            rP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            assertTrue(matcher.matches(iP, rP, margin));

            // These pass because the input contains all the ptms of the reference
            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2;");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            rP = pf.getProteoform("A2RUS2-2;00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

            // The input still contains the all the reference PTMs because a ptm is repeated 3 times
            iP = pf.getProteoform("A2RUS2;01234:12,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-2;00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-2;00048:null,00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00048:490");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-2;00048:490,00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertTrue(matcher.matches(iP, rP, margin));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentUniProtAccTest() {

        try {
            iP = pf.getProteoform("A2RUS2;");
            rP = pf.getProteoform("P01308;");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("P01308");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("P01308;00046:472");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00046:472,00046:490");
            rP = pf.getProteoform("P01308;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            rP = pf.getProteoform("P01308;00000:null,00046:490");
            assertFalse(matcher.matches(iP, rP, margin));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentIsoformTest() {

        try {
            iP = pf.getProteoform("A2RUS2-1;");
            rP = pf.getProteoform("A2RUS2;");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("A2RUS2-1");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-1");
            rP = pf.getProteoform("A2RUS2-2");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-1;00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-1;00046:472,00046:490");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            rP = pf.getProteoform("A2RUS2-3;00000:null,00046:490");
            assertFalse(matcher.matches(iP, rP, margin));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentNumberOfPtmsTest() {

        // They fail because some of the PTMs in the input are not in the reference. They would match if it was the other way around
        try {
            iP = pf.getProteoform("A2RUS2;");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2-2;00048:10");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP, margin));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchDifferentPtmTypesTest() {

        try {
            iP = pf.getProteoform("A2RUS2;00048:472");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00000:472");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00000:472,00000:453");
            rP = pf.getProteoform("A2RUS2;00000:453,00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00000:472,00000:453");
            rP = pf.getProteoform("A2RUS2;00000:460,00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

            iP = pf.getProteoform("A2RUS2;00000:472,00000:453");
            rP = pf.getProteoform("A2RUS2;00000:460,00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentPtmCoordinatesTest() {

        try {
            iP = pf.getProteoform("A2RUS2;00048:400");
            rP = pf.getProteoform("A2RUS2;00048:472");
            assertFalse(matcher.matches(iP, rP, margin));

            // This one matches because the null is a wild card for the 472
            iP = pf.getProteoform("A2RUS2;00046:null");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP, margin));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }
}