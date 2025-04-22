param(
  [Parameter(Mandatory)] [string]$IP,
  [Parameter(Mandatory)] [int]$BatchSize
)

javac .\Client.java

$protocol = @(
  (1..5 | ForEach-Object { 'SEND'; $BatchSize })
  'bye'
)

$protocol -join "`n" | java Client $IP 6000