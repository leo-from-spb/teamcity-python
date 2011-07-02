package jetbrains.buildServer.python.hunter;

import jetbrains.buildServer.python.common.PythonKind;
import jetbrains.buildServer.python.common.PythonVersion;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.utils.WinRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Python detector for Windows.
 * @author Leonid Bushuev from JetBrains
 */
public class SnakeHunterForWindows extends SnakeHunter
{

    static final String ourClassicPythonRegPath = "HKLM\\SOFTWARE\\Python";

    private final WinRegistry winReg = new WinRegistry();


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
        List<File> dirsFromRegistry = peekClassicPythonsFromWinRegistry();
        Set<File> dirsToLook = new LinkedHashSet<File>();
        dirsToLook.addAll(dirsFromRegistry);
        dirsToLook.addAll(runPaths);

        lookFiles(dirsToLook, new String[] {"python.exe"}, PythonKind.Classic, pythons);
    }


    private static final Pattern ourClassicPythonRegKeyPatthern =
            Pattern.compile("HK[A-Z_]*\\\\SOFTWARE\\\\Python\\\\PythonCore\\\\.*\\\\InstallPath", Pattern.CASE_INSENSITIVE);

    private List<File> peekClassicPythonsFromWinRegistry()
    {
        final List<File> dirsWithPythons = new ArrayList<File>(4);

        for (Bitness bitness: Arrays.asList((Bitness)null, Bitness.BIT64, Bitness.BIT32))
        {
            WinRegistry.DumpConsumer consumer =
                    new WinRegistry.DumpConsumer()
                    {
                        boolean keyToProcess = false;

                        @Override
                        public void handleKey(@NotNull String keyName)
                        {
                            Matcher m = ourClassicPythonRegKeyPatthern.matcher(keyName);
                            keyToProcess = m.matches();
                        }

                        @Override
                        public void handleValue(@NotNull String entryName, @NotNull String entryValue)
                        {
                            if (keyToProcess && entryName.equals(""))
                            {
                                File file = new File(entryValue);
                                dirsWithPythons.add(file);
                            }
                        }
                    };

            try
            {
                winReg.dump(ourClassicPythonRegPath, bitness, consumer);
            }
            catch (WinRegistry.Error wre)
            {
                String bitnessStr = bitness == null ? "default" : Byte.toString(bitness.value);
                System.err.println("WinRegistry ("+ ourClassicPythonRegPath +"), bitness: " + bitnessStr + ":\n" + wre.getMessage());
            }
        }

        return dirsWithPythons;
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


    private void lookFiles(Collection<File> dirsToLook, String[] exeNames, PythonKind kind, InstalledPythons foundPythons)
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
