package jetbrains.buildServer.python.common;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author Leonid Bushuev from JetBrains
 */
public class PythonVersionTest
{

    @Test
    public void testComparability1() {
        PythonVersion v12 = new PythonVersion(1, 2, "");
        PythonVersion v21 = new PythonVersion(2, 1, "");
        int z = v12.compareTo(v21);
        assertTrue(z < 0);
    }

    @Test
    public void testComparability2() {
        PythonVersion v26 = new PythonVersion(2, 6, "");
        PythonVersion v27 = new PythonVersion(2, 7, "");
        int z = v26.compareTo(v27);
        assertTrue(z < 0);
    }

    @Test
    public void testComparability3() {
        PythonVersion v331 = new PythonVersion(3, 3, "3.3.1");
        PythonVersion v333 = new PythonVersion(3, 3, "3.3.3");
        int z = v331.compareTo(v333);
        assertTrue(z < 0);
        assertFalse(v331.equals(v333));
    }

}
