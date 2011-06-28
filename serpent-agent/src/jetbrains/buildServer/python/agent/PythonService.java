package jetbrains.buildServer.python.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProcessListener;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import org.jetbrains.annotations.NotNull;

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

        // resolve executable
        final String executable = ensureExecutable();

        // resolve main script or prepare it if needed
        String runFile = ensureRunFile();

        Map<String,String> innerEnv = prepareEnv();

        List<String> arguments = new ArrayList<String>(4);
        arguments.add(runFile);

        ProgramCommandLine pcl =
                new SimpleProgramCommandLine(innerEnv, getWorkingDirectory().getAbsolutePath(), executable, arguments);
        return pcl;
    }


    @NotNull
    private String ensureExecutable()
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
            case '*':
            case '\0':
                return tryToDetermineDefaultPython();
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
        final String theScriptModeStr = getParam("python.script.mode");
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
        final String theCode = getParam("python.script.code");
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
        final String fileName = getParam("python.script.file.name");
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


    private void printRunnerParameters()
    {
        Map<String,String> runnerParameters = getRunnerParameters();
        for (Map.Entry<String,String> paramEntry: runnerParameters.entrySet())
            logMessage("runner parameter: " + paramEntry.getKey() + " -> " + paramEntry.getValue());
    }


    private void logMessage(final String message)
    {
        getLogger().message(message);
    }


    private Map<String,String> prepareEnv()
    {
        Map<String,String> innerEnv = new TreeMap<String,String>( getEnvironmentVariables() );

        // TODO include new environment variables

        return innerEnv;
    }


    private String getParam(final @NotNull String paramName)
    {
        Map<String,String> runnerParameters = getRunnerParameters();
        String value = trimAndNull(runnerParameters.get(paramName));
        return value;
    }


    @NotNull
    @Override
    public List<ProcessListener> getListeners()
    {
        return super.getListeners();
        // here I can provide a listener to parse SUT's output
    }


}
