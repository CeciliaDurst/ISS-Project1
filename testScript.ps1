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
$isWindows = $false
$isMac     = $false
$osPlatform = [System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform(
    [System.Runtime.InteropServices.OSPlatform]::Windows
)
if ($osPlatform) { $isWindows = $true }
if ([System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform([System.Runtime.InteropServices.OSPlatform]::OSX)) { $isMac = $true }

# Determine host executable
if ($PSVersionTable.PSVersion.Major -ge 7) {
    # PowerShell Core (pwsh)
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
        # Build the escaped command for macOS
        $escapedCommand = $hostExe + ' -NoProfile -File ' + '"' + $childScript + '"' + ' -IP ' + $IP + ' -BatchSize ' + $BatchSize
        # Build AppleScript to open Terminal and run the command
        $appleScript = 'tell application "Terminal" to do script "' + $escapedCommand + '"'
        # Execute AppleScript
        Start-Process -FilePath 'osascript' -ArgumentList @('-e', $appleScript)
    } else {
        # Windows: open new PowerShell windows
        Start-Process -FilePath $hostExe -ArgumentList @(
            '-NoProfile',
            '-NoExit',
            '-File',      $childScript,
            '-IP',        $IP,
            '-BatchSize', $BatchSize
        ) -WindowStyle Normal
    }
}

Write-Host "Spawned $numClients client(s) connecting to $IP with batch size $BatchSize."
