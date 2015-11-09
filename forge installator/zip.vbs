Set oFSO = CreateObject("Scripting.FileSystemObject")
ToZip    = oFSO.GetAbsolutePathName(WScript.Arguments.Item(0))
ZipName  = oFSO.GetAbsolutePathName(WScript.Arguments.Item(1))

d=WindowsZip(ToZip, ZipName)

Function WindowsZip(sFile, sZipFile)
  Set oZipShell = CreateObject("WScript.Shell")
  Set oZipFSO   = CreateObject("Scripting.FileSystemObject")
  If Not oZipFSO.FileExists(sZipFile) Then
    NewZip(sZipFile)
  End If
  Set oZipApp = CreateObject("Shell.Application")
  sZipFileCount = oZipApp.NameSpace(sZipFile).items.Count
      aFileName = Split(sFile, "\")
      sFileName = (aFileName(Ubound(aFileName)))
          sDupe = False
  For Each sFileNameInZip In oZipApp.NameSpace(sZipFile).items
    If LCase(sFileName) = LCase(sFileNameInZip) Then
        sDupe = True
        Exit For
    End If
  Next
  If Not sDupe Then
        wscript.echo "Adding " & sfile
        oZipApp.NameSpace(sZipFile).Copyhere sFile
        On Error Resume Next
        Do Until sZipFileCount < oZipApp.NameSpace(sZipFile).Items.Count
            Wscript.Sleep(100)
        Loop
        On Error GoTo 0
  End If
End Function

Sub NewZip(sNewZip)
  Set oNewZipFSO  = CreateObject("Scripting.FileSystemObject")
  Set oNewZipFile = oNewZipFSO.CreateTextFile(sNewZip)
  oNewZipFile.Write Chr(80) & Chr(75) & Chr(5) & Chr(6) & String(18, 0)
  oNewZipFile.Close
  Set oNewZipFSO = Nothing
  Wscript.Sleep(500)
End Sub