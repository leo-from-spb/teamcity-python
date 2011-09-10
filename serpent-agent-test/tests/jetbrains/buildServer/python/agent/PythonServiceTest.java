package jetbrains.buildServer.python.agent;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

import static jetbrains.buildServer.python.agent.PythonService.*;


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
                prepareEnv(env1, executable);

        String path2 = env2.get("Path");
        int n = dir.length();
        assertEquals(dir, path2.substring(0,n));

        char separator = SystemInfo.isWindows ? ';' : ':';
        assertEquals(separator, path2.charAt(n));

        assertEquals("oooooooooooooooooooooo", path2.substring(n+1));
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
