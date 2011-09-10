import os
import urllib.request


Libs = \
    {
        "junit-4.8.2.jar" : "https://github.com/downloads/KentBeck/junit/junit-4.8.2.jar"
    }

LibDir = "./lib"



def Main():

    print("Downloading libraries\n")

    ensureDir(LibDir)
    for lib in Libs.items():
        processOneLibFile(lib)

    print("Ok.")


def processOneLibFile(lib):

    (name, url) = lib

    print("\t" + name)

    fileName = LibDir + '/' + name
    if os.path.exists(fileName):
        existentSize = os.path.getsize(fileName)
        print("\t\tskipped; current file size is %d bytes" % existentSize)
        return

    content = downloadTeamCityLibFile(url)
    writeBinFile(fileName, content)
    resultSize = os.path.getsize(fileName)
    print("\t\tdownloaded %d bytes" % resultSize)


def downloadTeamCityLibFile(url):

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
