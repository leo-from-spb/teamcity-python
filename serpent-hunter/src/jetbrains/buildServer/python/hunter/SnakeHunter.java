package jetbrains.buildServer.python.hunter;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.python.common.PythonKind;
import jetbrains.buildServer.python.common.PythonVersion;
import jetbrains.buildServer.util.Bitness;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jetbrains.buildServer.utils.YASU.*;


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
            List<String> pathEntries = split(pathVar, File.pathSeparatorChar, true, true);
            for (String pathEntry: pathEntries)
                runPaths.add(new File(pathEntry));
        }
    }


    public InstalledPythons hunt()
    {
        InstalledPythons pythons = new InstalledPythons();

        huntClassicPythons(pythons);
        huntIronPythons(pythons);
        huntJythons(pythons);

        return pythons;
    }


    public SortedMap<String,String> select(InstalledPythons allPythons)
    {
        SortedMap<String,String> selection = new TreeMap<String,String>();

        // there we're assuming that all pythons are already sorted
        // and better ones are at the bottom
        for (InstalledPython python: allPythons.getPythons())
        {
            String path = python.executable.getAbsolutePath();

            switch (python.kind)
            {
                case Classic:
                    selection.put("Python", path);
                    selection.put("Python." + python.version.major, path);
                    selection.put("Python.x" + python.bitness, path);
                    selection.put("Python." + python.version.major + ".x" + python.bitness, path);
                    break;

                case Iron:
                    selection.put("IronPython", path);
                    selection.put("IronPython.x" + python.bitness, path);
                    break;

                case Jython:
                    selection.put("Jython", path);
                    break;
            }
        }

        if (!selection.isEmpty())
            selection.put("AnyPython", selection.get(selection.lastKey()));

        return selection;
    }



    //// CLASSIC PYTHONS HUNTING \\\\\


    private void huntClassicPythons(@NotNull InstalledPythons pythons)
    {
        Set<File> dirsToLook = new LinkedHashSet<File>();
        collectDirsToLookForClassicPython(dirsToLook);

        Set<File> pretendents = new LinkedHashSet<File>(dirsToLook.size());
        lookExeFiles(dirsToLook, getClassicPythonExeFileNamePattern(), pretendents);

        examClassicPythons(pretendents, pythons);
    }

    protected abstract void collectDirsToLookForClassicPython(Set<File> dirsToLook);


    protected abstract Pattern getClassicPythonExeFileNamePattern();


    private static final String ourClassicExamText =
            "import platform                         \n" +
            "(bitness,oss)=platform.architecture()   \n" +
            "print('bitness='+bitness)               \n" +
            "\u001A";

    private static final Pattern ourClassicPythonVersionPattern =
            Pattern.compile("Python\\s+((\\d+)\\.(\\d+)(\\.\\d+)*)", Pattern.CASE_INSENSITIVE);

    private static final String[] ourClassicPythonCommandLineParams =
            new String[] { "-i" };

    private void examClassicPythons(Collection<File> pretendents, InstalledPythons pythons)
    {
        examPythons(pretendents,
                    PythonKind.Classic,
                    ourClassicPythonCommandLineParams,
                    ourClassicExamText,
                    ourClassicPythonVersionPattern,
                    2, 3, 1, pythons);
    }



    //// IRON PYTHONS HUNTING \\\\\


    private void huntIronPythons(@NotNull InstalledPythons pythons)
    {
        Set<File> dirsToLook = new LinkedHashSet<File>();
        collectDirsToLookForIronPython(dirsToLook);


        Set<File> pretendents = new LinkedHashSet<File>(2);
        lookExeFiles(dirsToLook, getIronPythonExeFileNamePattern(), pretendents);

        examIronPythons(pretendents, pythons);
    }


    protected abstract void collectDirsToLookForIronPython(Set<File> dirsToLook);


    protected abstract Pattern getIronPythonExeFileNamePattern();


    private static final String ourIronExamText = // must be in one line without line breaks, because is passed via a command-line parameter
            "import sys; print(sys.version); from System import IntPtr; print ('bitness='+(IntPtr.Size*8).ToString())";

    private static final String[] ourIronCommandLineParams =
            new String[] { "-c", '"' + ourIronExamText + '"' };

    private static final Pattern ourIronPythonVersionPattern =
            Pattern.compile(
                    "(Iron)?Python(Context)?\\s+((\\d+)\\.(\\d+)(\\.\\d+)*(\\s*\\([\\d\\.]+\\))?)",
                    Pattern.CASE_INSENSITIVE);

    private void examIronPythons(Collection<File> pretendents, InstalledPythons pythons)
    {
        examPythons(pretendents, PythonKind.Iron, ourIronCommandLineParams, null, ourIronPythonVersionPattern, 4, 5, 3, pythons);
    }



    //// JYTHONS HUNTING \\\\\


    private void huntJythons(@NotNull InstalledPythons pythons)
    {
        // TODO implmement SnakeHunterForWindows.huntJythons
    }



    //// COMMON HUNTING ROUTINES \\\\


    protected void lookExeFiles(Collection<File> dirsToLook, Pattern fileNamePattern, Collection<File> foundFiles)
    {
        for (File dir: dirsToLook)
        {
            File[] files = dir.listFiles();
            if (files == null || files.length == 0)
                continue;

            for (File file: files)
            {
                final Matcher m = fileNamePattern.matcher(file.getName());
                if (m.matches() && file.canExecute())
                    foundFiles.add(file);
            }
        }
    }


    protected ExecResult runExeFile(final @NotNull File exeFile,
                                    final @Nullable String[] args,
                                    final @Nullable String inputText)
            throws ExecutionException
    {
        GeneralCommandLine cmdLine = new GeneralCommandLine();
        cmdLine.setExePath(exeFile.getAbsolutePath());
        cmdLine.setPassParentEnvs(true);
        if (args != null)
            cmdLine.addParameters(Arrays.asList(args));

        final byte[] input = inputText != null
                ? inputText.getBytes()
                : new byte[0];

        try
        {
            ExecResult execResult;
            final Thread currentThread = Thread.currentThread();
            final String threadName = currentThread.getName();
            final String newThreadName = "Python Detector: waiting for: " + cmdLine.getCommandLineString();
            currentThread.setName(newThreadName);
            try
            {
                execResult = SimpleCommandLineProcessRunner.runCommand(cmdLine, input);
            }
            finally
            {
                currentThread.setName(threadName);
            }
            return execResult;
        }
        catch (Exception e)
        {
            throw new ExecutionException(e.getMessage(), e);
        }
    }


    private int sequentialOrder = 0;

    private static final Pattern ourPythonBitnessPattern =
            Pattern.compile("bitness\\s*=\\s*(32|64)", Pattern.CASE_INSENSITIVE);

    protected void examPythons(final @NotNull Collection<File> pretendents,
                               final @NotNull PythonKind pythonKind,
                               final @NotNull String[] commandLineParams,
                               final @Nullable String inputStreamText,
                               final @NotNull Pattern versionPattern,
                               final int versionPatternMajorGroup,
                               final int versionPatternMinorGroup,
                               final int versionPatternVersionStringGroup,
                               final @NotNull InstalledPythons pythons)
    {
        for (File pretendent: pretendents)
        {
            try
            {
                ExecResult result =
                        runExeFile(pretendent, commandLineParams, inputStreamText);
                String output = result.getStdout() + result.getStderr();

                Matcher m1 = versionPattern.matcher(output);
                boolean ok1 = m1.find();
                if (!ok1)
                    continue;

                String majorStr = m1.group(versionPatternMajorGroup);
                String minorStr = m1.group(versionPatternMinorGroup);
                int major = Integer.parseInt(majorStr);
                int minor = Integer.parseInt(minorStr);
                String versionStr = m1.group(versionPatternVersionStringGroup);
                PythonVersion version = new PythonVersion(major, minor, versionStr);

                Bitness bitness = null;
                Matcher m2 = ourPythonBitnessPattern.matcher(output);
                boolean ok2 = m2.find();
                if (ok2)
                    if (m2.group(1).equals("64"))
                        bitness = Bitness.BIT64;
                    else
                        bitness = Bitness.BIT32;

                int foundOrder = ++sequentialOrder;
                InstalledPython python = new InstalledPython(pythonKind, version, bitness, pretendent, foundOrder);
                pythons.addPython(python);
            }
            catch (ExecutionException ee)
            {
                System.err.println("Failed to try " + pretendent.getAbsolutePath() + ": " + ee.getMessage());
            }
        }
    }
}
