echo 'get libs first'
exit

CLASS_PATH="lib/openni_linux_x86/org.openni.jar:lib/nite_linux_x86/com.primesense.nite.jar:."
javac -cp $CLASS_PATH= src/*.java -d `pwd`
