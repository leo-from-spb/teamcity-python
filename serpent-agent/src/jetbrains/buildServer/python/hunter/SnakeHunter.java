package jetbrains.buildServer.python.hunter;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.python.common.PythonKind;
import jetbrains.buildServer.python.common.PythonVersion;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.StringUtil;
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
            List<String> pathEntries = StringUtil.split(pathVar, true, File.pathSeparatorChar);
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




    //// CLASSIC PYTHONS HUNTING \\\\\


    private void huntClassicPythons(@NotNull InstalledPythons pythons)
    {
        Set<File> dirsToLook = new LinkedHashSet<File>();
        collectDirsToLookForClassicPython(dirsToLook);

        Set<File> pretendents = new LinkedHashSet<File>(dirsToLook.size());
        lookExeFiles(dirsToLook, getClassicPythonExeFiles(), pretendents);

        examClassicPythons(pretendents, pythons);
    }

    protected abstract void collectDirsToLookForClassicPython(Set<File> dirsToLook);


    protected abstract String[] getClassicPythonExeFiles();


    private static final String ourClassicExamText =
            "import platform                         \n" +
            "(bitness,oss)=platform.architecture()   \n" +
            "print('bitness='+bitness)               \n" +
            "\u001A";

    private static final Pattern ourClassicPythonVersionPattern =
            Pattern.compile("Python\\s+((\\d+)\\.(\\d+))", Pattern.CASE_INSENSITIVE);

    private void examClassicPythons(Collection<File> pretendents, InstalledPythons pythons)
    {
        examPythons(pretendents, PythonKind.Classic, ourClassicExamText, ourClassicPythonVersionPattern, 2, 3, 1, pythons);
    }



    //// IRON PYTHONS HUNTING \\\\\


    private void huntIronPythons(@NotNull InstalledPythons pythons)
    {
        Set<File> dirsToLook = new LinkedHashSet<File>();
        collectDirsToLookForIronPython(dirsToLook);


        Set<File> pretendents = new LinkedHashSet<File>(2);
        lookExeFiles(dirsToLook, getIronPythonExeFiles(), pretendents);

        examIronPythons(pretendents, pythons);
    }


    protected abstract void collectDirsToLookForIronPython(Set<File> dirsToLook);


    protected abstract String[] getIronPythonExeFiles();


    private static final String ourIronExamText =
            "from System import IntPtr                     \n" +
            "print ('bitness='+(IntPtr.Size*8).ToString()) \n" +
            "\u001A\n";

    private static final Pattern ourIronPythonVersionPattern =
            Pattern.compile(
                    "(Iron)?Python(Context)?\\s+((\\d+)\\.(\\d+)(\\.\\d+)*(\\s*\\([\\d\\.]+\\))?)",
                    Pattern.CASE_INSENSITIVE);

    private void examIronPythons(Collection<File> pretendents, InstalledPythons pythons)
    {
        examPythons(pretendents, PythonKind.Iron, ourIronExamText, ourIronPythonVersionPattern, 4, 5, 3, pythons);
    }



    //// JYTHONS HUNTING \\\\\


    private void huntJythons(@NotNull InstalledPythons pythons)
    {
        // TODO implmement SnakeHunterForWindows.huntJythons
    }



    //// SELECT PREFERRED INSTALLATIOND \\\\


    public Map<String,String> choosePythonInstallations(final @NotNull InstalledPythons installedPythons)
    {
        // installed pythons are ordered - better pythons (more fat and delicious) are at the end of the list.
        // for each variable, we're selecting the last matched one.
        return null; // TODO implement
    }



    //// COMMON HUNTING ROUTINES \\\\


    protected void lookExeFiles(Collection<File> dirsToLook, String[] fileNames, Collection<File> foundFiles)
    {
        for (File dir: dirsToLook)
        {
            for (String fileName: fileNames)
            {
                File file = new File(dir, fileName);
                if (file.exists() && file.canExecute())
                    foundFiles.add(file);
            }
        }
    }


    protected ExecResult runExeFile(final @NotNull File exeFile,
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


    private int sequentialOrder = 0;

    private static final Pattern ourPythonBitnessPattern =
            Pattern.compile("bitness\\s*=\\s*(32|64)", Pattern.CASE_INSENSITIVE);

    protected void examPythons(final @NotNull Collection<File> pretendents,
                               final @NotNull PythonKind pythonKind,
                               final @NotNull String examText,
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
                        runExeFile(pretendent, Arrays.asList("-i"), examText);
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
