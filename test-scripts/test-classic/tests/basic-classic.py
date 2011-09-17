__author__ = 'Leonid'



def main():

    printOS()
    checkLocal()


def printOS():
    import platform
    (bitness,oss)=platform.architecture()
    print('oss='+oss)
    print('bitness='+bitness)


def checkLocal():
    import justLocal
    str = justLocal.function()
    print(str)


main()