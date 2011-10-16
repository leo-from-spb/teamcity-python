package jetbrains.buildServer.python.agent;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author Leonid Bushuev from JetBrains
 */

public class PythonRunnerInfo implements AgentBuildRunnerInfo
{

    @NotNull
    @Override
    public String getType()
    {
        return "python";
    }


    @Override
    public boolean canRun(@NotNull BuildAgentConfiguration agentConfiguration)
    {
        return true;
    }
}
