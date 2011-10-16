package jetbrains.buildServer.python.hunter;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Python detector for Unix-like OS.
 * @author Leonid Bushuev from JetBrains
 */
class SnakeHunterForUnix extends SnakeHunter
{

    //// CLASSIC PYTHONS HUNTING \\\\\


    @Override
    protected void collectDirsToLookForClassicPython(Set<File> dirsToLook)
    {
        dirsToLook.addAll(runPaths);

        String thePythonHome = System.getenv("PYTHONHOME");

        if (thePythonHome != null)
            dirsToLook.add(new File(thePythonHome));
        else
            dirsToLook.add(new File("/usr/bin"));
    }


    @Override
    protected Pattern getClassicPythonExeFileNamePattern()
    {
        return Pattern.compile("python(\\d(\\.\\d)?)?");
    }



    //// IRON PYTHONS HUNTING \\\\\


    @Override
    protected void collectDirsToLookForIronPython(Set<File> dirsToLook)
    {
        dirsToLook.addAll(runPaths);
        dirsToLook.add(new File("/usr/lib/ironpython"));
    }


    @Override
    protected Pattern getIronPythonExeFileNamePattern()
    {
        return Pattern.compile("ipy(32|64)?(\\.exe)?", Pattern.CASE_INSENSITIVE);
    }

}
