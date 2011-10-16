package jetbrains.buildServer.python.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProcessListener;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static jetbrains.buildServer.python.common.PythonConstants.*;
import static jetbrains.buildServer.utils.YAFU.*;
import static jetbrains.buildServer.utils.YASU.*;

/**
 * @author Leonid Bushuev from JetBrains
 */

public class PythonService extends BuildServiceAdapter
{


    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine()
            throws RunBuildException
    {
        // just print the parameters
        printRunnerParameters();

        char pythonKind = Character.toUpperCase(firstChar(getParam("python-kind")));

        // resolve executable
        final String executable = ensureExecutable();

        // resolve main script or prepare it if needed
        String runFile = ensureRunFile();
        String pythonPathVar = getParam("python-path");

        // arguments for the python script
        List<String> pythonArguments = getArguments();

        Map<String,String> innerEnv = prepareEnv(pythonKind, getEnvironmentVariables(), executable, pythonPathVar);

        List<String> arguments = new ArrayList<String>(1 + pythonArguments.size());
        arguments.add(runFile);
        arguments.addAll(pythonArguments);

        return new SimpleProgramCommandLine(innerEnv, getWorkingDirectory().getAbsolutePath(), executable, arguments);
    }


    @NotNull
    String ensureExecutable()
            throws RunBuildException
    {
        String executable = getParam("python-exe");
        if (executable != null && executable.length() > 0)
            return executable;
        else
            return resolveAlternativeExecutable();
    }


    @NotNull
    private String resolveAlternativeExecutable()
            throws RunBuildException
    {
        final String pyKindStr = getParam("python.kind");
        char pyKind = firstChar(pyKindStr);

        switch (pyKind)
        {
            case 'C':
            case 'c':
                return "python";
            case 'I':
            case 'i':
                return "ipy";
            case 'J':
            case 'j':
                return "jython";
            case 'a':
            case 'A':
            case '*':
            case '\0':
                return tryToDetermineDefaultPython();
            case 'x':
            case 'X':
                throw new RunBuildException("Custom python is selected but the executable path is not specified.");
            default:
                throw new RunBuildException("Unknown python kind: " + pyKindStr);
        }
    }


    private String tryToDetermineDefaultPython()
    {
        return "python";  // TODO implement
    }


    private String ensureRunFile()
            throws RunBuildException
    {
        final String theScriptModeStr = getParam("python-script-mode");
        if (theScriptModeStr == null)
            throw new RunBuildException("Python run mode is not specified.");
        if (theScriptModeStr.equalsIgnoreCase("code"))
            return prepareRunFileFromGivenCode();
        if (theScriptModeStr.equalsIgnoreCase("file"))
            return provideRunFile();
        throw new RunBuildException("Python run mode ("+theScriptModeStr+") is unknown.");
    }


    @NotNull
    private String prepareRunFileFromGivenCode()
            throws RunBuildException
    {
        final String theCode = getParam("python-script-code");
        if (theCode == null)
            throw new RunBuildException("No python code to execute.");

        assert theCode.length() > 0;

        File file = getAbsoluteFile(CODE_SCRIPT_NAME);
        try
        {
            writeTextFile(file, theCode);
        }
        catch (IOException ioe)
        {
            throw new RunBuildException("Could not create file ("+CODE_SCRIPT_NAME+"). "+ioe.getMessage(), ioe);
        }

        if (file.length() == 0)
            throw new RunBuildException("Could not write to file: " + file);

        return CODE_SCRIPT_NAME;
    }


    @NotNull
    private String provideRunFile()
            throws RunBuildException
    {
        final String fileName = getParam("python-script-file-name");
        if (fileName == null)
            throw new RunBuildException("No python file name provided.");

        File file = getAbsoluteFile(fileName);
        if (!file.exists())
            throw new RunBuildException("Python file ("+fileName+") doesn't exist.");
        if (!file.canRead())
            throw new RunBuildException("Python file ("+fileName+") could not be read.");

        return fileName;
    }


    @NotNull
    private File getAbsoluteFile(final @NotNull String relName)
            throws RunBuildException
    {
        final File workingDirectory = getWorkingDirectory();
        return new File(workingDirectory, relName);
    }


    @NotNull
    private List<String> getArguments()
    {
        String text = getParam("python-arguments");
        if (text == null)
            return Collections.emptyList();
        text = text.trim();
        if (text.length() == 0)
            return Collections.emptyList();

        String[] lines = text.split("\n");
        List<String> args = new ArrayList<String>(lines.length);
        for (String line: lines)
        {
            line = line.trim();
            if (line.length() > 0)
                args.add(line);
        }

        return args;
    }


    private void printRunnerParameters()
    {
        /*
        Map<String,String> runnerParameters = getRunnerParameters();
        for (Map.Entry<String,String> paramEntry: runnerParameters.entrySet())
            logMessage("runner parameter: " + paramEntry.getKey() + " = " + paramEntry.getValue());
        */
    }


    private void logMessage(final String message)
    {
        getLogger().message(message);
    }


    static Map<String,String> prepareEnv(final char pythonKind,
                                         final @NotNull Map<String, String> environment,
                                         final @NotNull String executable,
                                         final @Nullable String pythonPathVar)
    {
        Map<String,String> innerEnv = new TreeMap<String,String>(environment);

        File exeFile = new File(executable);
        innerEnv.put("python", exeFile.getAbsolutePath());

        String dir = exeFile.getParent();

        String pathKey = adjustCase("PATH", innerEnv.keySet());
        String pathVar = innerEnv.get(pathKey);
        pathVar = dir + (pathVar != null ? File.pathSeparatorChar + pathVar : "");
        innerEnv.put(pathKey, pathVar);

        if (pythonPathVar != null && pythonPathVar.length() > 0)
        {
            String ppvName = (pythonKind == 'I') ? "IRONPYTHONPATH" : "PYTHONPATH";
            String ppvKey = adjustCase(ppvName, innerEnv.keySet());
            String ppvValue = innerEnv.get(ppvKey);
            ppvValue = pythonPathVar + (ppvValue != null ? File.pathSeparatorChar + ppvValue : "");
            innerEnv.put(ppvKey, ppvValue);
        }

        return innerEnv;
    }


    private String getParam(final @NotNull String paramName)
    {
        Map<String,String> parameters = getRunParameters();
        String value = trimAndNull(parameters.get(paramName));
        return value;
    }

    /**
     * Just for make it able to overried in tests.
     * @return just the result of {@link jetbrains.buildServer.agent.runner.BuildServiceAdapter#getRunnerParameters()}.
     */
    protected Map<String, String> getRunParameters()
    {
        return getRunnerParameters();
    }


    @NotNull
    @Override
    public List<ProcessListener> getListeners()
    {
        return super.getListeners();
        // here I can provide a listener to parse SUT's output
    }


}
