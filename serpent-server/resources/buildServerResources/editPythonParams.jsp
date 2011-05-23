<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>

<jsp:useBean id="bean" class="jetbrains.buildServer.python.PythonPropertiesBean"/>


<tr>
    <th>
        <label for="python.kind">Python kind</label>
    </th>
    <td>
        <props:selectProperty name="python.kind">
            <props:option value="*">any</props:option>
            <props:option value="C">Classic Python</props:option>
            <props:option value="I">Iron Python</props:option>
            <props:option value="J">Jython</props:option>
        </props:selectProperty>
        <props:selectProperty name="python.ver">
            <props:option value="*">any</props:option>
            <props:option value="2">2.x</props:option>
            <props:option value="3">3.x</props:option>
        </props:selectProperty>
    </td>
</tr>


<tr>
    <th>
        <label for="bitness">Bitness</label>
    </th>
    <td>
        <props:selectProperty name="bitness">
            <props:option value="32">x32</props:option>
            <props:option value="64">x64</props:option>
        </props:selectProperty>
    </td>
</tr>


<forms:workingDirectory/>


<tr>
    <th>
        <label>File to run</label>
    </th>
    <td>
        <props:textProperty name="python.file" className="longField"/>
    </td>
</tr>


