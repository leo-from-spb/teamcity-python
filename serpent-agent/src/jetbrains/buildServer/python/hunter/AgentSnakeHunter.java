package jetbrains.buildServer.python.hunter;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.SortedMap;


/**
 * @author Leonid.Bushuev
 */
public class AgentSnakeHunter
{

    private static final Logger log =
      Logger.getInstance("PythonHunter");

    private final @NotNull SnakeHunter myHunter;
    private final @NotNull BuildAgentConfiguration myBuildAgentConfiguration;


    public AgentSnakeHunter(@NotNull final SnakeHunter hunter,
                            @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher,
                            @NotNull final BuildAgentConfiguration buildAgentConfiguration)
    {
      myHunter = hunter;
      myBuildAgentConfiguration = buildAgentConfiguration;
      dispatcher.addListener(new Listener());
    }


    private class Listener extends AgentLifeCycleAdapter
    {

      @Override
      public void beforeAgentConfigurationLoaded(@NotNull final BuildAgent agent)
      {
          InstalledPythons pythons =
                  myHunter.hunt();
          SortedMap<String,String> selection =
                  myHunter.select(pythons);

          String summaryText =
                  SnakeHunterRunner.PrepareSummaryText(pythons, selection);
          log.info(summaryText);

          for(SortedMap.Entry<String,String> entry: selection.entrySet())
              myBuildAgentConfiguration.addConfigurationParameter(entry.getKey(), entry.getValue());
      }

    }

}
