package jetbrains.buildServer.python.server;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Python runner - server side.
 * @author Leonid Bushuev from JetBrains
 */
public class PythonRunType extends RunType
{

  private final PluginDescriptor myDescriptor;



  public PythonRunType(@NotNull final RunTypeRegistry reg,
                       @NotNull final PluginDescriptor descriptor)
  {
    myDescriptor = descriptor;
    reg.registerRunType(this);
  }



  @NotNull
  @Override
  public String getType()
  {
    return "python";
  }

  @Override
  public String getDisplayName()
  {
    return "Python runner";
  }

  @Override
  public String getDescription()
  {
    return "Python runner";
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor()
  {
    return new PythonPropertiesProcessor();
  }

  @Override
  public String getEditRunnerParamsJspFilePath()
  {
    return myDescriptor.getPluginResourcesPath("editPythonParams.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath()
  {
    return myDescriptor.getPluginResourcesPath("viewPythonParams.jsp");
  }

  @Override
  public Map<String,String> getDefaultRunnerProperties()
  {
    Map<String,String> props = new HashMap<String,String>();
    props.put("python.kind", "*");
    props.put("python.ver", "*");
    props.put("bitness", "*");
    props.put("python.script.mode", "code");
    return props;
  }


}
