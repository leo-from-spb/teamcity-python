__author__ = 'Leonid.Bushuev'

import time
import sys
import subprocess
import os
import shutil


# TeamCity location

tcHome = sys.argv[1]
tcData = tcHome + "/DATA"
tcBin = tcHome + "/bin"

print("Redeploying to a test server: %s"%tcHome)


# OS tricks

cmdExe = "C:/windows/system32/cmd.exe"
shellPrefix = "/C "


# stopping the currently running TC

p = subprocess.Popen(shellPrefix+'runAll.bat stop', executable=cmdExe, cwd=tcBin)
p.wait()
time.sleep(3)


# removing plugin

deployedZipPath = tcData + "/plugins/python.zip"
deployedUnpackedPath = tcData + "/plugins/.unpacked/python"

if os.path.exists(deployedZipPath):
    os.remove(deployedZipPath)

if os.path.exists(deployedUnpackedPath):
    shutil.rmtree(deployedUnpackedPath)

# copy new build of plugin

newZipPath = "out/artifacts/python.zip"

shutil.copy(newZipPath, deployedZipPath)


# starting TC

p = subprocess.Popen(shellPrefix+'runAll.bat start', executable=cmdExe, cwd=tcBin)
p.wait()








  