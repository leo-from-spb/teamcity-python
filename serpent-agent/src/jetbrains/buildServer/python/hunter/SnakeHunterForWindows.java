package jetbrains.buildServer.python.hunter;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.CommandLineExecutor;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.python.common.PythonKind;
import jetbrains.buildServer.python.common.PythonVersion;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.utils.WinRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.plugin.javascript.navig.Array;

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
                        runExeFile(pretendent, Arrays.asList("-V"), null);
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


    private static final String ourIronExamText =
            "from System import IntPtr                     \n" +
            "print ('bitness='+(IntPtr.Size*8).ToString()) \n" +
            "\u001A";

    private static final Pattern ourIronPythonVersionPattern =
            Pattern.compile(
                    "(Iron)?Python(Context)?\\s+((\\d+)\\.(\\d+)(\\.\\d+)*(\\s*\\([\\d\\.]+\\))?)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern ourIronPythonBitnessPattern =
            Pattern.compile("bitness\\s*=\\s*(32|64)", Pattern.CASE_INSENSITIVE);


    private void examIronPythons(Collection<File> pretendents, InstalledPythons pythons)
    {
        for (File pretendent: pretendents)
        {
            try
            {
                ExecResult result =
                        runExeFile(pretendent, Arrays.asList("-i"), ourIronExamText);
                String output = result.getStdout() + result.getStderr();

                Matcher m1 = ourIronPythonVersionPattern.matcher(output);
                boolean ok1 = m1.find();
                if (!ok1)
                    continue;

                String majorStr = m1.group(4);
                String minorStr = m1.group(5);
                int major = Integer.parseInt(majorStr);
                int minor = Integer.parseInt(minorStr);
                String versionStr = m1.group(3);
                PythonVersion version = new PythonVersion(major, minor, versionStr);

                Bitness bitness = null;
                Matcher m2 = ourIronPythonBitnessPattern.matcher(output);
                boolean ok2 = m2.find();
                if (ok2)
                    if (m2.group(1).equals("64"))
                        bitness = Bitness.BIT64;
                    else
                        bitness = Bitness.BIT32;

                InstalledPython python = new InstalledPython(PythonKind.Iron, version, bitness, pretendent);
                pythons.addPython(python);
            }
            catch (ExecutionException ee)
            {
                System.err.println("Failed to try " + pretendent.getAbsolutePath() + ": " + ee.getMessage());
            }
        }
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


    private ExecResult runExeFile(final @NotNull File exeFile,
                                  final @Nullable List<String> args,
                                  final @Nullable String inputText)
            throws ExecutionException
    {
        GeneralCommandLine cmdLine = new GeneralCommandLine();
        cmdLine.setExePath(exeFile.getAbsolutePath());
        cmdLine.setPassParentEnvs(true);
        if (args != null)
            cmdLine.addParameters(args);

        byte[] input = null;
        if (inputText != null)
            input = inputText.getBytes();

        try
        {
            ExecResult execResult
                = SimpleCommandLineProcessRunner.runCommand(cmdLine, input);
            return execResult;
        }
        catch (Exception e)
        {
            throw new ExecutionException(e.getMessage(), e);
        }
    }




}
