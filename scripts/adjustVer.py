__author__ = 'Leonid'


import sys
import os
import subprocess
import re


def Main():

    versionTemplate, fileNames = HandleArguments()
    revision = GetRevision()
    build = GetBuildNrFromTeamCityVar()
    versionStr = HandleVersion(versionTemplate, revision, build)
    PatchPluginFiles(fileNames, versionStr)


def HandleArguments():

    if len(sys.argv) < 2:
        print ("Usage: \n")
        print ("%s version-template file-1 file-2 ...\n" % sys.argv[0])
        exit(-1)

    ### Check arguments

    versionTemplate = sys.argv[1]
    names = sys.argv[2:]

    for name in names:

        if not os.access(name, os.F_OK):
            print ("File: \"%s\" doesn't exist.\n" % name)
            exit(-2)

        if not os.access(name, os.R_OK):
            print ("Could not access file: \"%s\"\n" % name)
            exit(-2)

    return versionTemplate, names


def GetRevision():

    args = [ 'hg', 'tip' ]
    output = RunProcess(args)
    revPattern = re.compile("""changeset:\s*(\d+):[a-fA-F0-9]+""")
    found = revPattern.search(str(output))
    revStr = found.group(1)
    rev = int(revStr)
    return rev


def RunProcess(args):

    popen = subprocess.Popen(args, stdout=subprocess.PIPE)
    popen.wait()
    output = popen.communicate()[0]
    return output


def GetBuildNrFromTeamCityVar():

    underTeamCity = os.environ.get('TEAMCITY_VERSION',"") != ""

    if underTeamCity:
        buildNr = str(os.environ.get('BUILD_NUMBER','0'))
    else:
        buildNr = 0

    return buildNr


def HandleVersion(template, rev, build):

    version = template
    version = re.sub(r'R#', str(rev), version)
    version = re.sub(r'B#', str(build), version)

    print ("Version=%s\n" % version)

    if build > 0: ## under TeamCity
        print ("##teamcity[buildNumber '%s']\n" % version)

    return version


def PatchPluginFiles(fileNames, versionStr):

    for fileName in fileNames:
        origFileName = fileName + '.orig'
        RenameFile(fileName, origFileName)
        PatchPluginFile(origFileName, fileName, versionStr)


def RenameFile(src, dst):

    if os.access(dst, os.F_OK):
        os.remove(dst)

    os.rename(src, dst)

    if not os.access(dst, os.F_OK):
        print ("Could not rename %s to %s\n" % (src,dst))
        exit(-4)


def PatchPluginFile(srcFileName, dstFileName, version):

    pattern = r'.*<version>\s*(\S+)\s*</version>.*'
    grp = 1
    PatchTextFile(srcFileName, dstFileName, pattern, grp, version)


def PatchTextFile(srcFileName, dstFileName, pattern, grp, subst):

    compiledPattern = re.compile(pattern)

    etalon_f = open(srcFileName, "r", 1)
    patched_f = open(dstFileName, "w")
    changes = 0
    for line in etalon_f:
        matched = compiledPattern.match(line)
        if matched:
           patched_line = line[:matched.start(grp)] + subst + line[matched.end(grp):]
           patched_f.write(patched_line)
           changes += 1
        else:
           patched_f.write(line)
    patched_f.close()
    etalon_f.close()

    changesStr = ("%d changes" % changes) if changes >= 2 else "1 change" if changes == 1 else "no changes"
    print ("\t%s patched (%s)\n" % (dstFileName, changesStr))



#############################################################################

Main()

