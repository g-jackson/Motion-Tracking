export LD_LIBRARY_PATH=`pwd`/lib/openni_linux_x64:`pwd`/lib/nite_linux_x64
java -cp "lib/openni_linux_x64/org.openni.jar:lib/nite_linux_x64/com.primesense.nite.jar:." -Djava.library.path="`pwd`/lib/openni_linux_x64/:`pwd`/lib/nite_linux_x64/" UserViewerApplication

#java -cp "lib/openni_linux_x64/org.openni.jar:lib/nite_linux_x64/com.primesense.nite.jar"  UserViewerApplication
#