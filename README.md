# LeetCode

Mijn LeetCode-oplossingen in Java, op volgorde van opgavenummer, plus een lokale **boilerplate/harness**
om opgaven in IntelliJ op te lossen en te testen: LeetCode-voorbeelden plakken, inputs parsen,
PASS/FAIL met duidelijke diffs, timeouts en stacktraces naar je eigen regel code.

## Structuur

```
.
├── 00001. Two Sum.java          # gearchiveerde oplossingen: precies wat op LeetCode is ingediend
├── ...
├── boilerplate/                 # IntelliJ source root (JDK 25)
│   ├── Solution.java            # werkbank voor de huidige opgave (klein, wordt na elke opgave gereset)
│   ├── Harness.java             # test-runner, matchers en parsers (hoeft niet aangepast te worden)
│   ├── ListNode.java            # identiek aan de LeetCode-definitie
│   └── TreeNode.java            # identiek aan de LeetCode-definitie
├── templates/Solution.java.tmpl # schone versie van boilerplate/Solution.java
└── scripts/archive.ps1          # archiveert de huidige oplossing en werkt de index bij
```

**Naamconventie:** `NNNNN. <exacte LeetCode-titel>.java` (5 cijfers, punt, spatie).
Het bestand bevat letterlijk de ingediende `class Solution { ... }`, optioneel met een header:

```java
// Runtime 2ms beats 60.29%
// Memory 42.9MB beats 88.97%
```

De gearchiveerde bestanden staan bewust *buiten* de source root: ze heten allemaal `Solution` en
gebruiken de imports die LeetCode automatisch toevoegt, dus ze compileren niet samen.

## Setup (IntelliJ)

1. Open de map als project; de module `leetcode` heeft `boilerplate/` als source root.
2. Project SDK: **JDK 25** (`openjdk-25`). `Solution` gebruikt een instance `void main()` (JEP 512).
3. Run `Solution` via het groene pijltje naast `void main()`.

Zonder IDE:

```bash
javac -d out boilerplate/*.java
```

```bash
java -cp out Solution
```

## Workflow

1. **Opgave openen** — kopieer de methode-signatuur van LeetCode tussen `// region solution` en
   `// endregion` in `boilerplate/Solution.java` (vervang de placeholder `solve`).
2. **Voorbeelden plakken** — kopieer de "Example"-blokken uit de opgave letterlijk in `examples(...)`.
3. **Eigen edge cases** toevoegen met `test(...)`.
4. **Runnen** tot alles ✅ is, dan de code tussen de markers op LeetCode indienen.
5. **Archiveren**:

   ```powershell
   ./scripts/archive.ps1 -Number 46 -Title "Permutations" -Runtime "1ms beats 93%" -Memory "44MB beats 70%"
   ```

   Dit schrijft `00046. Permutations.java`, reset `boilerplate/Solution.java` naar de template en
   werkt de index onderaan deze README bij. (`-NoReset` om de boilerplate te laten staan, `-Force`
   om een bestaand bestand te overschrijven, `-IndexOnly` om alleen de index te regenereren.)
6. **Committen** — één commit per opgave, berichtstijl zoals de bestaande historie:

   ```bash
   git add -A
   ```

   ```bash
   git commit -m "Voegt oplossing voor Permutations toe"
   ```

## Harness cheat-sheet

Een typische `boilerplate/Solution.java`:

```java
class Solution extends Harness {

    void main() {
        examples("""
                Example 1:
                Input: nums = [2,7,11,15], target = 9
                Output: [0,1]
                Explanation: Because nums[0] + nums[1] == 9, we return [0, 1].

                Example 2:
                Input: nums = [3,2,4], target = 6
                Output: [1,2]
                """);

        test("negatief", ints("[-3,4,3,90]"), 0, ints("[0,2]"), this::twoSum);
    }

    // region solution
    public int[] twoSum(int[] nums, int target) { ... }
    // endregion
}
```

Output:

```
✅ PASS  twoSum · Example 1  (8.49 ms)
✅ PASS  twoSum · Example 2  (50.2 µs)
❌ FAIL  negatief  (19.1 µs)
    expected: [0, 2]
      actual: [2, 0]
        diff: at char 1: …[0, 2]…  vs  …[2, 0]…

==================================================
❌ Summary: 2 passed, 1 failed, 3 total
Total time: 8.56 ms
==================================================
```

