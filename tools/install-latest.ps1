$ErrorActionPreference='Stop';$ProgressPreference='SilentlyContinue'
$m='D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods'
if(!(Test-Path $m)){throw "No existe $m"}
$a=(Invoke-RestMethod 'https://api.github.com/repos/Santi-PdR/GripeVerde/releases/latest' -UseBasicParsing).assets|?{$_.name -like 'gripeverde-*.jar'}|select -First 1
if(!$a){throw 'Sin JAR en el ultimo release'}
gci $m -Filter '*gripeverde*.jar'|%{try{rm $_.FullName -Force -EA Stop}catch{throw "Cerra Minecraft: $($_.Name) en uso"}}
$o=Join-Path $m $a.name
iwr $a.browser_download_url -OutFile $o -UseBasicParsing
$f=Get-Item $o
if($f.Length -ne $a.size){throw "Incompleto: $($f.Length)/$($a.size) bytes"}
"OK $($f.Name) $($f.Length) bytes en $m"
