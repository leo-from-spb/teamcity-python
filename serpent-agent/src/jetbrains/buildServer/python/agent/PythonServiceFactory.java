package jetbrains.buildServer.python.agent;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;


/**
 * @author Leonid Bushuev from JetBrains
 */
public class PythonServiceFactory implements CommandLineBuildServiceFactory
{

    @NotNull
    @Override
    public CommandLineBuildService createService()
    {
        return new PythonService();
    }


    private static final PythonRunnerInfo RUNNER_INFO = new PythonRunnerInfo();


    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo()
    {
        return RUNNER_INFO;
    }
}
