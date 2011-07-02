package jetbrains.buildServer.python.hunter;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.CommandLineExecutor;
import jetbrains.buildServer.ExecResult;
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

    private final WinRegistry winReg = new WinRegistry();


    @Override
    protected InstalledPythons hunt()
    {
        InstalledPythons pythons = new InstalledPythons();

        huntClassicPythons(pythons);
        huntIronPythons(pythons);
        huntJythons(pythons);

        return pythons;
    }


    //// CLASSIC PYTHONS HUNTING \\\\\


    static final String ourClassicPythonRegPath = "HKLM\\SOFTWARE\\Python";


    private void huntClassicPythons(@NotNull InstalledPythons pythons)
    {
        List<File> dirsFromRegistry = peekClassicPythonsFromWinRegistry();
        Set<File> dirsToLook = new LinkedHashSet<File>();
        dirsToLook.addAll(dirsFromRegistry);
        dirsToLook.addAll(runPaths);

        Set<File> pretendents = new LinkedHashSet<File>(dirsFromRegistry.size() + 2);
        lookFiles(dirsToLook, new String[] {"python.exe"}, pretendents);

        examClassicPythons(pretendents, pythons);
    }


    private static final Pattern ourClassicPythonRegKeyPattern =
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
                            Matcher m = ourClassicPythonRegKeyPattern.matcher(keyName);
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


    private static final Pattern ourClassicPythonVersionPattern =
            Pattern.compile("Python\\s+((\\d+)\\.(\\d+))", Pattern.CASE_INSENSITIVE);


    private void examClassicPythons(Collection<File> pretendents, InstalledPythons pythons)
    {
        for (File pretendent: pretendents)
        {
            try
            {
                ExecResult result =
                        runExeFile(pretendent, "-V");
                String output = result.getStdout() + result.getStderr();
                Matcher m = ourClassicPythonVersionPattern.matcher(output);
                if (m.find())
                {
                    String majorStr = m.group(2);
                    String minorStr = m.group(3);
                    int major = Integer.parseInt(majorStr);
                    int minor = Integer.parseInt(minorStr);
                    PythonVersion version = new PythonVersion(major, minor, m.group(1));
                    Bitness bitness = examClassicPythonsBitness(pretendent, version);
                    InstalledPython python = new InstalledPython(PythonKind.Classic, version, bitness, pretendent);
                    pythons.addPython(python);
                }
            }
            catch (ExecutionException ee)
            {
                System.err.println("Failed to try " + pretendent.getAbsolutePath() + ": " + ee.getMessage());
            }
        }
    }

    private Bitness examClassicPythonsBitness(File pythonExeFile, PythonVersion version)
    {
        // TODO implement examClassicPythonsBitness()
        return null;
    }



    //// IRON PYTHONS HUNTING \\\\\


    private void huntIronPythons(@NotNull InstalledPythons pythons)
    {
        List<File> dirsToLook = new ArrayList<File>(this.runPaths);
        dirsToLook.addAll(runPaths);

        Set<File> pretendents = new LinkedHashSet<File>(2);
        lookFiles(dirsToLook, new String[] {"ipy64.exe", "ipy.exe", "ipy32.exe"}, pretendents);

        examIronPythons(pretendents, pythons);
    }


    private static final Pattern ourIronPythonVersionPattern =
            Pattern.compile("(Iron)?Python(Context)?\\s+((\\d+)\\.(\\d+)(\\.\\d+)*)", Pattern.CASE_INSENSITIVE);


    private void examIronPythons(Collection<File> pretendents, InstalledPythons pythons)
    {
        for (File pretendent: pretendents)
        {
            try
            {
                ExecResult result =
                        runExeFile(pretendent, "-V");
                String output = result.getStdout() + result.getStderr();
                Matcher m = ourIronPythonVersionPattern.matcher(output);
                if (m.find())
                {
                    String majorStr = m.group(4);
                    String minorStr = m.group(5);
                    int major = Integer.parseInt(majorStr);
                    int minor = Integer.parseInt(minorStr);
                    PythonVersion version = new PythonVersion(major, minor, m.group(3));
                    Bitness bitness = examIronPythonsBitness(pretendent, version);
                    InstalledPython python = new InstalledPython(PythonKind.Iron, version, bitness, pretendent);
                    pythons.addPython(python);
                }
            }
            catch (ExecutionException ee)
            {
                System.err.println("Failed to try " + pretendent.getAbsolutePath() + ": " + ee.getMessage());
            }
        }
    }

    private Bitness examIronPythonsBitness(File pythonExe, PythonVersion version)
    {
        // TODO implmement SnakeHunterForWindows.examIronPythonsBitness
        return null;
    }


    //// JYTHONS HUNTING \\\\\


    private void huntJythons(@NotNull InstalledPythons pythons)
    {
        // TODO implmement SnakeHunterForWindows.huntJythons
    }



    //// COMMON HUNTING ROUTINES \\\\


    private void lookFiles(Collection<File> dirsToLook, String[] fileNames, Collection<File> foundFiles)
    {
        for (File dir: dirsToLook)
        {
            for (String exeName: fileNames)
            {
                File exeFile = new File(dir, exeName);
                if (exeFile.exists() && exeFile.canExecute())
                {
                    foundFiles.add(exeFile);
                }
            }
        }
    }


    private ExecResult runExeFile(File exeFile, String... args)
            throws ExecutionException
    {
        GeneralCommandLine cmdLine = new GeneralCommandLine();
        cmdLine.setExePath(exeFile.getAbsolutePath());
        cmdLine.setPassParentEnvs(true);
        cmdLine.addParameters(args);

        CommandLineExecutor executor = new CommandLineExecutor(cmdLine);
        ExecResult execResult = executor.runProcess();
        return execResult;
    }




}
