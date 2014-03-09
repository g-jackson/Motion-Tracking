set PATH="%0\..\lib\openni_linux_x64;$0\..\lib\nite_linux_x64;%PATH%";
set CLASS_PATH="lib\openni_linux_x64\org.openni.jar:lib\nite_linux_x64\com.primesense.nite.jar:."
set NATIVE_PATH="`pwd`\lib\openni_linux_x64\:`pwd`\lib\nite_linux_x64\"

java -cp %CLASS_PATH% -Djava.library.path=%NATIVE_PATH% UserViewerApplication
