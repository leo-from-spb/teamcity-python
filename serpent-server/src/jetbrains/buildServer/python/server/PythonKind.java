package jetbrains.buildServer.python.server;

import java.util.Collections;
import java.util.Map;


/**
 * Kind of python.
 * @author Leonid Bushuev from JetBrains
 */
public enum PythonKind
{

    /**
     * Native python.
     */
    Classic ('C', "2", "2.x", "3", "3.x"),

    /**
     * Dot.Net port.
     */
    Iron ('I', "2", "2.x"),

    /**
     * Java port.
     */
    Jython ('J', "2", "2.5.x");


    /**
     * Char-code of the kind.
     */
    public final char code;

    /**
     * Codes-descriptions of supported versions.
     */
    public final Map<String,String> versions;


    PythonKind(char code, String verCode1, String verDescr1)
    {
        this.code = code;
        this.versions = Collections.singletonMap(verCode1, verDescr1);
    }

    PythonKind(char code, String verCode1, String verDescr1, String verCode2, String verDescr2)
    {
        this.code = code;
        this.versions = Collections.singletonMap(verCode1, verDescr1);
    }


    public static PythonKind forCode(char c)
    {
        switch (Character.toUpperCase(c))
        {
            case 'C': return Classic;
            case 'I': return Iron;
            case 'J': return Jython;
            default:  throw new IllegalArgumentException("Unknown python kind code: " + c);
        }
    }

    public static PythonKind forCode(String str)
    {
        int n = str != null ? str.length() : 0;
        switch (n)
        {
            case 0:  return null;
            case 1:  return forCode(str.charAt(0));
            default: throw new IllegalArgumentException("Unknown python kind code: " + str);
        }
    }

}
