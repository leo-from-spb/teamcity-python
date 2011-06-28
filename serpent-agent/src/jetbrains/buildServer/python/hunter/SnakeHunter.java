package jetbrains.buildServer.python.hunter;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static jetbrains.buildServer.utils.YASU.*;
import static jetbrains.buildServer.utils.YAFU.*;


/**
 * Base class for "Snake Hunter" - an utility designed for
 * detecting python installation on agent (like Java Dowser).
 *
 * @author Leonid Bushuev from JetBrains
 */
public abstract class SnakeHunter
{

    protected final Map<String,String> env2 = new TreeMap<String,String>();
    protected final List<File> runPaths = new ArrayList<File>(24);


    protected void init()
    {
        initEnv2();
        initPaths();
    }


    private void initEnv2()
    {
        Map<String,String> env = System.getenv();
        for (Map.Entry<String,String> envEntry: env.entrySet())
        {
            String key = envEntry.getKey().toLowerCase();
            if (key.endsWith("python") || key.equalsIgnoreCase("PATH"))
            {
                String value = envEntry.getValue();
                env2.put(key,value);
            }
        }
    }

    private void initPaths()
    {
        String pathVar = trimAndNull(env2.get("path"));
        if (pathVar != null)
        {
            List<String> pathEntries = StringUtil.split(pathVar, true, File.pathSeparatorChar);
            for (String pathEntry: pathEntries)
                runPaths.add(new File(pathEntry));
        }
    }


    protected abstract InstalledPythons hunt();

}