### Voorbeelden plakken: `examples`

| Aanroep | Gebruik |
|---|---|
| `examples(text)` | Draait alle `Input:`/`Output:`-blokken tegen de enige publieke methode. `Explanation:` en andere tekst worden genegeerd. |
| `examples("methodName", text)` | Als `Solution` meerdere publieke methodes heeft. |
| `examples(text, Harness::anyOrder)` | Wikkelt elke Output in een matcher (zie hieronder). |
| `show(rawArgs)` | Draait de methode met waarden in het *Testcase*-formaat van LeetCode (één per regel) en print het resultaat zonder te vergelijken. |

Parameters worden via reflectie omgezet naar het juiste type: `int`/`long`/`double`/`boolean`/`char`/`String`,
arrays daarvan (ook 2D, bijv. `char[][]` voor Sudoku), geneste `List<...>`, `ListNode` en `TreeNode`.
Bij een **void**-methode (in-place opgaven zoals Rotate Image) wordt het eerste argument na de aanroep met de Output vergeleken.

### Type-safe tests

| Methode | Gebruik |
|---|---|
| `test(label, expected, () -> ...)` | Geen argumenten / vrije expressie. |
| `test(label, a, [b, c, d,] expected, this::method)` | 1–4 argumenten. |
| `test(label, () -> ints("[...]"), expected, this::method)` | Verse input per run (als de methode de input muteert). |
| `testInPlace(label, () -> input, expectedAfter, this::method)` | Void-methode die de input aanpast. |
| `testThrows(label, SomeException.class, () -> ...)` | Verwacht een exceptie. |
| `check(label, () -> actual, actual -> boolean)` | Eigen judge, bijv. #26 Remove Duplicates (return `k` + eerste `k` elementen). |

### Matchers (als `expected`)

| Matcher | Wanneer |
|---|---|
| `anyOrder(x)` | Volgorde van de buitenste lijst maakt niet uit (3Sum, Permutations). Toont bij falen wat ontbreekt en wat onverwacht is. |
| `anyOrderDeep(x)` | Volgorde maakt op geen enkel niveau uit (Combination Sum, Group Anagrams). |
| `oneOf(a, b, ...)` | Meerdere geldige antwoorden (Longest Palindromic Substring: `"bab"` of `"aba"`). |
| `approx(v[, eps])` | Getal binnen tolerantie. `double`/`float` worden standaard al met `epsilon = 1e-5` vergeleken. |

Daarnaast worden `int`/`long` op waarde vergeleken (`25` == `25L`), alle arrays diep, en `ListNode`/`TreeNode` structureel.

### Parsers (plak LeetCode-literals)

| Helper | Resultaat |
|---|---|
| `ints("[1,2,3]")`, `longs`, `doubles`, `bools` | `int[]`, `long[]`, `double[]`, `boolean[]` |
| `chars("[\"a\",\"b\"]")` of `chars("ab")` | `char[]` |
| `strings("[\"eat\",\"tea\"]")` | `String[]` |
| `intMatrix("[[1,2],[3,4]]")`, `charMatrix(...)`, `stringMatrix(...)` | `int[][]`, `char[][]`, `String[][]` |
| `intList`, `intLists("[[2,2,3],[7]]")`, `stringList`, `stringLists` | `List<Integer>`, `List<List<Integer>>`, ... |
| `listNode("[1,2,3]")` / `listNode(1, 2, 3)` | `ListNode` |
| `tree("[1,null,2,3]")` / `tree(1, null, 2, 3)` | `TreeNode` (level-order, zoals LeetCode) |
| `parseAs(literal, type)` | Elk ondersteund type, bijv. `parseAs(s, long[][].class)` |
| `listNodeToList(head)`, `treeToList(root)` | Terug naar een lijst |
| `log(...)`, `pretty(x)` | Debug-printen in LeetCode-stijl |

### Instellingen

Overschrijf ze bovenaan `main()`, bijv. `timeoutMs = 10_000;`.

| Instelling | Default | Betekenis |
|---|---|---|
| `timeoutMs` | `2000` | Max looptijd per test (0 = onbeperkt). Staat automatisch uit tijdens debuggen. |
| `stopOnFirstFail` | `false` | Overige tests worden na de eerste FAIL overgeslagen. |
| `showTiming` | `true` | Looptijd per test en totaal. |
| `exitNonZeroOnFail` | `false` | Exit code 1 bij falende tests. |
| `epsilon` | `1e-5` | Tolerantie voor `double`/`float`. |
| `stackSizeBytes` | `64 MB` | Stack van de testthread (diepe recursie). |
| `maxTraceFrames` | `8` | Aantal stackframes uit je eigen code bij een exceptie. |

