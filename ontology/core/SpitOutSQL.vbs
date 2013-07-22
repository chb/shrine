Option Explicit

'
' vb macro that takes the contents of Sheet1 and and spits out a SQL script
'
Public Sub SpitOutSQL()
On Error GoTo errlbl

Dim msgtitle As String
msgtitle = "SHRINE Ontology SQLGen VBA Script"

' Choose a file
Dim hasFile As Boolean

Dim fso As New Scripting.FileSystemObject
Dim fileStream As Scripting.TextStream

Do
    Dim fileName As String
    fileName = "C:\Shrine.sql"
    fileName = InputBox("Output SQL File", msgtitle, fileName)
    
    If fso.FileExists(fileName) Then
        If MsgBox("File: '" & fileName & "' exists. Overwrite?", vbYesNo, msgtitle) = vbYes Then
            Set fileStream = fso.OpenTextFile(fileName, ForWriting)
            hasFile = True
        End If
    Else
        Set fileStream = fso.CreateTextFile(fileName, True)
        hasFile = True
    End If
    
Loop While hasFile = False

' Choose a database name
Dim databaseName As String
databaseName = InputBox("Database Name", msgtitle, "SHRINE")

' Enter the version
Dim version As String
version = InputBox("Version", msgtitle, "")

' Double all single quotes (doubling to escape the quote in "Huntington's" for ex.)
Sheet1.Cells.Replace "'", "''", xlPart

' read the header row
Dim r, c, s, sHeader As String
Const HEADER_ROW = 1
Dim cols As New Collection


For c = 1 To Sheet1.Columns.Count
    s = Trim(Sheet1.Cells(HEADER_ROW, c))
    If Len(s) = 0 Then
        Exit For
    Else
        cols.Add s
        If Len(sHeader) > 0 Then sHeader = sHeader & ", "
        sHeader = sHeader & s
    End If
Next
' Debug.Print sHeader

Dim insertStr As String
insertStr = "INSERT INTO " & databaseName & " (" & sHeader & " )"

'insert the magic version record
Dim versionSQl As String
versionSQl = insertStr & " " & "VALUES (0, '\SHRINE\ONTOLOGYVERSION\" & version _
                       & "\', 'ONTOLOGYVERSION', 'N', 'LH', NULL, NULL, '', 'concept_cd'," & _
                       " 'concept_dimension', 'concept_path', 'T', 'LIKE', '\SHRINE\ONTOLOGYVERSION\', NULL );"
fileStream.WriteLine versionSQl



'read each row
Const FIRST_COLUMN = 1
For r = HEADER_ROW + 1 To Sheet1.Rows.Count
    s = Trim(Sheet1.Cells(r, FIRST_COLUMN))
    If Len(s) = 0 Then
        Exit For
    Else
        Dim sRow As String
        sRow = ""
        For c = 1 To cols.Count
            s = Trim(Sheet1.Cells(r, c))
            If c = 1 Then
                sRow = s
            Else
                If UCase(s) <> "NULL" Then
                    s = "'" & s & "'"
                End If
                sRow = sRow & ", " & s
            End If
        Next
        ' Debug.Print sRow
    End If

    Dim valueStr As String
    valueStr = "VALUES (" & sRow & " );"
    
    Dim sqlStr As String
    sqlStr = insertStr & " " & valueStr
    ' Debug.Print sqlStr
    fileStream.WriteLine sqlStr
Next

fileStream.Close
    
' undo single-quote doubling
Sheet1.Cells.Replace "''", "'", xlPart
MsgBox "SQL script generation complete!", vbInformation And vbOKOnly, msgtitle

Exit Sub
errlbl:
    MsgBox "Error: " & Hex$(Err.Number) & vbCrLf & Err.Description, vbCritical And vbOKOnly, msgtitle
End Sub





