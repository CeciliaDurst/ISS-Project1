<#
.SYNOPSIS
  Launch multiple instances of script.ps1 in parallel in separate terminals,
  passing each the same -IP and -BatchSize; works on Windows and macOS.

.PARAMETER numClients
  Number of parallel clients to spin up.

.PARAMETER IP
  The IP address to pass to each client.

.PARAMETER BatchSize
  The batch size to pass to each client.

.NOTES
  - Requires PowerShell 7+ on both macOS and Windows, or PowerShell 5.1 on Windows.
  - `script.ps1` must be in the same directory as this file.
#>

param(
    [Parameter(Mandatory)] [int]    $numClients,
    [Parameter(Mandatory)] [string] $IP,
    [Parameter(Mandatory)] [int]    $BatchSize
)

# Determine OS
$osPlatform = [System.Runtime.InteropServices.RuntimeInformation]::OSDescription
$isWindows = $osPlatform -match 'Windows'
$isMac     = $osPlatform -match 'Darwin'  # macOS kernel identifies as Darwin

# Determine host executable
if ($PSVersionTable.PSVersion.Major -ge 7) {
    # PowerShell Core
    $exeName = if ($isWindows) { 'pwsh.exe' } else { 'pwsh' }
    $hostExe = Join-Path $PSHOME $exeName
} else {
    # Legacy Windows PowerShell
    $hostExe = 'powershell'
}

# Child script path
$childScript = Join-Path $PSScriptRoot 'script.ps1'

# Launch clients
for ($i = 1; $i -le $numClients; $i++) {
    if ($isMac) {
        # On macOS, use AppleScript to open a new Terminal window and run the command
        $escapedCommand = "$hostExe -NoProfile -File /"$childScript/" -IP $IP -BatchSize $BatchSize"
        $appleScript = "tell application /"Terminal/" to do script /"$escapedCommand/""
        Start-Process -FilePath 'osascript' -ArgumentList '-e', $appleScript
    } else {
        # On Windows, Start-Process will open a new console window by default
        Start-Process -FilePath $hostExe -ArgumentList @(
            '-NoProfile'
            '-File',      $childScript
            '-IP',        $IP
            '-BatchSize', $BatchSize
        ) -WindowStyle Normal
    }
}

Write-Host "Spawned $numClients client(s) connecting to $IP with batch size $BatchSize."
