import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LeetCode local runner / boilerplate (Java 25).
 * <p>
 * Doel:
 * - Snelle PASS/FAIL tests met deep compare
 * - Handige helpers voor typische LeetCode types (arrays, lists, ListNode, TreeNode)
 * - Ondersteuning voor in-place/void methods (testInPlace)
 * <p>
 * Tip:
 * - Gebruik Supplier-varianten als je methode input muteert (arrays/matrices/lists).
 */
class Solution {

    // ======= Runner settings (pas aan naar smaak) ==========================
    private static final boolean STOP_ON_FIRST_FAIL = false;
    private static final boolean SHOW_TIMING_PER_TEST = true;
    private static final boolean PRINT_FINAL_SUMMARY = true;
    private static final boolean EXIT_NONZERO_ON_FAIL = false; // handig in CI / scripts

    public static void main(String[] args) {
        var s = new Solution();

        s.suite(() -> {
            // --- Example tests (replace with your own) --------------------------
            // Primitive types example:
//            s.test("n=1", 1, "1", s::countAndSay);
//            s.test("n=2", 2, "11", s::countAndSay);
//            s.test("n=3", 3, "21", s::countAndSay);
//            s.test("n=4", 4, "1211", s::countAndSay);

            // Array-returning example:
//             s.test("twoSum", arr(2,7,11,15), 9, arr(0,1), (a, t) -> s.twoSum(a, t));

            // Supplier variant (fresh input each test; useful when input is mutated):
//             s.test("rotate", () -> parseIntMatrix("[[1,2],[3,4]]"), parseIntMatrix("[[3,1],[4,2]]"), m -> { s.rotate(m); return m; });

            // In-place void example:
//             s.testInPlace("rotate in-place",
//                     () -> parseIntMatrix("[[1,2],[3,4]]"),
//                     parseIntMatrix("[[3,1],[4,2]]"),
//                     m -> s.rotate(m));

            // Linked list example:
//             s.test("reverseList",
//                     listNode(1,2,3),
//                     listNode(3,2,1),
//                     s::reverseList);

            // Exception example:
//             s.testThrows("invalid input throws", IllegalArgumentException.class, () -> s.someMethod(-1));
        });
    }

    // ======================================================================
    // ✅ Your LeetCode solution methods go below (replace per problem)
    // ======================================================================


    // ======================================================================
    // 🧪 Test harness
    // ======================================================================

    private static final class TestStats {
        int passed = 0;
        int failed = 0;
        long totalNanos = 0;
        int total = 0;
    }

    private static final TestStats STATS = new TestStats();

    /**
     * Groepeer tests en print eindsummary.
     */
    private void suite(Runnable tests) {
        try {
            tests.run();
        } finally {
            if (PRINT_FINAL_SUMMARY) printFinalSummary();
            if (EXIT_NONZERO_ON_FAIL && STATS.failed > 0) System.exit(1);
        }
    }

    // ---- Standard tests (0..3 args) --------------------------------------

    public <R> void test(String name, R expected, Supplier<R> fn) {
        runTest(name, expected, fn);
    }

    public <A, R> void test(String name, A a, R expected, Function<A, R> fn) {
        runTest(name, expected, () -> fn.apply(a));
    }

    public <A, B, R> void test(String name, A a, B b, R expected, BiFunction<A, B, R> fn) {
        runTest(name, expected, () -> fn.apply(a, b));
    }

    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    public <A, B, C, R> void test(String name, A a, B b, C c, R expected, TriFunction<A, B, C, R> fn) {
        runTest(name, expected, () -> fn.apply(a, b, c));
    }

    // ---- Supplier variants (fresh input per run; useful when input mutates) ----

    public <A, R> void test(String name, Supplier<A> aFactory, R expected, Function<A, R> fn) {
        runTest(name, expected, () -> fn.apply(aFactory.get()));
    }

    public <A, B, R> void test(String name, Supplier<A> aFactory, Supplier<B> bFactory, R expected, BiFunction<A, B, R> fn) {
        runTest(name, expected, () -> fn.apply(aFactory.get(), bFactory.get()));
    }

    // ---- In-place / void method tests ------------------------------------

    public <A> void testInPlace(String name, Supplier<A> aFactory, A expectedAfter, Consumer<A> fn) {
        runTest(name, expectedAfter, () -> {
            A a = aFactory.get();
            fn.accept(a);
            return a;
        });
    }

