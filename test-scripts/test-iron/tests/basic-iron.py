__author__ = 'Leonid'



def main():

    printOS()
    checkLocal()


def printOS():
    from System import IntPtr
    print ('bitness='+(IntPtr.Size*8).ToString())


def checkLocal():
    import justLocal
    str = justLocal.function()
    print(str)


main()