import glob, argparse, os

fileName="pom.xml"
mainVer = 1
minorVer = 3
patchVer = 0
tag="-SNAPSHOT"
ver=str(mainVer)+"."+str(minorVer)+"."+str(patchVer)+tag
# 0 Mayor, 1 minor, 2 patch, 3 release
nextVersion=1

def findPoms():
    path = './'
    files = [f for f in glob.glob(path + "**/"+fileName, recursive=True)]
    #print("found "+str(len(files))+" poms")
    return files;

def toNextVersion():
    global ver
    if nextVersion==0: # mayor change
        ver = str(mainVer+1)+"."+str(minorVer)+"."+str(patchVer)+tag
    elif nextVersion==2: # patch
        ver = str(mainVer)+"."+str(minorVer)+"."+str(patchVer+1)+tag
    elif nextVersion==3: # release (remove snapshot)
        ver = str(mainVer)+"."+str(minorVer)+"."+str(patchVer)
    else: # minor change (default)
        ver = str(mainVer)+"."+str(minorVer+1)+"."+str(patchVer)+tag

def currentVersion(line):
    verPtr=line.find("<!--VerNo-->")
    aux=line.replace("<!--VerNo-->","")
    ver=aux[verPtr:aux.find("</version>")]
    mainVer=int(ver.split('.')[0])
    minorVer=int(ver.split('.')[1])
    if tag in ver.split('.')[2]:
        patchVer=int(ver.split('.')[2].split("-")[0])
    else:
        patchVer=int(ver.split()[2])

def nextPom(pom):
    #print("generating update for pom: "+pom)
    fh = open(pom)
    lines=list()

    while True:
        # read line
        line = fh.readline()

        # check if line is not empty
        if not line:
            break

        if "<!--VerNo-->" in line:
            #print("got version entry: "+line)
            currentVersion(line)
            toNextVersion()
            line="\t<version><!--VerNo-->"+ver+"</version>\n"
            #print("new version entry: "+line)

        lines.append(line)
    fh.close()
    return lines
def updatePom(pom, lines):
    #print("overwriting pom: "+pom)
    with open(pom, 'w') as f:
        for line in lines:
            f.write("%s" % line)

branch = os.popen("git branch").read()[1]
if branch == 'master':
    global nextVersion
    nextVersion = 3 # set to release

parser = argparse.ArgumentParser(description='Updates the pom version by increasing the snapshot version or removing the snapshot tag of all poms below the cwd (recursively).')

parser.add_argument('--versionType',type=str, action='store_const', const='<minor>', help="the pom target version, if currently the pom is a release: then it can be <major>, <minor>, or <patch> snapshot; otherwise <release>")
# if current version is a release and <minor> is selected then only removes the snapshot.
args = parser.parse_args()
print(args.accumulate(args.integers))


poms = findPoms()
for pom in poms:
    updatePom(pom, nextPom(pom))
print(ver)