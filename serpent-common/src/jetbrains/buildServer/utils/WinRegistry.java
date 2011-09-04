package jetbrains.buildServer.utils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.CommandLineExecutor;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.util.Bitness;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jetbrains.buildServer.utils.YASU.*;


/**
 * Simple windows registry accessor (read only).
 *
 * <p>
 *     Uses the <b>reg.exe</b> utility to read Windows registry.
 *     Administrative rights are not required.
 * </p>
 *
 * <p>
 *     The class is designed as instantiable just in order to
 *     allow unittest other class and replace this class with mock.
 *     Really, it doesn't hold any state.
 * </p>
 *
 * @author Leonid Bushuev from JetBrains
 */
public final class WinRegistry
{

    private static final File ourRegToolExecutableName = prepareRegToolExecutableName();


    public interface DumpConsumer
    {
        void handleKey (@NotNull String keyName);
        void handleValue (@NotNull String entryName, @NotNull String entryValue);
    }



    public void dump (final @NotNull String keyName, final Bitness bitness, final @NotNull DumpConsumer consumer)
            throws Error
    {
        List<String> args = new ArrayList<String>(4);
        args.add("query");
        args.add(keyName);
        args.add("/s");

        if (bitness != null) args.add("/reg:" + bitness.value);

        performRegQuery(args, consumer);
    }


    private void performRegQuery (final @NotNull List<String> args, final @NotNull DumpConsumer consumer)
            throws Error
    {
        GeneralCommandLine cmdLine = new GeneralCommandLine();
        cmdLine.setExePath(ourRegToolExecutableName.getAbsolutePath());
        cmdLine.setPassParentEnvs(true);
        cmdLine.addParameters(args);

        CommandLineExecutor executor = new CommandLineExecutor(cmdLine);
        ExecResult execResult;
        try
        {
            execResult = executor.runProcess();
        }
        catch (ExecutionException e)
        {
            throw new Error("Could not execute reg.exe: " + e.getMessage(), e);
        }

        String errs = trimAndNull(execResult.getStderr());
        if (errs != null)
        {
            if (errs.startsWith("ERROR:") && errs.contains("unable to find"))
                return; // it's not an error - just no such key in the registry.
            throw new Error("Failed reg.exe run: \n" + errs);
        }

        String output = trimRightAndNull(execResult.getStdout());
        if (output == null)
            throw new Error("Failed reg.exe run: empty output");
        if (output.startsWith("ERROR:") && output.contains("unable to find"))
            return; // it's not an error - just no such key in the registry.

        parseRegOutput(output, consumer);
    }


    private void parseRegOutput(final @NotNull String output, final @NotNull DumpConsumer consumer)
    {
        int n = output.length();
        int k1 = 0;
        while (k1 < n)
        {
            int k2 = output.indexOf('\n', k1);
            if (k2 < 0) k2 = n;
            String line = output.substring(k1, k2);
            k1 = k2 + 1;
            line = trimRightAndNull(line);
            if (line == null)
                continue;

            parseLine(line, consumer);
        }
    }


    private void parseLine(final @NotNull String line, final @NotNull DumpConsumer consumer)
    {
        assert line.length() > 0;

        char c1 = line.charAt(0);
        switch (c1)
        {
            case 'H':
                consumer.handleKey(line);
                break;

            case ' ':
            case '\t':
                Matcher m = ourValuePattern.matcher(line);
                if (m.matches())
                {
                    String name = m.group(1);
                    String value = m.group(4);

                    if (name.equalsIgnoreCase("(Default)"))
                        name = "";

                    consumer.handleValue(name, value);
                }
                break;
        }
    }

    private static final Pattern ourValuePattern =
            Pattern.compile("^\\s+(\\S(.*\\S)?)\\s+(REG_[A-Z_]+)\\s+(\\S(.*\\S)?)\\s*$");



    private static File prepareRegToolExecutableName()
    {
        // windows directory
        String winDirPath = System.getenv("SystemRoot");
        if (winDirPath == null)
            winDirPath = System.getenv("windir");
        if (winDirPath == null)
            winDirPath = "C:\\Windows";
        File winDir = new File(winDirPath);
        if (!winDir.exists())
            throw new IllegalStateException("Could not determine Windows directory");

        // windows system directory
        File winSysDir = new File(winDir, "system32");
        if (!winSysDir.exists())
            throw new IllegalStateException("Could not determine Windows system directory");

        // reg util
        File regUtil = new File(winSysDir, "reg.exe");
        if (!regUtil.exists())
            throw new IllegalStateException("Could not found the 'reg.exe' utility.");

        // ok
        return regUtil;
    }



    public static class Error extends RuntimeException
    {
        public Error(String message)
        {
            super(message);
        }

        public Error(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

}
