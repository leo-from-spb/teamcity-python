import sys

def main():    
    if len(sys.argv) < 2:
        print("No parameters!")
        exit(-1)

    k = 0
    for i in range(1,len(sys.argv)):
        print("Param %d: %s" % (i, sys.argv[i]))


main()
