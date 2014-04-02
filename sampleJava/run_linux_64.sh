export LD_LIBRARY_PATH=`pwd`/lib/openni_linux_x64:`pwd`/lib/nite_linux_x64

CLASS_PATH="lib/openni_linux_x64/org.openni.jar:lib/nite_linux_x64/com.primesense.nite.jar:lib/geomlib.jar:.";
NATIVE_PATH="`pwd`/lib/openni_linux_x64/:`pwd`/lib/nite_linux_x64/"

java -cp $CLASS_PATH -Djava.library.path=$NATIVE_PATH UserViewerApplication