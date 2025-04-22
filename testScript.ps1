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
  - Works on PowerShell 7+ (Core) on macOS or Windows, and on Windows PowerShell 5.1.
  - Ensure `script.ps1` resides in the same directory as this file.
#>

param(
    [Parameter(Mandatory)] [int]    $numClients,
    [Parameter(Mandatory)] [string] $IP,
    [Parameter(Mandatory)] [int]    $BatchSize
)

# Determine host executable based on PowerShell version
if ($PSVersionTable.PSVersion.Major -ge 7) {
    # PowerShell Core (7+)
    $exeName = 'pwsh'
    if ($env:OS -eq 'Windows_NT') {
        $exeName += '.exe'
    }
    $hostExe = Join-Path $PSHOME $exeName
} else {
    # Windows PowerShell 5.1
    $hostExe = 'powershell'
}

# Path to the child script
$childScript = Join-Path $PSScriptRoot 'script.ps1'

# Launch specified number of clients in parallel
for ($i = 1; $i -le $numClients; $i++) {
    Start-Process -FilePath $hostExe -ArgumentList @(
        '-NoProfile'
        '-File',      $childScript
        '-IP',        $IP
        '-BatchSize', $BatchSize
    )
}

Write-Host "Spawned $numClients clients connecting to $IP with batch size $BatchSize."