    public void testThrows(String name, Class<? extends Throwable> expectedType, Runnable fn) {
        STATS.total++;
        long t0 = System.nanoTime();
        try {
            fn.run();
            recordFail(name, "Expected exception: " + expectedType.getSimpleName() + " but nothing was thrown.", t0);
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                recordPass(name, "Threw " + t.getClass().getSimpleName() + " (as expected)", t0);
            } else {
                recordFail(name,
                        "Expected " + expectedType.getSimpleName() + " but threw " + t.getClass().getSimpleName()
                                + ": " + t.getMessage(), t0);
            }
        }
    }

    private <R> void runTest(String name, R expected, Supplier<R> invoke) {
        STATS.total++;
        long t0 = System.nanoTime();
        try {
            R actual = invoke.get();
            if (deepEquals(expected, actual)) {
                recordPass(name, null, t0);
            } else {
                String exp = pretty(expected);
                String act = pretty(actual);
                recordFail(name,
                        "Mismatch\nexpected: " + exp +
                                "\n  actual: " + act +
                                "\n    diff: " + firstDiff(exp, act),
                        t0);
            }
        } catch (Throwable t) {
            recordFail(name, "Threw exception: " + t.getClass().getSimpleName() + ": " + t.getMessage(), t0);
        }

        if (STOP_ON_FIRST_FAIL && STATS.failed > 0) {
            printFinalSummary();
            throw new RuntimeException("Stopped on first failure.");
        }
    }

    private static void recordPass(String name, String extra, long startNanos) {
        long dt = System.nanoTime() - startNanos;
        STATS.totalNanos += dt;
        STATS.passed++;

        if (SHOW_TIMING_PER_TEST) {
            System.out.println("✅ PASS  " + name + "  (" + formatNanos(dt) + ")" + (extra == null ? "" : " — " + extra));
        } else {
            System.out.println("✅ PASS  " + name + (extra == null ? "" : " — " + extra));
        }
    }

    private static void recordFail(String name, String reason, long startNanos) {
        long dt = System.nanoTime() - startNanos;
        STATS.totalNanos += dt;
        STATS.failed++;

        if (SHOW_TIMING_PER_TEST) {
            System.out.println("❌ FAIL  " + name + "  (" + formatNanos(dt) + ")");
        } else {
            System.out.println("❌ FAIL  " + name);
        }
        System.out.println(indent(reason, 2));
        System.out.println();
    }

    private static void printFinalSummary() {
        System.out.println("==================================================");
        System.out.println("Summary: " + STATS.passed + " passed, " + STATS.failed + " failed, " + STATS.total + " total");
        System.out.println("Total time: " + formatNanos(STATS.totalNanos));
        System.out.println("==================================================");
        System.out.println();
    }

    private static String formatNanos(long nanos) {
        if (nanos < 1_000) return nanos + " ns";
        if (nanos < 1_000_000) return (nanos / 1_000.0) + " µs";
        if (nanos < 1_000_000_000) return (nanos / 1_000_000.0) + " ms";
        return (nanos / 1_000_000_000.0) + " s";
    }

    // ======================================================================
    // 🔍 Deep equals + pretty printing
    // ======================================================================

    /**
     * Deep equality:
     * - primitive arrays, Object[] (incl nested), Lists, Maps, Sets
     * - fallback: Objects.equals
     */
    @SuppressWarnings({"rawtypes"})
    private static boolean deepEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        // Arrays (including multi-dim via Object[])
        if (a.getClass().isArray() && b.getClass().isArray()) {
            return arrayDeepEquals(a, b);
        }

        // Lists (ordered)
        if (a instanceof List la && b instanceof List lb) {
            if (la.size() != lb.size()) return false;
            for (int i = 0; i < la.size(); i++) {
                if (!deepEquals(la.get(i), lb.get(i))) return false;
            }
            return true;
        }

        // Sets (order independent)
        if (a instanceof Set sa && b instanceof Set sb) {
            if (sa.size() != sb.size()) return false;
            // Best effort: canonicalize via pretty for deep elements.
            // (Kan in zeldzame gevallen collisions geven, maar is praktisch voor LeetCode debug.)
            var pa = sa.stream().map(Solution::pretty).sorted().toList();
            var pb = sb.stream().map(Solution::pretty).sorted().toList();
            return pa.equals(pb);
        }

        // Maps (best effort: key equality via equals; meestal prima in LeetCode)
        if (a instanceof Map ma && b instanceof Map mb) {
            if (ma.size() != mb.size()) return false;
            for (Object k : ma.keySet()) {
                if (!mb.containsKey(k)) return false;
                if (!deepEquals(ma.get(k), mb.get(k))) return false;
            }
            return true;
        }

        // ListNode / TreeNode support (handig; compare structure/values)
        if (a instanceof ListNode la && b instanceof ListNode lb) return deepEquals(toList(la), toList(lb));
        if (a instanceof TreeNode ta && b instanceof TreeNode tb)
            return pretty(ta).equals(pretty(tb)); // canonical level-order

        return Objects.equals(a, b);
    }

    private static boolean arrayDeepEquals(Object a, Object b) {
        if (a instanceof int[] aa && b instanceof int[] bb) return Arrays.equals(aa, bb);
        if (a instanceof long[] aa && b instanceof long[] bb) return Arrays.equals(aa, bb);
        if (a instanceof char[] aa && b instanceof char[] bb) return Arrays.equals(aa, bb);
        if (a instanceof byte[] aa && b instanceof byte[] bb) return Arrays.equals(aa, bb);
        if (a instanceof short[] aa && b instanceof short[] bb) return Arrays.equals(aa, bb);
        if (a instanceof boolean[] aa && b instanceof boolean[] bb) return Arrays.equals(aa, bb);
        if (a instanceof float[] aa && b instanceof float[] bb) return Arrays.equals(aa, bb);
        if (a instanceof double[] aa && b instanceof double[] bb) return Arrays.equals(aa, bb);

        // Object arrays (incl nested int[][] which is Object[] of int[])
        if (a instanceof Object[] aa && b instanceof Object[] bb) {
            if (aa.length != bb.length) return false;
            for (int i = 0; i < aa.length; i++) {
                if (!deepEquals(aa[i], bb[i])) return false;
            }
            return true;
        }
        return false;
    }

    private static String pretty(Object o) {
        if (o == null) return "null";

        if (o instanceof ListNode ln) return pretty(toList(ln));
        if (o instanceof TreeNode tn) return pretty(levelOrder(tn));

        if (o.getClass().isArray()) {
            if (o instanceof int[] a) return Arrays.toString(a);
            if (o instanceof long[] a) return Arrays.toString(a);
            if (o instanceof char[] a) return Arrays.toString(a);
            if (o instanceof byte[] a) return Arrays.toString(a);
            if (o instanceof short[] a) return Arrays.toString(a);
            if (o instanceof boolean[] a) return Arrays.toString(a);
            if (o instanceof float[] a) return Arrays.toString(a);
            if (o instanceof double[] a) return Arrays.toString(a);
            if (o instanceof Object[] a) {
                var parts = new ArrayList<String>(a.length);
                for (Object x : a) parts.add(pretty(x));
                return "[" + String.join(", ", parts) + "]";
            }
        }

        if (o instanceof List<?> l) {
            var parts = new ArrayList<String>(l.size());
            for (Object x : l) parts.add(pretty(x));
            return "[" + String.join(", ", parts) + "]";
        }

        if (o instanceof Set<?> s) {
            var parts = s.stream().map(Solution::pretty).sorted().toList();
            return "{" + String.join(", ", parts) + "}";
        }

        if (o instanceof Map<?, ?> m) {
            var parts = new ArrayList<String>(m.size());
            for (var e : m.entrySet()) {
                parts.add(pretty(e.getKey()) + "=" + pretty(e.getValue()));
            }
            parts.sort(String::compareTo);
            return "{" + String.join(", ", parts) + "}";
        }

        return String.valueOf(o);
    }

    private static String firstDiff(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return "@" + i + " ('" + a.charAt(i) + "' vs '" + b.charAt(i) + "')";
            }
        }
        if (a.length() != b.length()) {
            return "length (" + a.length() + " vs " + b.length() + ")";
        }
        return "(no diff?)";
    }

    private static String indent(String s, int spaces) {
        String pad = " ".repeat(spaces);
        return pad + s.replace("\n", "\n" + pad);
    }

    // ======================================================================
    // 🧰 Handy helpers
    // ======================================================================

    @SafeVarargs
    public static <T> List<T> list(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    public static int[] arr(int... items) {
        return items;
    }

    public static int[][] matrix(int[]... rows) {
        return rows;
    }

    public static void log(Object... parts) {
        var sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(pretty(parts[i]));
        }
        System.out.println(sb);
    }

    // ---- Parsers: super handig voor copy/paste van LeetCode samples --------

    /**
     * Parse "[1,2,3]" -> int[]
     */
    public static int[] parseIntArray(String s) {
        s = s.trim();
        if (s.equals("[]")) return new int[0];
        if (!s.startsWith("[") || !s.endsWith("]")) throw new IllegalArgumentException("Not an array: " + s);
        var nums = new ArrayList<Integer>();
        int i = 1;
        while (i < s.length() - 1) {
            while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == ',')) i++;
            if (i >= s.length() - 1) break;
            int sign = 1;
            if (s.charAt(i) == '-') {
                sign = -1;
                i++;
            }
            int val = 0;
            boolean any = false;
            while (i < s.length() - 1 && Character.isDigit(s.charAt(i))) {
                val = val * 10 + (s.charAt(i) - '0');
                i++;
                any = true;
            }
            if (!any) throw new IllegalArgumentException("Expected number in: " + s);
            nums.add(sign * val);
        }
        int[] out = new int[nums.size()];
        for (int k = 0; k < nums.size(); k++) out[k] = nums.get(k);
        return out;
    }

    /**
     * Parse "[[1,2],[3,4]]" -> int[][]
     */
    public static int[][] parseIntMatrix(String s) {
        s = s.trim();
        if (s.equals("[]")) return new int[0][0];
        if (!s.startsWith("[") || !s.endsWith("]")) throw new IllegalArgumentException("Not a matrix: " + s);

        var rows = new ArrayList<int[]>();
        int i = 0;
        while (i < s.length()) {
            int open = s.indexOf('[', i);
            if (open < 0) break;
            int close = findMatchingBracket(s, open);
            if (close < 0) throw new IllegalArgumentException("Unbalanced brackets: " + s);

            String row = s.substring(open, close + 1);
            // Skip the outermost matrix bracket: the first '[' at index 0 belongs to the matrix itself
            if (open == 0) {
                i = open + 1;
                continue;
            }

            rows.add(parseIntArray(row));
            i = close + 1;
        }
        return rows.toArray(int[][]::new);
    }

    private static int findMatchingBracket(String s, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    // ---- Linked list (ListNode) helpers ----------------------------------

    public static final class ListNode {
        public int val;
        public ListNode next;

        public ListNode(int val) {
            this.val = val;
        }

        public ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public static ListNode listNode(int... values) {
        if (values.length == 0) return null;
        ListNode head = new ListNode(values[0]);
        ListNode cur = head;
        for (int i = 1; i < values.length; i++) {
            cur.next = new ListNode(values[i]);
            cur = cur.next;
        }
        return head;
    }

    public static List<Integer> toList(ListNode head) {
        var out = new ArrayList<Integer>();
        var cur = head;
        int guard = 0; // simpele cycle-guard (geen perfecte detectie, wel handig)
        while (cur != null) {
            out.add(cur.val);
            cur = cur.next;
            if (++guard > 100_000) throw new IllegalStateException("Possible cycle in ListNode.");
        }
        return out;
    }

    // ---- Binary tree (TreeNode) helpers ----------------------------------

    public static final class TreeNode {
        public int val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }

        public TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Build a tree from level-order values (null means missing).
     * Example: tree(1, 2, 3, null, 4)  =>
     * 1
     * / \
     * 2   3
     * \
     * 4
     */
    public static TreeNode tree(Integer... levelOrder) {
        if (levelOrder.length == 0 || levelOrder[0] == null) return null;
        var root = new TreeNode(levelOrder[0]);
        var q = new ArrayDeque<TreeNode>();
        q.add(root);
        int i = 1;
        while (!q.isEmpty() && i < levelOrder.length) {
            var node = q.remove();
            if (i < levelOrder.length) {
                Integer lv = levelOrder[i++];
                if (lv != null) {
                    node.left = new TreeNode(lv);
                    q.add(node.left);
                }
            }
            if (i < levelOrder.length) {
                Integer rv = levelOrder[i++];
                if (rv != null) {
                    node.right = new TreeNode(rv);
                    q.add(node.right);
                }
            }
        }
        return root;
    }

    /**
     * Canonical level-order representation (trims trailing nulls).
     */
    public static List<Integer> levelOrder(TreeNode root) {
        if (root == null) return List.of();
        var out = new ArrayList<Integer>();
        var q = new ArrayDeque<TreeNode>();
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode n = q.remove();
            if (n == null) {
                out.add(null);
            } else {
                out.add(n.val);
                q.add(n.left);
                q.add(n.right);
            }
        }
        // trim trailing nulls
        int end = out.size();
        while (end > 0 && out.get(end - 1) == null) end--;
        return out.subList(0, end);
    }
}