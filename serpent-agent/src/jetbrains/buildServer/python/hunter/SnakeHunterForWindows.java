package jetbrains.buildServer.python.hunter;

import jetbrains.buildServer.python.common.PythonKind;
import jetbrains.buildServer.python.common.PythonVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Python detector for Windows.
 * @author Leonid Bushuev from JetBrains
 */
public class SnakeHunterForWindows extends SnakeHunter
{



    @Override
    protected InstalledPythons hunt()
    {
        InstalledPythons pythons = new InstalledPythons();

        huntClassicPythons(pythons);
        huntIronPythons(pythons);
        huntJythons(pythons);

        return examPythons(pythons);
    }


    private void huntClassicPythons(InstalledPythons pythons)
    {
        List<File> dirsToLook = new ArrayList<File>(this.runPaths);

        lookFiles(dirsToLook, new String[] {"python.exe"}, PythonKind.Classic, pythons);
    }


    private void huntIronPythons(InstalledPythons pythons)
    {
        List<File> dirsToLook = new ArrayList<File>(this.runPaths);

        lookFiles(dirsToLook, new String[] {"ipy64.exe", "ipy.exe", "ipy32.exe"}, PythonKind.Iron, pythons);
    }


    private void huntJythons(InstalledPythons pythons)
    {
        // TODO implmement SnakeHunterForWindows.huntJythons
    }


    private void lookFiles(List<File> dirsToLook, String[] exeNames, PythonKind kind, InstalledPythons foundPythons)
    {
        for (File dir: dirsToLook)
        {
            for (String exeName: exeNames)
            {
                File exeFile = new File(dir, exeName);
                if (exeFile.exists() && exeFile.canExecute())
                {
                    InstalledPython pretendent =
                            new InstalledPython(kind, PythonVersion.zero, exeFile);
                    foundPythons.addPython(pretendent);
                }
            }
        }
    }


    private InstalledPythons examPythons(InstalledPythons pretendents)
    {
        // TODO implmement SnakeHunterForWindows.examPythons
        return pretendents;
    }


}
