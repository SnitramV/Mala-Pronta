param([string]$Jdk = 'C:\Program Files\Java\jdk-17')
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force build\tests | Out-Null
& "$Jdk\bin\javac.exe" -encoding UTF-8 -d build\tests app\src\main\java\br\com\malapronta\Checklist.java tests\ChecklistTest.java
if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar testes' }
& "$Jdk\bin\java.exe" -cp build\tests br.com.malapronta.ChecklistTest
if ($LASTEXITCODE -ne 0) { throw 'Falha nos testes' }
