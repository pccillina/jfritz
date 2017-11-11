; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!
;
; modifications:
; 30.10.2013, 17:36 by Robert
;     Added version information to feedback email
; 16.09.2011, 20:12 by Robert
;     Added EMail feedback to uninstall
;     Checking if JFritz is running before (un)install
;     Deleting backup directory to uninstall
; 01.09.2011, 21:47 by Robert
;     Fixed deinstallation of XML-Files
; 29.08.2011, 19:27 by Robert
;     Removed jfritz2.exe, replaced jfritz.exe with a new one (64Bit-capable, new error messages)
; 16.10.2010, 22:42 by Robert
;     Added cmd line parameter "--noJavaCheck" to disable Java Version checking on startup
; 14.10.2010, 13:43 by Robert
;     Added check for 32-Bit Java on 64-Bit architecture
; 09.01.2010, 14:45 by Robert
;     Added autoupdate.exe for elevated execution rights (UAC)
;     Updated InstallDelete (added missing files)
; 05.03.2009, 13:54 by Rainer
;     removeUserprofileFiles implemented to remove .update, jfritz.txt in %userprofile%/.jfritz and folder .jfritz
;     added .lock in section "UninstallDelete"   !workaround, because this file is left after uninstall -> bug in JFritz?!
; 25.02.2009, 12:48 by Rainer
;     #Version renamed to #AppVersion
;     {#AppName} and {#AppType} implemented in OutputBaseFilename
;     include-file renamed to JFritz-Version.txt (upper letters)
; 24.02.2009, 20:44 by Rainer; UserProfile implemented in updateVersion()
;
#include "JFritz-Version.txt"
#include "fixfonts.iss"
#include "feedback.iss"

[Setup]
PrivilegesRequired=admin
AppName={#AppName}
AppPublisher={#AppName} Team
AppPublisherURL=http://www.jfritz.org
AppSupportURL=http://www.jfritz.org
AppUpdatesURL=http://www.jfritz.org
DefaultDirName={pf}\{#AppName}
DefaultGroupName={#AppName}
AllowNoIcons=true
OutputDir=Output
; SetupIconFile=setup.ico
Compression=lzma/ultra
SolidCompression=true
AppID={{AF5B3ED5-70D3-48CF-A00F-FC29F5261A37}
ShowLanguageDialog=yes
WizardImageFile=setup_big_new.bmp
WizardSmallImageFile=setup_small.bmp
WizardImageBackColor=clWhite
InternalCompressLevel=ultra
DisableReadyMemo=true
VersionInfoCompany={#AppName} Team
LicenseFile=templates\COPYING.TXT
AppVerName={#AppName} {#AppVersion} Rev. {#AppRevision}
VersionInfoVersion={#AppVersion}
OutputBaseFilename={#AppName}-{#AppVersion}-{#AppType}

[Languages]
Name: en; MessagesFile: compiler:Default.isl; InfoBeforeFile: templates\README-en.TXT
;InfoAfterFile: templates\Telnet-Readme-en.TXT
Name: de; MessagesFile: compiler:Languages\German.isl; InfoBeforeFile: templates\README-de.TXT
;InfoAfterFile: templates\Telnet-Readme-de.TXT

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}
Name: quicklaunchicon; Description: {cm:CreateQuickLaunchIcon}; GroupDescription: {cm:AdditionalIcons}; Flags: unchecked
Name: autostart; Description: {cm:Autostart}; GroupDescription: {cm:AdditionalIcons}; Flags: unchecked

[Files]
source: templates\autoupdate.exe; DestDir: {app}; Flags: ignoreversion
source: templates\COPYING.txt; DestDir: {app}; Flags: ignoreversion
source: templates\jfritz.exe; DestDir: {app}; Flags: ignoreversion
source: templates\MSVCR71.dll; DestDir: {app}; Flags: ignoreversion
source: templates\README-de.txt; DestDir: {app}; DestName: README.txt; Flags: ignoreversion; Languages: de
source: templates\README-en.txt; DestDir: {app}; DestName: README.txt; Flags: ignoreversion; Languages: en
source: templates\Start-JFritz.bat; DestDir: {app}; Flags: ignoreversion
source: templates\Start-JFritz-DebugMode.bat; DestDir: {app}; Flags: ignoreversion
source: binaries\*.jar; DestDir: {app}; Flags: ignoreversion
source: binaries\*.png; DestDir: {app}; Flags: ignoreversion
; source: binaries\JFritz-Handbuch.pdf; DestDir: {app}; Flags: ignoreversion;
source: binaries\log4j.xml; DestDir: {app}; Flags: ignoreversion
source: binaries\jacob.dll; DestDir: {app}; Flags: ignoreversion
source: binaries\conf\*; DestDir: {app}\conf; Flags: ignoreversion recursesubdirs
source: binaries\lib\windows\*.*; DestDir: {app}\lib\windows\; Flags: ignoreversion recursesubdirs
source: binaries\lib\*.jar; DestDir: {app}\lib\; Flags: ignoreversion
source: binaries\lang\*.properties; DestDir: {app}\lang\; Flags: ignoreversion
source: binaries\lang\flags\*.gif; DestDir: {app}\lang\flags; Flags: ignoreversion
source: binaries\lang\flags\*.png; DestDir: {app}\lang\flags; Flags: ignoreversion
source: binaries\number\*; DestDir: {app}\number; Flags: ignoreversion recursesubdirs
source: binaries\pictures\*; DestDir: {app}\pictures; Flags: ignoreversion recursesubdirs
source: binaries\styles\*; DestDir: {app}\styles; Flags: ignoreversion recursesubdirs
source: binaries\Changelog.txt; DestDir: {app}; Flags: ignoreversion
source: jFritz.ico; DestDir: {app}; Flags: ignoreversion

[INI]
Filename: {app}\jfritz.url; Section: InternetShortcut; Key: URL; String: http://www.jfritz.org
;Filename: {app}\temp.txt; Section: Settings; Key: updateOnStart; String: false

[Icons]
Name: {group}\{cm:UninstallProgram,JFritz}; Filename: {uninstallexe}
Name: {group}\{cm:ProgramOnTheWeb,JFritz}; Filename: {app}\jfritz.url
Name: {group}\JFritz; Filename: {app}\jfritz.exe; IconFilename: {app}\jFritz.ico; IconIndex: 0; WorkingDir: {app}
Name: {group}\Readme; Filename: {app}\README.txt
Name: {group}\COPYING; Filename: {app}\COPYING.txt
;Name: {group}\Handbuch; Filename: {app}\JFritz-Handbuch.pdf;
Name: {group}\Changelog; Filename: {app}\Changelog.txt
Name: {userdesktop}\JFritz; Filename: {app}\jfritz.exe; Tasks: desktopicon; IconFilename: {app}\jFritz.ico; IconIndex: 0; WorkingDir: {app}
Name: {userappdata}\Microsoft\Internet Explorer\Quick Launch\JFritz; Filename: {app}\jfritz.exe; Tasks: quicklaunchicon; IconFilename: {app}\jFritz.ico; IconIndex: 0; WorkingDir: {app}
Name: {commonstartup}\JFritz; Filename: {app}\jfritz.exe; Tasks: autostart; IconFilename: {app}\jFritz.ico; IconIndex: 0; WorkingDir: {app}

[Run]
Filename: {app}\jfritz.exe; Description: {cm:LaunchProgram,JFritz}; Parameters: --lang=de; Flags: nowait postinstall skipifsilent; WorkingDir: {app}; Languages: de
Filename: {app}\jfritz.exe; Description: {cm:LaunchProgram,JFritz}; Parameters: --lang=en; Flags: nowait postinstall skipifsilent; WorkingDir: {app}; Languages: en


[UninstallDelete]
Name: {app}\jfritz.url; Type: files
Name: {app}\temp.txt; Type: files
Name: {app}\conf; Type: filesandordirs
Name: {app}\lang; Type: filesandordirs
Name: {app}\lib; Type: filesandordirs
Name: {app}\number; Type: filesandordirs
Name: {app}\pictures; Type: filesandordirs
Name: {app}\styles; Type: filesandordirs
Name: {app}\update; Type: filesandordirs
Name: {app}\.lock; Type: files
Name: {app}; Type: dirifempty
Name: {app}\log4j.log; Type: files

[Dirs]
Name: {app}\lib
Name: {app}\lib\windows
Name: {app}\lib\windows\x86
Name: {app}\conf
Name: {app}\lang
Name: {app}\lang\flags
Name: {app}\number
Name: {app}\number\austria
Name: {app}\number\germany
Name: {app}\number\international
Name: {app}\number\turkey
Name: {app}\number\usa
Name: {app}\pictures
Name: {app}\styles
Name: {app}\update

[Messages]
BeveledLabel=JFritz Team

[InstallDelete]
Name: {app}\autoupdate.exe; Type: files
Name: {app}\Changelog.txt; Type: files
Name: {app}\fritzbox.jar; Type: files
Name: {app}\jacob.dll; Type: files
Name: {app}\jfritz.exe; Type: files
Name: {app}\jfritz.jar; Type: files
Name: {app}\jfritz-internals.jar; Type: files
Name: {app}\MSVCR71.dll; Type: files
Name: {app}\resources.jar; Type: files
Name: {app}\reverselookup.jar; Type: files
Name: {app}\JFritz-Handbuch.pdf; Type: files
Name: {app}\Handbuch.pdf; Type: files
Name: {app}\lib; Type: filesandordirs
Name: {app}\lang; Type: filesandordirs
Name: {app}\number; Type: filesandordirs
Name: {app}\update; Type: filesandordirs
Name: {app}\conf; Type: filesandordirs
Name: {app}\log4j.log; Type: files

[CustomMessages]
de.NoJavaInstalled1=Setup hat festgestellt das keine Java Runtime installiert ist.
de.NoJavaInstalled2=Sie m�ssen mindestens Java Runtime 1.5 oder h�her installiert haben um das Setup fortzusetzen.
de.NoJavaInstalled3=Bitte gehen Sie zu http://www.java.com/en/download/manual.jsp und installieren eine aktuelle Version.
de.NoJavaInstalled4=Danach k�nnen Sie das Setup erneut ausf�hren.

en.NoJavaInstalled1=No Java Runtime installed. To run JFritz you need at least Java Runtime 1.5.
en.NoJavaInstalled2=Please install the latest Java version (http://www.java.com/en/download/manual.jsp)
en.NoJavaInstalled3=and then restart installation of JFritz.
en.NoJavaInstalled4=

de.WrongJavaVersion1=Setup hat festgestellt dass Sie eine Java Version unter 1.5 benutzen, JFritz ben�tigt aber mindestens Java 1.5.
de.WrongJavaVersion2=Bitte gehen Sie zu http://www.java.com/en/download/manual.jsp und installieren eine aktuelle Version.
de.WrongJavaVersion3=Danach k�nnen Sie das Setup erneut ausf�hren.

en.WrongJavaVersion1=JFritz need at least Java Runtime Environment 1.5 but you have installed an older version.
en.WrongJavaVersion2=Please install the latest Java version (http://www.java.com/en/download/manual.jsp)
en.WrongJavaVersion3=and then restart installation of JFritz.

de.DeleteAllFiles=Sollen die Anrufliste, Einstellungen und das Telefonbuch gel�scht werden?
en.DeleteAllFiles=Delete call list, all settings and phonebook-entries?

de.DeleteError=Es konnten nicht alle Dateien gel�scht werden.
en.DeleteError=Could not delete all files.

de.Autostart=JFritz &automatisch beim Windows-Start starten.
en.Autostart=Start JFritz &automatically on Windows startup.

de.InstallRunning1=JFritz wird derzeit noch ausgef�hrt. Bitte beenden Sie JFritz, bevor Sie mit der Installation fortfahren.
de.InstallRunning2=Wollen Sie nun mit der Installation fortfahren?

en.InstallRunning1=JFritz is currently running. Please close JFritz before proceeding.
en.InstallRunning2=Do you want to proceed with the installation of JFritz?

de.UninstallRunning1=JFritz wird derzeit noch ausgef�hrt. Bitte beenden Sie JFritz, bevor Sie mit der Deinstallation fortfahren.
de.UninstallRunning2=Wollen Sie nun mit der Deinstallation fortfahren?

en.UninstallRunning1=JFritz is currently running. Please close JFritz before proceeding.
en.UninstallRunning2=Do you want to proceed with the deinstallation of JFritz?

de.UninstallFeedbackTitle=JFritz Deinstallations R�ckmeldung
en.UninstallFeedbackTitle=JFritz Uninstall Feedback

de.Send=&Senden
en.Send=&Send

de.Cancel=&Abbruch
en.Cancel=&Cancel

de.EMailText1=Um JFritz weiter zu verbessern, w�rden wir gerne �ber alle Probleme von JFritz informiert werden. 
en.EMailText1=To help us with future versions of JFritz, we want to know about any troubles or difficulties you have experienced while using JFritz.

de.EMailText2=Bitte geben Sie uns Bescheid, wieso Sie JFritz deinstallieren. Vielen Dank.
en.EMailText2=Please let us know why are you uninstalling JFritz. Thank You.

de.EMailSubject=JFritz Deinstallations R�ckmeldung
en.EMailSubject=JFritz Uninstall Feedback

[Code]
var fehler : boolean;

function getUserProfilePath(): String;
var
  path: String;
begin
  path := ExpandConstant('{%USERPROFILE|PathError}');
  if path = 'PathError' then
  begin
    // Error - no USERPROFILE environment variable found (Win 95/98/ME)
    path := ExpandConstant('{userdesktop}') + '\..\';
  end else
  begin
    // UserProfile exists
    path := path + '\';
  end;
  Result := path;
end;

procedure updateVersion();
var
  updateFile : String;
  section : String;
  updateOnStart : Boolean;
  locale : String;
  programVersion : String;
 begin
  updateFile := getUserProfilePath() + '.jfritz\.update';
  section := 'Settings';
  updateOnStart := true;
  locale := 'de_de';
  programVersion := '';
  if FileExists(updateFile) then
  begin
//    if IniKeyExists(section, 'updateOnStart', updateFile) then
//      begin
//        updateOnStart := GetIniBool(section, 'updateOnStart', true, updateFile);
//      end;
//    if IniKeyExists(section, 'locale', updateFile) then
//      begin
//        locale := GetIniString(section, 'locale', 'de_de', updateFile);
//      end;
//    if IniKeyExists(section, 'programVersion', updateFile) then
//      begin
//        programVersion := GetIniString(section, 'programVersion', '0.0.0', updateFile);
     SetIniString(section, 'programVersion', ExpandConstant('{#AppVersion}'), updateFile);
     SetIniString(section, 'programRevision', ExpandConstant('{#AppRevision}'), updateFile);
//      end;
  end;
end;

procedure Explode(var Dest: TArrayOfString; Text: String; Separator: String);
// Author:			Jared Breland <jbreland@legroom.net>
// Homepage:		http://www.legroom.net/mysoft
// License:			GNU Lesser General Public License (LGPL), version 3
var
	i: Integer;
begin
	i := 0;
	repeat
		SetArrayLength(Dest, i+1);
		if Pos(Separator,Text) > 0 then	begin
			Dest[i] := Copy(Text, 1, Pos(Separator, Text)-1);
			Text := Copy(Text, Pos(Separator,Text) + Length(Separator), Length(Text));
			i := i + 1;
		end else begin
			 Dest[i] := Text;
			 Text := '';
		end;
	until Length(Text)=0;
end;

function getJFritzDataDirectory(): String;
var
  f : String;
  saveDir, firstLine : String;
  lines : Array Of String;
  numLines : Integer;
  splitted : TArrayOfString;
begin
  f := getUserProfilePath() + '.jfritz\jfritz.txt';
  if FileExists(f) then
  begin
    if IniKeyExists('Settings', 'Save_Directory', f) then
      begin
        saveDir := GetIniString('Settings', 'Save_Directory', '...', f);
      end else begin
        LoadStringsFromFile(f, lines);
        numLines := GetArrayLength(lines) -1;
        firstLine := lines[0];
        Explode(splitted, firstLine, '=');
        saveDir := splitted[1];
      end;
  end;
  Result := saveDir;
end;


function LoadValueFromXML(const AFileName, APath: string): string;
var
  XMLNode: Variant;
  XMLDocument: Variant;  
begin
  Result := '';
  XMLDocument := CreateOleObject('Msxml2.DOMDocument.6.0');
  try
    XMLDocument.async := False;
    XMLDocument.load(AFileName);
    if (XMLDocument.parseError.errorCode <> 0) then
    begin
      Result := 'exception';
    end
    else
    begin
      XMLDocument.setProperty('SelectionLanguage', 'XPath');
      XMLNode := XMLDocument.selectSingleNode(APath);
      Result := XMLNode.text;
    end;
  except
    Result := 'exception';
  end;
end;

function getFirmwareFromPropertyFile() : String;
var
  jfritzDataPath : String;
  fwVersion : String;
  propertyFile : String;
begin
  fwVersion := 'unknown';
  jfritzDataPath := getJFritzDataDirectory();
  propertyFile := ExpandConstant(jfritzDataPath) + '\jfritz.properties.xml';
  if FileExists(propertyFile) then
  begin
    fwVersion := LoadValueFromXML(propertyFile, '//properties/entry[@key = ''box.firmware'']');
    if fwVersion = 'exception' then
    begin
      fwVersion := 'could not detect firmware [1]';
    end;
  end
  else
  begin
    fwVersion := 'could not detect firmware [2]';
  end;

  Result := fwVersion;
end;

function getJavaVersion(): String;
var
     javaVersion: String;
begin
     javaVersion := '';
     if IsWin64 then begin
        RegQueryStringValue(HKLM64, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', javaVersion);
        if javaVersion = '' then begin
           RegQueryStringValue(HKLM32, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', javaVersion);
        end;
     end else begin
        RegQueryStringValue(HKLM32, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', javaVersion);
     end;
     GetVersionNumbersString(javaVersion, javaVersion);
     Result := javaVersion;
end;

function useJavaCheck(): Boolean;
var s: String;
begin
  s := GetCmdTail;
  if Pos (' --noJavaCheck', GetCmdTail) <> 0 then begin
    Result := false;
    end
  else begin
    Result := true;
  end
end;

function doesLockFileExist() : Boolean;
var
  jfritzDataPath : String;
begin
  jfritzDataPath := getJFritzDataDirectory();
  if FileExists(ExpandConstant(jfritzDataPath) + '\.lock') then
  begin
    Result := true
  end else begin
    Result := false;
  end;
end;

function InitializeSetup(): Boolean;
begin
     Result := true;
     if (doesLockFileExist()) then 
     begin
       Result := MsgBox(CustomMessage('InstallRunning1') + #10#13 + #10#13 + 
              CustomMessage('InstallRunning2'), mbConfirmation, MB_YESNO) = IDYES;
     end;  

     updateVersion();
     if (not useJavaCheck()) then
       MsgBox('Java check disabled', mbInformation, MB_OK);
     if (useJavaCheck() and (Length( getJavaVersion() ) = 0)) then begin
       MsgBox(CustomMessage('NoJavaInstalled1') + #13
          + CustomMessage('NoJavaInstalled2') + #13
          + CustomMessage('NoJavaInstalled3') + #13
          + CustomMessage('NoJavaInstalled4'), mbInformation, MB_OK);
          Result := false;
          end
     else begin
          if (getJavaVersion()) < '1.5' then begin
               MsgBox(CustomMessage('WrongJavaVersion1') + #13
                    + CustomMessage('WrongJavaVersion2') + #13
                    + CustomMessage('WrongJavaVersion3'), mbInformation, MB_OK);
               Result := false
          end
     end;
end;

procedure removeUserprofileFiles();
var
  uProfile : String;
  updateFile : String;
  fritzTxtFile : String;
 begin
  uProfile := ExpandConstant('{%USERPROFILE|PathError}');
  if uProfile = 'PathError' then
  begin
    // Error - no USERPROFILE environment variable found (Win 95/98/ME)
    // updateFile := ExpandConstant('{win}') + '\.jfritz\.update';                    works!  -> c:\windows\.jfritz\.update
    // updateFile := ExpandConstant('{userdesktop}') + '\..\.jfritz\.update';         works!  -> c:\windows\desktop\..\.jfritz\.update
    updateFile := ExpandConstant('{userdesktop}') + '\..\.jfritz\.update';
    fritzTxtFile := ExpandConstant('{userdesktop}') + '\..\.jfritz\jfritz.txt';
  end else
  begin
    // UserProfile exists
    updateFile := uProfile + '\.jfritz\.update';
    fritzTxtFile := uProfile + '\.jfritz\jfritz.txt';
  end;
  // .update-File
  if FileExists(updateFile) then
  begin
      if not DeleteFile(updateFile) then
      begin
        fehler := True;
      end;
  end;
  // jfritz.txt
  if FileExists(fritzTxtFile) then
  begin
      if not DeleteFile(fritzTxtFile) then
      begin
        fehler := True;
      end;
  end;
  // .jfritz-Folder
  RemoveDir(uProfile + '\.jfritz');
end;


procedure DeleteDirectoryRecursive(ADirName: string);
var
  FindRec: TFindRec;
begin
  if FindFirst(ADirName + '\*.*', FindRec) then begin
    try
      repeat
        if FindRec.Attributes and FILE_ATTRIBUTE_DIRECTORY <> 0 then begin
          if (FindRec.Name <> '.') and (FindRec.Name <> '..') then begin
            DeleteDirectoryRecursive(ADirName + '\' + FindRec.Name);
            RemoveDir(ADirName + '\' + FindRec.Name);
          end;
        end else
          DeleteFile(ADirName + '\' + FindRec.Name);
      until not FindNext(FindRec);
    finally
      FindClose(FindRec);
    end;
  end;
end;

procedure removeBackups();
var
  jfritzDataPath : String;
begin
  jfritzDataPath := getJFritzDataDirectory();
  DeleteDirectoryRecursive(ExpandConstant(jfritzDataPath)+'\backup');
  RemoveDir(ExpandConstant(jfritzDataPath)+'\backup');
end;

procedure removeXMLFiles();
var
  jfritzDataPath : String;
  slFiles: TStringList;
  i: Integer;
begin
  fehler := false;
  jfritzDataPath := getJFritzDataDirectory();
  slFiles := TStringList.Create;
  slFiles.Add(jfritzDataPath+'jfritz.calls.xml');
  slFiles.Add(jfritzDataPath+'jfritz.clientsettings.xml');
  slFiles.Add(jfritzDataPath+'jfritz.phonebook.xml');
  slFiles.Add(jfritzDataPath+'jfritz.properties.xml');
  slFiles.Add(jfritzDataPath+'jfritz.quickdials.xml');
  slFiles.Add(jfritzDataPath+'jfritz.sipprovider.xml');
  slFiles.Add(jfritzDataPath+'jfritz.state.properties.xml');
  slFiles.Add(jfritzDataPath+'jfritz.window.properties.xml');
  slFiles.Add(jfritzDataPath+'debug.log');
  slFiles.Add(jfritzDataPath+'log4j.xml');
  slFiles.Add(jfritzDataPath+'log4j.log');
  slFiles.Add(jfritzDataPath+'..\JFritz.lock');
  
  for i := 0 to slFiles.Count - 1 do
  begin
    slFiles.Strings[i] := ExpandConstant(slFiles.Strings[i]);
    if FileExists(slFiles.Strings[i]) then
    begin
      if not DeleteFile(slFiles.Strings[i]) then
      begin
        RestartReplace(slFiles.Strings[i], '');
        fehler := True;
      end;
    end;
  end;

  RemoveDir(ExpandConstant(jfritzDataPath));
  slFiles.Free;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var WindowsVersion: String;
    FirmwareVersion : String;
begin
	case CurUninstallStep of
		usPostUninstall :
		begin
      if (MsgBox(CustomMessage('DeleteAllFiles'), mbConfirmation, MB_YESNO or MB_DEFBUTTON2) = IDYES) then
        begin
        removeBackups();
        removeXMLFiles();
        removeUserprofileFiles();
	      end;
	      
       WindowsVersion := GetWindowsVersionString();
       FirmwareVersion := getFirmwareFromPropertyFile();

	     UninstallFeedback(
	       CustomMessage('UninstallFeedbackTitle'), 
	       CustomMessage('Send'), 
	       CustomMessage('Cancel'),
	       CustomMessage('EMailText1') + #10#13 + CustomMessage('EMailText2'),
	       'jfritz@robotniko.de', 
	       CustomMessage('EMailSubject') + ' v{#AppVersion} Rev. {#AppRevision}',
         ' ' 
           + #10#13 
           + #10#13 + '--' 
           + #10#13 + 'JFritz Version: {#AppVersion} Rev. {#AppRevision}' 
           + #10#13 + 'Firmware version: ' + FirmwareVersion
           + #10#13 + 'Windows version: ' + WindowsVersion);
	  end;
  end;
end;

function InitializeUninstall(): Boolean;
begin
  Result := true;
  if (doesLockFileExist()) then 
  begin
    Result := MsgBox(CustomMessage('UninstallRunning1') + #10#13 + #10#13 +
           CustomMessage('UninstallRunning2'), mbConfirmation, MB_YESNO) = IDYES;
  end;  
end;

procedure DeinitializeUninstall();
begin
if (fehler) then
   begin
   MsgBox(CustomMessage('DeleteError'),mbError, MB_OK);
   end;
end;
