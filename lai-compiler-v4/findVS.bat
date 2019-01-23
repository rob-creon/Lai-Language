@echo off

set INSTALLPATH=

if exist "%programfiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe" (
  for /F "tokens=* USEBACKQ" %%F in (`"%programfiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe" -version 15.0 -property installationPath`) do set INSTALLPATH=%%F
)

REM Save current dir for later
pushd %CD%

if NOT "" == "%INSTALLPATH%" (
  call "%INSTALLPATH%\Common7\Tools\VsDevCmd.bat"
) else (
  goto ERROR_NO_VS15
)

:WORK
REM Retrieve the working dir and proceed
popd
echo Compiling from %CD%
cl output.c
goto END

:ERROR_NO_VS15
echo Visual Studio 2017 Tools Not Available!

:END
echo Processing ends.