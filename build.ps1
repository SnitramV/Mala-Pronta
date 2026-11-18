param([string]$Sdk = $env:ANDROID_HOME, [string]$Jdk = 'C:\Program Files\Java\jdk-17')
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$env:JAVA_HOME = $Jdk
$env:PATH = "$Jdk\bin;$env:PATH"
$bt = Join-Path $Sdk 'build-tools\35.0.0'
$android = Join-Path $Sdk 'platforms\android-35\android.jar'
function Run([string]$Exe, [string[]]$Arguments) {
    & $Exe @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Falha em $Exe (código $LASTEXITCODE)" }
}
New-Item -ItemType Directory -Force build\classes,build\dex,dist | Out-Null
# Local copy avoids JDK archive path resolution restrictions in sandboxed builds.
Copy-Item $android 'build\android.jar' -Force
$android = Join-Path $PSScriptRoot 'build\android.jar'
# javac requires UTF-8 sources without BOM.
$sourceFiles = @(Get-ChildItem app\src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
foreach ($source in $sourceFiles) { [System.IO.File]::WriteAllText($source,[System.IO.File]::ReadAllText($source),[System.Text.UTF8Encoding]::new($false)) }
Run "$bt\aapt2.exe" @('compile','--dir','app\src\main\res','-o','build\resources.zip')
Run "$bt\aapt2.exe" @('link','-o','build\unsigned.apk','--manifest','app\src\main\AndroidManifest.xml','-I',$android,'-R','build\resources.zip','--auto-add-overlay')
Run "$Jdk\bin\javac.exe" (@('-encoding','UTF-8','--release','8','-classpath',$android,'-d','build\classes') + $sourceFiles)
$classFiles = @(Get-ChildItem build\classes -Recurse -Filter *.class | ForEach-Object { $_.FullName })
Run "$bt\d8.bat" (@('--lib',$android,'--min-api','26','--output','build\dex') + $classFiles)
Run "$Jdk\bin\jar.exe" @('uf','build\unsigned.apk','-C','build\dex','classes.dex')
Run "$bt\zipalign.exe" @('-f','-p','4','build\unsigned.apk','build\aligned.apk')
if (!(Test-Path 'build\local-signing.p12')) {
    Run "$Jdk\bin\keytool.exe" @('-genkeypair','-keystore','build\local-signing.p12','-storetype','PKCS12','-storepass','malapronta-local','-keypass','malapronta-local','-alias','malapronta','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Mala Pronta, O=Mala Pronta, C=BR')
}
Run "$bt\apksigner.bat" @('sign','--ks','build\local-signing.p12','--ks-pass','pass:malapronta-local','--ks-key-alias','malapronta','--out','dist\Mala-Pronta.apk','build\aligned.apk')
Run "$bt\apksigner.bat" @('verify','--verbose','dist\Mala-Pronta.apk')
Run "$bt\aapt2.exe" @('dump','badging','dist\Mala-Pronta.apk')
Get-FileHash dist\Mala-Pronta.apk -Algorithm SHA256
