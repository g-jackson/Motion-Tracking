SET CLASS_PATH="lib\openni_windows_x64\org.openni.jar;lib\nite_windows_x64\com.primesense.nite.jar;lib\geomlib.jar;."
javac -cp %CLASS_PATH% src\*.java -d %0\..\
