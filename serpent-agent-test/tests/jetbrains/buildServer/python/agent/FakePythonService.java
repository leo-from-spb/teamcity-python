package jetbrains.buildServer.python.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Leonid.Bushuev
 */
public class FakePythonService extends PythonService
{

    final @NotNull Map<String,String> myRunParameters =
            new TreeMap<String,String>();


    @NotNull @Override
    public Map<String, String> getRunParameters()
    {
        return myRunParameters;
    }


    void addParameter(@NotNull String name, @Nullable String value)
    {
        myRunParameters.put(name, value);
    }


}
