export LD_LIBRARY_PATH=`pwd`/lib/openni_linux_x86:`pwd`/lib/nite_linux_x86

CLASS_PATH="lib/openni_linux_x86/org.openni.jar:lib/nite_linux_x86/com.primesense.nite.jar:lib/geomlib.jar:.";
NATIVE_PATH="`pwd`/lib/openni_linux_x86/:`pwd`/lib/nite_linux_x86/"

java -cp $CLASS_PATH -Djava.library.path=$NATIVE_PATH UserViewerApplication