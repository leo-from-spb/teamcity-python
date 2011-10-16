<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="bean" class="jetbrains.buildServer.python.server.PythonPropertiesBean"/>


<div class="parameter">
  Python kind:
  <c:choose>
    <c:when test="${propertiesBean.properties['python-kind'] eq '*'}">
      any kind of python that found on an agent
    </c:when>
    <c:when test="${propertiesBean.properties['python-kind'] eq 'C'}">
      <strong>
        Classic Python
        <c:if test="${propertiesBean.properties['python-ver'] ne '*'}">
          ${propertiesBean.properties['python-ver']}.x
        </c:if>
      </strong>
    </c:when>
    <c:when test="${propertiesBean.properties['python-kind'] eq 'I'}">
      <strong>Iron Python</strong>
    </c:when>
    <c:when test="${propertiesBean.properties['python-kind'] eq 'J'}">
      <strong>Jython</strong>
    </c:when>
    <c:when test="${propertiesBean.properties['python-kind'] eq 'X'}">
      custom python (specified by the path of executable file)
    </c:when>
  </c:choose>
</div>

<div class="parameter">
  Bitness:
  <c:if test="${propertiesBean.properties['bitness'] ne '*'}">
    <strong>x${propertiesBean.properties['bitness']}</strong>
  </c:if>
  <c:if test="${propertiesBean.properties['bitness'] eq '*'}">
    any
  </c:if>
</div>

<div class="parameter">
  Executable path: <strong><c:out value="${propertiesBean.properties['python-exe']}"/></strong>
</div>

<props:viewWorkingDirectory />

<div class="parameter">
  <c:choose>
    <c:when test="${propertiesBean.properties['python-script-mode'] eq 'file'}">
      Python file: <strong>${propertiesBean.properties['python-script-file-name']}</strong>
    </c:when>
    <c:when test="${propertiesBean.properties['python-script-mode'] eq 'code'}">
      Python script:<br/>
      <div style="display: inline-block; border: 1px solid gray; margin: 1ex 1em 1ex 1em; padding: 4px; min-width: 40em;">
        <pre><c:out value="${propertiesBean.properties['python-script-code']}"/></pre>
      </div>
    </c:when>
  </c:choose>
</div>

<div class="parameter">
  <c:if test="${propertiesBean.properties['python-arguments'] ne ''}">
    Arguments:
    <pre>
      <c:out value="${propertiesBean.properties['python-arguments']}"/>
    </pre>
  </c:if>
</div>

<c:if test="${propertiesBean.properties['python-path'] ne ''}">
  <div class="parameter">
    Python modules path: <strong><c:out value="${propertiesBean.properties['python-path']}"/></strong>
  </div>
</c:if>


