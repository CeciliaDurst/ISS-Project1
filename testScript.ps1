<#
.SYNOPSIS
  Launch multiple instances of script.ps1 in parallel,
  passing each the same -IP and -BatchSize.

.PARAMETER numClients
  Number of parallel clients to spin up.

.PARAMETER IP
  The IP address to pass to each client.

.PARAMETER BatchSize
  The batch size to pass to each client.

.NOTES
  - Requires PowerShell 7+ on macOS or Windows (or PowerShell 5.1 on Windows).
  - script.ps1 must live in the same folder as this file.
#>

param(
  [Parameter(Mandatory)] [int]    $numClients,
  [Parameter(Mandatory)] [string] $IP,
  [Parameter(Mandatory)] [int]    $BatchSize
)

# 1) Pick the right host executable
#    - On PS 7+ (Core), use 'pwsh' (pwsh.exe on Windows, pwsh on macOS)
#    - On Desktop (PS 5.1), fall back to 'powershell'
if ($PSVersionTable.PSEdition -eq 'Desktop') {
    $hostExe = 'powershell'
} else {
    # Core: point at the pwsh in $PSHOME (this works on both Windows & macOS)
    $hostExe = Join-Path $PSHOME ('pwsh' + ($IsWindows ? '.exe' : ''))
}

# 2) Path to the child script
#    Use $PSScriptRoot so it works no matter where you invoke it from
$childScript = Join-Path $PSScriptRoot 'script.ps1'

# 3) Launch N copies in parallel
for ($i = 1; $i -le $numClients; $i++) {
    Start-Process -FilePath $hostExe -ArgumentList @(
      '-NoProfile'
      '-File' , $childScript
      '-IP'   , $IP
      '-BatchSize', $BatchSize
    )
}

Write-Host "Spawned $numClients clients connecting to $IP with batch size $BatchSize."
