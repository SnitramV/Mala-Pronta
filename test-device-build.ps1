$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$bt=Join-Path $env:ANDROID_HOME 'build-tools\35.0.0'
function Run([string]$Exe,[string[]]$Arguments){ & $Exe @Arguments; if($LASTEXITCODE -ne 0){throw "Falha: $Exe"} }
New-Item -ItemType Directory -Force build\instrumentation\classes,build\instrumentation\dex | Out-Null
$src=Join-Path $PWD 'tests\PhotoInstrumentation.java'
[IO.File]::WriteAllText($src,[IO.File]::ReadAllText($src),[Text.UTF8Encoding]::new($false))
Run "$env:JAVA_HOME\bin\javac.exe" @('-encoding','UTF-8','--release','8','-classpath','build\android.jar;build\classes','-d','build\instrumentation\classes',$src)
$classes=@(Get-ChildItem build\instrumentation\classes -Recurse -Filter *.class | ForEach-Object {$_.FullName})
Run "$bt\d8.bat" (@('--lib','build\android.jar','--classpath','build\classes','--min-api','26','--output','build\instrumentation\dex')+$classes)
Run "$bt\aapt2.exe" @('link','-o','build\instrumentation\unsigned.apk','--manifest','tests\AndroidManifest.xml','-I','build\android.jar')
Run "$env:JAVA_HOME\bin\jar.exe" @('uf','build\instrumentation\unsigned.apk','-C','build\instrumentation\dex','classes.dex')
Run "$bt\zipalign.exe" @('-f','4','build\instrumentation\unsigned.apk','build\instrumentation\aligned.apk')
Run "$bt\apksigner.bat" @('sign','--ks','build\local-signing.p12','--ks-pass','pass:malapronta-local','--out','build\instrumentation\tests.apk','build\instrumentation\aligned.apk')
