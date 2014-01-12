package jetbrains.buildServer.python.hunter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Python detector for MacOS.
 * @author Leonid Bushuev from JetBrains
 */
class SnakeHunterForMac extends SnakeHunter
{
    //// CLASSIC PYTHONS HUNTING \\\\\


    @Override
    protected void collectDirsToLookForClassicPython(Set<File> dirsToLook)
    {
        addSubdirs(dirsToLook, "/Library/Frameworks/Python.framework/Versions");
        addDeepSubdirs(dirsToLook, "/Library/Frameworks/Python.framework/Versions", "bin");
        addDeepSubdirs(dirsToLook, "/usr/local/Cellar/python", "bin");
        addDeepSubdirs(dirsToLook, "/usr/local/Cellar/python3", "bin");

        dirsToLook.addAll(runPaths);

        String thePythonHome = System.getenv("PYTHONHOME");

        if (thePythonHome != null)
            dirsToLook.add(new File(thePythonHome));
    }


    @Override
    protected Pattern getClassicPythonExeFileNamePattern()
    {
        return Pattern.compile("python3?");
    }



    //// IRON PYTHONS HUNTING \\\\\


    @Override
    protected void collectDirsToLookForIronPython(Set<File> dirsToLook)
    {
        dirsToLook.addAll(runPaths);
    }


    @Override
    protected Pattern getIronPythonExeFileNamePattern()
    {
        return Pattern.compile("ipy(32|64)?(\\.exe)?", Pattern.CASE_INSENSITIVE);
    }



    //// UTILS \\\\

    private static void addSubdirs(@NotNull Set<File> dirsToLook, @NotNull final String parentPath)
    {
        File parentDir = new File(parentPath);
        if (!parentDir.isDirectory())
            return;
        File[] subDirs = parentDir.listFiles();
        if (subDirs != null)
            for (File subDir: subDirs)
                if (subDir.isDirectory() && !subDir.getName().startsWith("."))
                    dirsToLook.add(subDir);
    }

    private static void addDeepSubdirs(@NotNull Set<File> dirsToLook, @NotNull final String parentPath, @NotNull final String relativePath)
    {
        File parentDir = new File(parentPath);
        if (!parentDir.isDirectory())
            return;
        File[] subDirs = parentDir.listFiles();
        if (subDirs != null)
            for (File subDir: subDirs)
                if (subDir.isDirectory())
                {
                    File deepDir = new File(subDir, relativePath);
                    if (deepDir.isDirectory())
                        dirsToLook.add(deepDir);
                }
    }

}
