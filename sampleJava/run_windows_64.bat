set PATH="%0\..\lib\openni_windows_x64\;%0\..\lib\nite_windows_x64\;%PATH%";
set CLASS_PATH=".\lib\openni_windows_x64\org.openni.jar;.\lib\nite_windows_x64\com.primesense.nite.jar;.";
set NATIVE_PATH=lib\openni_windows_x64\;lib\nite_windows_x64\

"C:\Program Files\Java\jre7\bin\java.exe" -cp %CLASS_PATH% -Djava.library.path=%NATIVE_PATH% UserViewerApplication
