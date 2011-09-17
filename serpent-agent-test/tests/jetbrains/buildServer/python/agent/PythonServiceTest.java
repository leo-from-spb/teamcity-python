package jetbrains.buildServer.python.agent;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import org.junit.Test;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

import static jetbrains.buildServer.python.agent.PythonService.*;
import static org.junit.Assert.assertNull;


/**
 * @author Leonid.Bushuev
 */
public class PythonServiceTest
{

    @Test
    public void test_prepareEnv_path()
    {
        String executable = SystemInfo.isWindows
                ? "C:\\python\\python.exe"
                : "/usr/bin/python";
        String dir = SystemInfo.isWindows
                ? "C:\\python"
                : "/usr/bin";

        Map<String,String> env1 = new TreeMap<String,String>();
        env1.put("Aaaaa", "zzzz");
        env1.put("Path", "oooooooooooooooooooooo");
        env1.put("Zeeeebra", "Zeeeebra");

        Map<String,String> env2 =
                prepareEnv('C', env1, executable, null);

        String path2 = env2.get("Path");
        int n = dir.length();
        assertEquals(dir, path2.substring(0,n));

        char separator = SystemInfo.isWindows ? ';' : ':';
        assertEquals(separator, path2.charAt(n));

        assertEquals("oooooooooooooooooooooo", path2.substring(n+1));
    }


    @Test
    public void test_prepareEnv_ppv_1()
    {
        Map<String,String> env1 = new TreeMap<String,String>();
        env1.put("Aaaaa", "zzzz");
        env1.put("Zeeeebra", "Zeeeebra");

        Map<String,String> env2 =
                prepareEnv('C', env1, "python", "iiii");

        String ppv = env2.get("PYTHONPATH");
        assertEquals("iiii", ppv);
    }


    @Test
    public void test_prepareEnv_ppv_2()
    {
        Map<String,String> env1 = new TreeMap<String,String>();
        env1.put("Aaaaa", "zzzz");
        env1.put("PythonPath", "formermodules");
        env1.put("Zeeeebra", "Zeeeebra");

        Map<String,String> env2 =
                prepareEnv('C', env1, "python", "iiii");

        String ppv = env2.get("PythonPath");
        assertEquals("iiii" + File.pathSeparatorChar + "formermodules" , ppv);
    }


    @Test
    public void test_prepareEnv_ppv_iron()
    {
        Map<String,String> env1 = new TreeMap<String,String>();
        env1.put("Aaaaa", "zzzz");
        env1.put("Zeeeebra", "Zeeeebra");

        Map<String,String> env2 =
                prepareEnv('I', env1, "ipy64", "iiii");

        String ppv = env2.get("IRONPYTHONPATH");
        assertEquals("iiii", ppv);
    }


    @Test
    public void test_prepareEnv_ppv_0()
    {
        Map<String,String> env1 = new TreeMap<String,String>();
        env1.put("Aaaaa", "zzzz");
        env1.put("Zeeeebra", "Zeeeebra");

        Map<String,String> env2 =
                prepareEnv('C', env1, "python", null);

        assertNull(env2.get("PythonPath"));
        assertNull(env2.get("PYTHONPATH"));
    }


    @Test
    public void test_ensureExecutable_specified()
            throws RunBuildException
    {
        FakePythonService fps = new FakePythonService();
        fps.addParameter("python-exe", "qwertuyuiop/python");

        String exe = fps.ensureExecutable();

        assertEquals("qwertuyuiop/python", exe);
    }

}
