import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Werkbank voor de huidige LeetCode-opgave. Alle test- en parse-helpers komen uit {@link Harness}.
 *
 * <ol>
 *   <li>Plak de LeetCode-code (signatuur) tussen de region-markers.</li>
 *   <li>Plak de voorbeelden uit de opgave in {@code examples(...)}; voeg eigen edge cases toe met {@code test(...)}.</li>
 *   <li>Run deze klasse. Alleen de code tussen de markers gaat naar LeetCode.</li>
 *   <li>Archiveer en commit: {@code ./scripts/archive.ps1} (vraagt alles interactief).</li>
 * </ol>
 */
class Solution extends Harness {

    void main() {
        examples("""
                Input: nums = [1,2,3]
                Output: 6
                """);

        // Eigen cases: test(label, argumenten..., verwacht, methode)
        // test("leeg", ints("[]"), 0, this::solve);
    }

    // region solution
    public int solve(int[] nums) {
        return 0;
    }
    // endregion
}
