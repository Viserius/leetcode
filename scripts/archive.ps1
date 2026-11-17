<#
.SYNOPSIS
    Archiveert de oplossing uit boilerplate/Solution.java als "NNNNN. Titel.java",
    reset de boilerplate en werkt de index in README.md bij.

.DESCRIPTION
    Zonder argumenten vraagt het script alles interactief uit: opgave (bijv. "46. Permutations"),
    runtime, memory, overschrijven en of er direct gecommit moet worden.

    Neemt de code tussen "// region solution" en "// endregion" uit boilerplate/Solution.java,
    wikkelt die in "class Solution { ... }" (precies wat je op LeetCode indient) en schrijft dat
    naar de root. Gebruikt de oplossing ListNode/TreeNode, dan wordt de LeetCode-definitie als
    commentaar toegevoegd.

.EXAMPLE
    ./scripts/archive.ps1
    Interactief.

.EXAMPLE
    ./scripts/archive.ps1 -Number 46 -Title "Permutations" -Runtime "1ms beats 93%" -Memory "44MB beats 70%"
    Niet-interactief (geen vragen, geen commit).

.EXAMPLE
    ./scripts/archive.ps1 -IndexOnly
#>
[CmdletBinding(DefaultParameterSetName = 'Archive')]
param(
    [Parameter(ParameterSetName = 'Archive', Position = 0)]
    [ValidateRange(1, 99999)]
    [int] $Number,

    [Parameter(ParameterSetName = 'Archive', Position = 1)]
    [string] $Title,

    [Parameter(ParameterSetName = 'Archive')] [string] $Runtime,
    [Parameter(ParameterSetName = 'Archive')] [string] $Memory,
    [Parameter(ParameterSetName = 'Archive')] [switch] $NoReset,
    [Parameter(ParameterSetName = 'Archive')] [switch] $Force,

    [Parameter(Mandatory, ParameterSetName = 'Index')]
    [switch] $IndexOnly
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$solutionPath = Join-Path $root 'boilerplate/Solution.java'
$templatePath = Join-Path $root 'templates/Solution.java.tmpl'
$readmePath = Join-Path $root 'README.md'
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$nl = "`r`n"
$namePattern = '^(\d{5})\. (.+)\.java$'

# --- Helpers -------------------------------------------------------------------

function Get-Slug([string] $text) {
    $slug = $text.ToLowerInvariant() -replace '[^a-z0-9 \-]', '' -replace '\s+', '-' -replace '-+', '-'
    return $slug.Trim('-')
}

function Get-RegionBody([string] $source) {
    $m = [regex]::Match($source, '(?s)//\s*region solution[^\n]*\n(.*?)\r?\n[ \t]*//\s*endregion')
    if (-not $m.Success) { throw "Geen '// region solution' ... '// endregion' gevonden." }
    return $m.Groups[1].Value.TrimEnd()
}

function Test-ValidTitle([string] $t) {
    return $t -and $t.IndexOfAny([System.IO.Path]::GetInvalidFileNameChars()) -lt 0
}

function Read-Answer([string] $prompt, [string] $default = '') {
    $suffix = if ($default) { " [$default]" } else { '' }
    $answer = Read-Host "$prompt$suffix"
    if ($null -eq $answer) { $answer = '' }
    $answer = $answer.Trim()
    if (-not $answer) { return $default }
    return $answer
}

function Read-YesNo([string] $prompt, [bool] $default) {
    $hint = if ($default) { 'J/n' } else { 'j/N' }
    while ($true) {
        $a = Read-Answer "$prompt ($hint)"
        if (-not $a) { return $default }
        switch -Regex ($a) {
            '^(j|ja|y|yes)$' { return $true }
            '^(n|nee|no)$' { return $false }
        }
        Write-Host '  Antwoord met j of n.' -ForegroundColor Yellow
    }
}

# "1ms 93" / "1 ms 93.5%" -> "1ms beats 93.5%"; volledige tekst blijft ongewijzigd.
function Format-Stat([string] $value) {
    if (-not $value) { return '' }
    $m = [regex]::Match($value, '^\s*([\d.]+)\s*([a-zA-Z]+)\s+([\d.]+)\s*%?\s*$')
    if ($m.Success) { return "$($m.Groups[1].Value)$($m.Groups[2].Value) beats $($m.Groups[3].Value)%" }
    return $value.Trim()
}

function Update-Index {
    $files = Get-ChildItem -LiteralPath $root -File -Filter '*.java' |
        Where-Object { $_.Name -match $namePattern } |
        Sort-Object Name

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("$($files.Count) opgaven opgelost.")
    $lines.Add('')
    $lines.Add('| # | Opgave | Oplossing |')
    $lines.Add('|--:|--------|-----------|')
    foreach ($f in $files) {
        $null = $f.Name -match $namePattern
        $nr = [int] $Matches[1]
        $title = $Matches[2]
        $href = $f.Name.Replace(' ', '%20').Replace('(', '%28').Replace(')', '%29')
        $lines.Add("| $nr | [$title](https://leetcode.com/problems/$(Get-Slug $title)/) | [Java]($href) |")
    }

    $start = '<!-- INDEX:START -->'
    $end = '<!-- INDEX:END -->'
    $readme = [System.IO.File]::ReadAllText($readmePath)
    $i = $readme.IndexOf($start)
    $j = $readme.IndexOf($end)
    if ($i -lt 0 -or $j -lt $i) { throw "README.md mist de markers $start / $end." }
    $before = $readme.Substring(0, $i + $start.Length)
    $after = $readme.Substring($j)
    $updated = $before + $nl + ($lines -join $nl) + $nl + $after
    [System.IO.File]::WriteAllText($readmePath, $updated, $utf8NoBom)
    Write-Host "README-index bijgewerkt ($($files.Count) opgaven)."
}

if ($IndexOnly) {
    Update-Index
    return
}

# --- Oplossing controleren -----------------------------------------------------

$body = Get-RegionBody ([System.IO.File]::ReadAllText($solutionPath))
$templateBody = Get-RegionBody ([System.IO.File]::ReadAllText($templatePath))
if ($body.Trim() -eq $templateBody.Trim()) {
    throw 'De oplossing is nog gelijk aan de template; niets te archiveren.'
}

# Interactief zodra nummer of titel ontbreekt.
$interactive = -not ($PSBoundParameters.ContainsKey('Number') -and $PSBoundParameters.ContainsKey('Title'))

if ($interactive) {
    $method = [regex]::Match($body, 'public\s+[\w<>\[\],\s]+?\s+(\w+)\s*\(').Groups[1].Value
    Write-Host ''
    Write-Host "Archiveren van de oplossing$(if ($method) { " ($method)" })" -ForegroundColor Cyan
    Write-Host ''

    # Nummer + titel; accepteert de LeetCode-kop letterlijk, bijv. "46. Permutations".
    while (-not $Number) {
        $a = Read-Answer 'Opgave (bijv. "46. Permutations")'
        $m = [regex]::Match($a, '^\s*#?(\d+)\s*\.?\s*(.*)$')
        if ($m.Success -and [int]$m.Groups[1].Value -ge 1 -and [int]$m.Groups[1].Value -le 99999) {
            $Number = [int]$m.Groups[1].Value
            if (-not $Title -and $m.Groups[2].Value.Trim()) { $Title = $m.Groups[2].Value.Trim() }
        } else {
            Write-Host '  Begin met het opgavenummer.' -ForegroundColor Yellow
        }
    }
    while (-not (Test-ValidTitle $Title)) {
        if ($Title) { Write-Host '  Titel bevat tekens die niet in een bestandsnaam mogen.' -ForegroundColor Yellow }
        $Title = Read-Answer 'Titel'
    }

    if (-not $Runtime) { $Runtime = Format-Stat (Read-Answer 'Runtime, bijv. "1ms 93" (Enter = overslaan)') }
    if (-not $Memory) { $Memory = Format-Stat (Read-Answer 'Memory, bijv. "44MB 70" (Enter = overslaan)') }
} elseif (-not (Test-ValidTitle $Title)) {
    throw "Titel bevat tekens die niet in een bestandsnaam mogen: '$Title'."
}

$fileName = '{0:D5}. {1}.java' -f $Number, $Title
$target = Join-Path $root $fileName

$sameNumber = @(Get-ChildItem -LiteralPath $root -File -Filter ('{0:D5}.*' -f $Number))
if ($sameNumber.Count -gt 0 -and -not $Force) {
    $existing = ($sameNumber | ForEach-Object Name) -join ', '
    if (-not $interactive) { throw "Er bestaat al een bestand voor #${Number}: $existing. Gebruik -Force om te overschrijven." }
    if (-not (Read-YesNo "Er bestaat al: $existing. Overschrijven?" $false)) {
        Write-Host 'Afgebroken; er is niets gewijzigd.'
        return
    }
}

if ($interactive) {
    Write-Host ''
    Write-Host "  Bestand : $fileName"
    Write-Host "  Link    : https://leetcode.com/problems/$(Get-Slug $Title)/"
    if ($Runtime) { Write-Host "  Runtime : $Runtime" }
    if ($Memory) { Write-Host "  Memory  : $Memory" }
    Write-Host ''
    if (-not (Read-YesNo 'Doorgaan?' $true)) {
        Write-Host 'Afgebroken; er is niets gewijzigd.'
        return
    }
}

# --- Schrijven -----------------------------------------------------------------

$removed = @($sameNumber | Where-Object { $_.FullName -ne $target })
$removed | Remove-Item

$out = New-Object System.Collections.Generic.List[string]
if ($Runtime) { $out.Add("// Runtime $Runtime") }
if ($Memory) { $out.Add("// Memory $Memory") }
if ($body -match '\bListNode\b') {
    $out.AddRange([string[]] @(
        '/**',
        ' * Definition for singly-linked list.',
        ' * public class ListNode {',
        ' *     int val;',
        ' *     ListNode next;',
        ' *     ListNode() {}',
        ' *     ListNode(int val) { this.val = val; }',
        ' *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }',
        ' * }',
        ' */'))
}
if ($body -match '\bTreeNode\b') {
    $out.AddRange([string[]] @(
        '/**',
        ' * Definition for a binary tree node.',
        ' * public class TreeNode {',
        ' *     int val;',
        ' *     TreeNode left;',
        ' *     TreeNode right;',
        ' *     TreeNode() {}',
        ' *     TreeNode(int val) { this.val = val; }',
        ' *     TreeNode(int val, TreeNode left, TreeNode right) {',
        ' *         this.val = val;',
        ' *         this.left = left;',
        ' *         this.right = right;',
        ' *     }',
        ' * }',
        ' */'))
}
$out.Add('class Solution {')
$out.Add(($body -replace '\r?\n', $nl))
$out.Add('}')
[System.IO.File]::WriteAllText($target, ($out -join $nl) + $nl, $utf8NoBom)
Write-Host "Gearchiveerd: $fileName"

if (-not $NoReset) {
    Copy-Item -LiteralPath $templatePath -Destination $solutionPath -Force
    Write-Host 'boilerplate/Solution.java gereset naar de template.'
}

Update-Index

# --- Commit --------------------------------------------------------------------

$defaultMessage = "Voegt oplossing voor $Title toe"
$paths = @($target, $readmePath) + @($removed | ForEach-Object FullName)
if (-not $NoReset) { $paths += $solutionPath }

if ($interactive -and (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host ''
    if (Read-YesNo 'Nu committen?' $true) {
        $message = Read-Answer 'Commitbericht' $defaultMessage
        Push-Location $root
        try {
            git add -A -- $paths
            git commit -m $message -- $paths
            if ($LASTEXITCODE -ne 0) { throw 'git commit is mislukt.' }
        } finally {
            Pop-Location
        }
        return
    }
}

Write-Host ''
Write-Host 'Volgende stap:'
Write-Host '  git add -A'
Write-Host "  git commit -m `"$defaultMessage`""
