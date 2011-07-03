package jetbrains.buildServer.python.hunter;

import java.io.File;
import java.util.Set;

/**
 * Python detector for MacOS.
 * @author Leonid Bushuev from JetBrains
 */
public class SnakeHunterForMac extends SnakeHunter
{
    //// CLASSIC PYTHONS HUNTING \\\\\


    @Override
    protected void collectDirsToLookForClassicPython(Set<File> dirsToLook)
    {
        dirsToLook.addAll(runPaths);

        String thePythonHome = System.getenv("PYTHONHOME");

        if (thePythonHome != null)
            dirsToLook.add(new File(thePythonHome));
    }


    @Override
    protected String[] getClassicPythonExeFiles()
    {
        return new String[]{"python"};
    }



    //// IRON PYTHONS HUNTING \\\\\


    @Override
    protected void collectDirsToLookForIronPython(Set<File> dirsToLook)
    {
        dirsToLook.addAll(runPaths);
    }


    @Override
    protected String[] getIronPythonExeFiles()
    {
        return new String[]{"ipy"};
    }
}
