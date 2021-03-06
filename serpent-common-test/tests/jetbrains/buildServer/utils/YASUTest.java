package jetbrains.buildServer.utils;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static jetbrains.buildServer.utils.YASU.*;
import static org.junit.Assert.*;



/**
 * @author Leonid.Bushuev
 */
public class YASUTest
{

    @Test
    public void testAdjustCase_contains()
    {
        Set<String> strings = new TreeSet<String>();
        strings.add("Balalaika");
        strings.add("Marimba");
        strings.add("Vargan");

        String x = adjustCase("Marimba", strings);
        assertEquals("Marimba", x);
    }


    @Test
    public void testAdjustCase_mix()
    {
        Set<String> strings = new TreeSet<String>();
        strings.add("baLaLaika");
        strings.add("mariMba");
        strings.add("vargAn");

        String x = adjustCase("Marimba", strings);
        assertEquals("mariMba", x);
    }


    @Test
    public void testAdjustCase_upper()
    {
        Set<String> strings = new TreeSet<String>();
        strings.add("BALALAIKA");
        strings.add("MARIMBA");
        strings.add("VARGAN");

        String x = adjustCase("maRimba", strings);
        assertEquals("MARIMBA", x);
    }


    @Test
    public void testAdjustCase_no()
    {
        Set<String> strings = new TreeSet<String>();
        strings.add("BALALAIKA");
        strings.add("VARGAN");

        String x = adjustCase("maRimba", strings);
        assertEquals("maRimba", x);
    }


    @Test
    public void testSplit_empty() {
        assertTrue(split(null, ',', true, true).isEmpty());
        assertTrue(split("", ',', true, true).isEmpty());
        assertTrue(split("   ", ',', true, true).isEmpty());
    }

    @Test
    public void testSplit_commas() {
        assertTrue(split(",", ',', true, true).isEmpty());
        assertTrue(split(",    ,", ',', true, true).isEmpty());
        assertTrue(split("  ,  ", ',', true, true).isEmpty());
    }

    @Test
    public void testSplit_basic() {
        final List<String> r = split("aaa,bbb,ccc", ',', true, true);
        assertEquals(3, r.size());
        assertEquals("aaa", r.get(0));
        assertEquals("bbb", r.get(1));
        assertEquals("ccc", r.get(2));
    }

    @Test
    public void testSplit_doubleSeparator() {
        final List<String> r = split("C://Windows", '/', true, true);
        assertEquals(2, r.size());
        assertEquals("C:", r.get(0));
        assertEquals("Windows", r.get(1));
    }

}