### Goed om te weten

- Elke test draait in een eigen thread; een oneindige loop wordt na `timeoutMs` als FAIL gemeld en de overige tests lopen door.
- De eerste test is trager door class loading/JIT; gebruik de timing alleen als grove indicatie.
- `examples` ondersteunt geen *design*-opgaven (zoals LRU Cache met `["LRUCache","put",...]`); test die met `test(...)` of `check(...)`.
- Namen uit de harness (`test`, `ints`, `tree`, ...) zijn geërfd; geef je eigen helpers geen identieke signatuur.

## Opgeloste opgaven

<!-- INDEX:START -->
45 opgaven opgelost.

| # | Opgave | Oplossing |
|--:|--------|-----------|
| 1 | [Two Sum](https://leetcode.com/problems/two-sum/) | [Java](00001.%20Two%20Sum.java) |
| 2 | [Add Two Numbers](https://leetcode.com/problems/add-two-numbers/) | [Java](00002.%20Add%20Two%20Numbers.java) |
| 3 | [Longest Substring Without Repeating Characters](https://leetcode.com/problems/longest-substring-without-repeating-characters/) | [Java](00003.%20Longest%20Substring%20Without%20Repeating%20Characters.java) |
| 4 | [Median of Two Sorted Arrays](https://leetcode.com/problems/median-of-two-sorted-arrays/) | [Java](00004.%20Median%20of%20Two%20Sorted%20Arrays.java) |
| 5 | [Longest Palindromic Substring](https://leetcode.com/problems/longest-palindromic-substring/) | [Java](00005.%20Longest%20Palindromic%20Substring.java) |
| 6 | [Zigzag Conversion](https://leetcode.com/problems/zigzag-conversion/) | [Java](00006.%20Zigzag%20Conversion.java) |
| 7 | [Reverse Integer](https://leetcode.com/problems/reverse-integer/) | [Java](00007.%20Reverse%20Integer.java) |
| 8 | [String to Integer (atoi)](https://leetcode.com/problems/string-to-integer-atoi/) | [Java](00008.%20String%20to%20Integer%20%28atoi%29.java) |
| 9 | [Palindrome Number](https://leetcode.com/problems/palindrome-number/) | [Java](00009.%20Palindrome%20Number.java) |
| 10 | [Regular Expression Matching](https://leetcode.com/problems/regular-expression-matching/) | [Java](00010.%20Regular%20Expression%20Matching.java) |
| 11 | [Container With Most Water](https://leetcode.com/problems/container-with-most-water/) | [Java](00011.%20Container%20With%20Most%20Water.java) |
| 12 | [Integer to Roman](https://leetcode.com/problems/integer-to-roman/) | [Java](00012.%20Integer%20to%20Roman.java) |
| 13 | [Roman to Integer](https://leetcode.com/problems/roman-to-integer/) | [Java](00013.%20Roman%20to%20Integer.java) |
| 14 | [Longest Common Prefix](https://leetcode.com/problems/longest-common-prefix/) | [Java](00014.%20Longest%20Common%20Prefix.java) |
| 15 | [3Sum](https://leetcode.com/problems/3sum/) | [Java](00015.%203Sum.java) |
| 16 | [3Sum Closest](https://leetcode.com/problems/3sum-closest/) | [Java](00016.%203Sum%20Closest.java) |
| 17 | [Letter Combinations of a Phone Number](https://leetcode.com/problems/letter-combinations-of-a-phone-number/) | [Java](00017.%20Letter%20Combinations%20of%20a%20Phone%20Number.java) |
| 18 | [4Sum](https://leetcode.com/problems/4sum/) | [Java](00018.%204Sum.java) |
| 19 | [Remove Nth Node From End of List](https://leetcode.com/problems/remove-nth-node-from-end-of-list/) | [Java](00019.%20Remove%20Nth%20Node%20From%20End%20of%20List.java) |
| 20 | [Valid Parentheses](https://leetcode.com/problems/valid-parentheses/) | [Java](00020.%20Valid%20Parentheses.java) |
| 21 | [Merge Two Sorted Lists](https://leetcode.com/problems/merge-two-sorted-lists/) | [Java](00021.%20Merge%20Two%20Sorted%20Lists.java) |
| 22 | [Generate Parentheses](https://leetcode.com/problems/generate-parentheses/) | [Java](00022.%20Generate%20Parentheses.java) |
| 23 | [Merge k Sorted Lists](https://leetcode.com/problems/merge-k-sorted-lists/) | [Java](00023.%20Merge%20k%20Sorted%20Lists.java) |
| 24 | [Swap Nodes in Pairs](https://leetcode.com/problems/swap-nodes-in-pairs/) | [Java](00024.%20Swap%20Nodes%20in%20Pairs.java) |
| 25 | [Reverse Nodes in k-Group](https://leetcode.com/problems/reverse-nodes-in-k-group/) | [Java](00025.%20Reverse%20Nodes%20in%20k-Group.java) |
| 26 | [Remove Duplicates from Sorted Array](https://leetcode.com/problems/remove-duplicates-from-sorted-array/) | [Java](00026.%20Remove%20Duplicates%20from%20Sorted%20Array.java) |
| 27 | [Remove Element](https://leetcode.com/problems/remove-element/) | [Java](00027.%20Remove%20Element.java) |
| 28 | [Find the Index of the First Occurrence in a String](https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/) | [Java](00028.%20Find%20the%20Index%20of%20the%20First%20Occurrence%20in%20a%20String.java) |
| 29 | [Divide Two Integers](https://leetcode.com/problems/divide-two-integers/) | [Java](00029.%20Divide%20Two%20Integers.java) |
| 30 | [Substring with Concatenation of All Words](https://leetcode.com/problems/substring-with-concatenation-of-all-words/) | [Java](00030.%20Substring%20with%20Concatenation%20of%20All%20Words.java) |
| 31 | [Next Permutation](https://leetcode.com/problems/next-permutation/) | [Java](00031.%20Next%20Permutation.java) |
| 32 | [Longest Valid Parentheses](https://leetcode.com/problems/longest-valid-parentheses/) | [Java](00032.%20Longest%20Valid%20Parentheses.java) |
| 33 | [Search in Rotated Sorted Array](https://leetcode.com/problems/search-in-rotated-sorted-array/) | [Java](00033.%20Search%20in%20Rotated%20Sorted%20Array.java) |
| 34 | [Find First and Last Position of Element in Sorted Array](https://leetcode.com/problems/find-first-and-last-position-of-element-in-sorted-array/) | [Java](00034.%20Find%20First%20and%20Last%20Position%20of%20Element%20in%20Sorted%20Array.java) |
| 35 | [Search Insert Position](https://leetcode.com/problems/search-insert-position/) | [Java](00035.%20Search%20Insert%20Position.java) |
| 36 | [Valid Sudoku](https://leetcode.com/problems/valid-sudoku/) | [Java](00036.%20Valid%20Sudoku.java) |
| 37 | [Sudoku Solver](https://leetcode.com/problems/sudoku-solver/) | [Java](00037.%20Sudoku%20Solver.java) |
| 38 | [Count and Say](https://leetcode.com/problems/count-and-say/) | [Java](00038.%20Count%20and%20Say.java) |
| 39 | [Combination Sum](https://leetcode.com/problems/combination-sum/) | [Java](00039.%20Combination%20Sum.java) |
| 40 | [Combination Sum II](https://leetcode.com/problems/combination-sum-ii/) | [Java](00040.%20Combination%20Sum%20II.java) |
| 41 | [First Missing Positive](https://leetcode.com/problems/first-missing-positive/) | [Java](00041.%20First%20Missing%20Positive.java) |
| 42 | [Trapping Rain Water](https://leetcode.com/problems/trapping-rain-water/) | [Java](00042.%20Trapping%20Rain%20Water.java) |
| 43 | [Multiply Strings](https://leetcode.com/problems/multiply-strings/) | [Java](00043.%20Multiply%20Strings.java) |
| 44 | [Wildcard Matching](https://leetcode.com/problems/wildcard-matching/) | [Java](00044.%20Wildcard%20Matching.java) |
| 45 | [Jump Game II](https://leetcode.com/problems/jump-game-ii/) | [Java](00045.%20Jump%20Game%20II.java) |
<!-- INDEX:END -->
