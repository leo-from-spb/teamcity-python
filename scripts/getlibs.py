import os
import urllib.request

TeamCityLibPath = "lib/teamcity"

TeamCityLibDirectories = [ "runtime", "src", "javadoc" ]

TeamCityLibFiles = [ "README.txt",
                     "common-api.jar",
                     "server-api.jar",
                     "agent-api.jar",
                     "runtime/runtime-util.jar",
                     "runtime/serviceMessages.jar",
                     "src/openApi-source.jar",
                     "src/serviceMessages-src.jar",
                     "javadoc/openApi-help.jar" ]


def Main():

    print("Downloading TeamCity libraries\n")

    ensureDir(TeamCityLibPath)
    for dname in TeamCityLibDirectories:
        ensureDir(TeamCityLibPath + '/' + dname)

    for fname in TeamCityLibFiles:
        processOneLibFile(fname)

    print("Ok.")


def processOneLibFile(name):

    print("\t" + name)
    content = downloadTeamCityLibFile(name)
    ## print(name + " - downloaded.")
    writeBinFile(TeamCityLibPath + '/' + name, content)
    ## print(name + " - writed.")


def downloadTeamCityLibFile(name):

    url = r"http://buildserver/guestAuth/repository/download/bt457/latest.lastPinned/devPackage/" + name
    response = urllib.request.urlopen(url)
    content = response.read()
    return content


def writeBinFile(fname, content):

    f = open(fname, 'wb')
    f.write(content)
    f.close()


def ensureDir(name):

    os.makedirs(name, exist_ok=True)





#############################################################################

Main()
