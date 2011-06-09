package jetbrains.buildServer.python.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProcessListener;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Leonid Bushuev from JetBrains
 */

public class PythonService extends BuildServiceAdapter
{


    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException
    {
        Map<String,String> runnerParameters = getRunnerParameters();
        for (Map.Entry<String,String> paramEntry: runnerParameters.entrySet())
            logMessage("runner parameter: " + paramEntry.getKey() + " -> " + paramEntry.getValue());

        Map<String,String> innerEnv = prepareEnv();

        List<String> arguments = new ArrayList<String>(4);
        arguments.add("-V");

        ProgramCommandLine pcl =
                new SimpleProgramCommandLine(innerEnv, getWorkingDirectory().getAbsolutePath(), "ipy", arguments);
        return pcl;
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


    @NotNull
    @Override
    public List<ProcessListener> getListeners()
    {
        return super.getListeners();
        // here I can provide a listener to parse SUT's output
    }


}
