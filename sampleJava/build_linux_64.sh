CLASS_PATH="lib/openni_linux_x64/org.openni.jar:lib/nite_linux_x64/com.primesense.nite.jar:lib/geomlib.jar:.";
javac -cp $CLASS_PATH src/*.java -Xlint -d $(pwd) 2>&1 | egrep -A 10000 -B 100000 --color "error";
