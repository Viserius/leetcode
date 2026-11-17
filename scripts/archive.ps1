<#
.SYNOPSIS
    Archiveert de oplossing uit boilerplate/Solution.java als "NNNNN. Titel.java",
    reset de boilerplate en werkt de index in README.md bij.

.DESCRIPTION
    Neemt de code tussen "// region solution" en "// endregion" uit boilerplate/Solution.java,
    wikkelt die in "class Solution { ... }" (precies wat je op LeetCode indient) en schrijft dat
    naar de root. Gebruikt de oplossing ListNode/TreeNode, dan wordt de LeetCode-definitie als
    commentaar toegevoegd. Commit doe je daarna zelf.

.EXAMPLE
    ./scripts/archive.ps1 -Number 46 -Title "Permutations" -Runtime "1ms beats 93%" -Memory "44MB beats 70%"

.EXAMPLE
    ./scripts/archive.ps1 -IndexOnly
#>
[CmdletBinding(DefaultParameterSetName = 'Archive')]
param(
    [Parameter(Mandatory, ParameterSetName = 'Archive', Position = 0)]
    [ValidateRange(1, 99999)]
    [int] $Number,

    [Parameter(Mandatory, ParameterSetName = 'Archive', Position = 1)]
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

function Get-Slug([string] $text) {
    $slug = $text.ToLowerInvariant() -replace '[^a-z0-9 \-]', '' -replace '\s+', '-' -replace '-+', '-'
    return $slug.Trim('-')
}

function Get-RegionBody([string] $source) {
    $m = [regex]::Match($source, '(?s)//\s*region solution[^\n]*\n(.*?)\r?\n[ \t]*//\s*endregion')
    if (-not $m.Success) { throw "Geen '// region solution' ... '// endregion' gevonden." }
    return $m.Groups[1].Value.TrimEnd()
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

# --- Archiveren ---------------------------------------------------------------

if ($Title.IndexOfAny([System.IO.Path]::GetInvalidFileNameChars()) -ge 0) {
    throw "Titel bevat tekens die niet in een bestandsnaam mogen: '$Title'. Pas de titel aan (de README-link gebruikt de slug)."
}

$body = Get-RegionBody ([System.IO.File]::ReadAllText($solutionPath))
$templateBody = Get-RegionBody ([System.IO.File]::ReadAllText($templatePath))
if ($body.Trim() -eq $templateBody.Trim()) {
    throw 'De oplossing is nog gelijk aan de template; niets te archiveren.'
}

$fileName = '{0:D5}. {1}.java' -f $Number, $Title
$target = Join-Path $root $fileName

$sameNumber = Get-ChildItem -LiteralPath $root -File -Filter ('{0:D5}.*' -f $Number)
if ($sameNumber -and -not $Force) {
    throw "Er bestaat al een bestand voor #${Number}: $($sameNumber.Name). Gebruik -Force om te overschrijven."
}
if ($Force) { $sameNumber | Where-Object { $_.FullName -ne $target } | Remove-Item }

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

Write-Host ''
Write-Host 'Volgende stap:'
Write-Host "  git add -A"
Write-Host "  git commit -m `"Voegt oplossing voor $Title toe`""
