import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Lokale test-harness voor LeetCode-opgaven (Java 25).
 * <p>
 * {@code Solution} erft van deze klasse, zodat alle helpers zonder prefix beschikbaar zijn:
 * <ul>
 *   <li>{@link #examples(String)} — plak de LeetCode-voorbeelden letterlijk ("Input: ... / Output: ...").</li>
 *   <li>{@code test(label, args..., expected, fn)} — type-safe tests (0–4 argumenten).</li>
 *   <li>{@link #testInPlace}, {@link #testThrows}, {@link #check}, {@link #show} — speciale gevallen.</li>
 *   <li>Matchers als expected: {@link #anyOrder}, {@link #anyOrderDeep}, {@link #oneOf}, {@link #approx}.</li>
 *   <li>Parsers voor LeetCode-literals: {@link #ints}, {@link #intMatrix}, {@link #charMatrix}, {@link #strings},
 *       {@link #intLists}, {@link #listNode(String)}, {@link #tree(String)}, ...</li>
 * </ul>
 * Elke test draait in een eigen thread met grote stack en een timeout; de samenvatting wordt
 * automatisch geprint wanneer het programma stopt.
 * <p>
 * Deze file hoeft per opgave niet aangepast te worden. Instellingen kun je vanuit
 * {@code Solution.main()} overschrijven, bijv. {@code timeoutMs = 10_000;}.
 */
abstract class Harness {

    // ======================================================================
    // ⚙️ Instellingen (overschrijfbaar vanuit Solution.main)
    // ======================================================================

    /** Na de eerste FAIL worden overige tests overgeslagen (SKIP). */
    protected static boolean stopOnFirstFail = false;
    /** Toon looptijd per test en in de samenvatting. */
    protected static boolean showTiming = true;
    /** Maximale looptijd per test in ms; 0 = onbeperkt. Staat automatisch uit tijdens debuggen. */
    protected static long timeoutMs = 2_000;
    /** Exit code 1 als er tests falen (handig voor scripts/CI). */
    protected static boolean exitNonZeroOnFail = false;
    /** Tolerantie voor het vergelijken van double/float (LeetCode gebruikt meestal 1e-5). */
    protected static double epsilon = 1e-5;
    /** Stackgrootte van de testthread, zodat diepe recursie niet direct een StackOverflowError geeft. */
    protected static long stackSizeBytes = 64L * 1024 * 1024;
    /** Maximaal aantal stackframes (uit je eigen code) bij een exception. */
    protected static int maxTraceFrames = 8;

    private static final boolean DEBUGGING = ManagementFactory.getRuntimeMXBean()
            .getInputArguments().stream().anyMatch(arg -> arg.contains("jdwp"));

    private static int passed, failed, skipped;
    private static long totalNanos;
    private static boolean stopped;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Harness::printSummary, "harness-summary"));
    }

    // ======================================================================
    // 🧪 Tests
    // ======================================================================

    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    @FunctionalInterface
    public interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    /** {@code test("label", expected, () -> ...)} */
    protected <R> void test(String name, R expected, Supplier<R> fn) {
        runCase(name, expected, fn);
    }

    /** {@code test("label", a, expected, this::method)} */
    protected <A, R> void test(String name, A a, R expected, Function<A, R> fn) {
        runCase(name, expected, () -> fn.apply(a));
    }

    protected <A, B, R> void test(String name, A a, B b, R expected, BiFunction<A, B, R> fn) {
        runCase(name, expected, () -> fn.apply(a, b));
    }

    protected <A, B, C, R> void test(String name, A a, B b, C c, R expected, TriFunction<A, B, C, R> fn) {
        runCase(name, expected, () -> fn.apply(a, b, c));
    }

    protected <A, B, C, D, R> void test(String name, A a, B b, C c, D d, R expected, QuadFunction<A, B, C, D, R> fn) {
        runCase(name, expected, () -> fn.apply(a, b, c, d));
    }

    /** Supplier-variant: verse input per run (handig als de methode de input muteert). */
    protected <A, R> void test(String name, Supplier<A> a, R expected, Function<A, R> fn) {
        runCase(name, expected, () -> fn.apply(a.get()));
    }

    protected <A, B, R> void test(String name, Supplier<A> a, Supplier<B> b, R expected, BiFunction<A, B, R> fn) {
        runCase(name, expected, () -> fn.apply(a.get(), b.get()));
    }

    /**
     * Voor void-methodes die de input aanpassen (Rotate Image, Sudoku Solver, ...):
     * vergelijkt de input <em>na</em> de aanroep met {@code expectedAfter}.
     */
    protected <A> void testInPlace(String name, Supplier<A> input, Object expectedAfter, Consumer<A> fn) {
        runCase(name, expectedAfter, () -> {
            A a = input.get();
            fn.accept(a);
            return a;
        });
    }

    /** Slaagt als {@code fn} een exceptie van (een subtype van) {@code expectedType} gooit. */
    protected void testThrows(String name, Class<? extends Throwable> expectedType, Runnable fn) {
        if (skipIfStopped(name)) return;
        Outcome out = execute(() -> {
            fn.run();
            return null;
        });
        if (out.timedOut()) fail(name, out, timeoutMessage());
        else if (out.error() == null) fail(name, out, "Expected " + expectedType.getSimpleName() + " but nothing was thrown.");
        else if (expectedType.isInstance(out.error())) pass(name, out, "threw " + out.error().getClass().getSimpleName());
        else fail(name, out, "Expected " + expectedType.getSimpleName() + " but threw " + describe(out.error())
                    + "\n" + trace(out.error()));
    }

    /**
     * Eigen controle op het resultaat, voor opgaven met meerdere geldige antwoorden of een "custom judge".
     * <pre>{@code check("k + prefix", () -> { int[] a = ints("[1,1,2]"); int k = removeDuplicates(a);
     *         return Arrays.copyOf(a, k); }, r -> Arrays.equals(r, ints("[1,2]")));}</pre>
     */
    protected <R> void check(String name, Supplier<R> actual, Predicate<R> ok) {
        if (skipIfStopped(name)) return;
        Outcome out = execute(actual);
        if (out.timedOut()) fail(name, out, timeoutMessage());
        else if (out.error() != null) fail(name, out, "Threw " + describe(out.error()) + "\n" + trace(out.error()));
        else {
            boolean good;
            try {
                @SuppressWarnings("unchecked") R value = (R) out.value();
                good = ok.test(value);
            } catch (Throwable t) {
                fail(name, out, "Check itself threw " + describe(t));
                return;
            }
            if (good) pass(name, out, null);
            else fail(name, out, "check failed for actual: " + render(out.value()));
        }
    }

    // ======================================================================
    // 📋 Voorbeelden plakken (reflectie)
    // ======================================================================

    /**
     * Draait alle voorbeelden uit een geplakte LeetCode-beschrijving tegen de enige publieke methode
     * van {@code Solution}. Herkent blokken als:
     * <pre>
     * Input: nums = [2,7,11,15], target = 9
     * Output: [0,1]
     * Explanation: ...   (wordt genegeerd)
     * </pre>
     * Argumenten worden op basis van de parametertypes geconverteerd (int, int[], int[][], char[][], String,
     * List&lt;List&lt;Integer&gt;&gt;, ListNode, TreeNode, ...). Bij een void-methode wordt het eerste argument
     * na de aanroep vergeleken met de Output (in-place opgaven).
     */
    protected void examples(String text) {
        examples(null, text, Function.identity());
    }

    /** Zoals {@link #examples(String)}, maar wikkelt elke Output, bijv. {@code examples(text, Harness::anyOrder)}. */
    protected void examples(String text, Function<Object, Object> expect) {
        examples(null, text, expect);
    }

    /** Expliciete methodenaam, nodig als {@code Solution} meerdere publieke methodes heeft. */
    protected void examples(String methodName, String text) {
        examples(methodName, text, Function.identity());
    }

    protected void examples(String methodName, String text, Function<Object, Object> expect) {
        Method method = findMethod(methodName);
        List<Example> parsed = parseExamples(text);
        for (Example ex : parsed) {
            String name = method.getName() + " · " + ex.label();
            Object[] args;
            Object expected;
            try {
                args = convertArgs(method, ex.args());
                Type outType = method.getReturnType() == void.class
                        ? method.getGenericParameterTypes()[0]
                        : method.getGenericReturnType();
                expected = expect.apply(convert(ex.output(), outType));
            } catch (RuntimeException e) {
                if (skipIfStopped(name)) continue;
                fail(name, new Outcome(null, null, 0, false), "Could not convert example: " + e.getMessage());
                continue;
            }
            runCase(name, expected, () -> invoke(method, args));
        }
    }

    /**
     * Draait de methode met ruwe argumenten (zoals in het "Testcase"-paneel van LeetCode, één waarde per regel)
     * en print het resultaat, zonder te vergelijken. Handig om een eigen testcase te verkennen.
     */
    protected void show(String rawArgs) {
        show(null, rawArgs);
    }

    protected void show(String methodName, String rawArgs) {
        Method method = findMethod(methodName);
        var parser = new LiteralParser(rawArgs, 0);
        Object[] args = convertArgs(method, parseArgList(parser, true));
        String argText = String.join(", ", Arrays.stream(args).map(Harness::pretty).toList());
        Outcome out = execute(() -> invoke(method, args));
        System.out.println("🔎 SHOW  " + method.getName() + "(" + abbreviate(argText, 120) + ")" + timing(out));
        if (out.timedOut()) System.out.println(indent(timeoutMessage(), 4));
        else if (out.error() != null) System.out.println(indent("Threw " + describe(out.error()) + "\n" + trace(out.error()), 4));
        else System.out.println(indent("→ " + render(out.value()), 4));
    }

    private record Example(String label, List<Object> args, Object output) {}

    static List<Example> parseExamples(String text) {
        var out = new ArrayList<Example>();
        int from = 0;
        while (true) {
            int in = text.indexOf("Input:", from);
            if (in < 0) break;
            var p = new LiteralParser(text, in + "Input:".length());
            List<Object> args = parseArgList(p, false);
            int outIdx = text.indexOf("Output:", p.pos);
            if (outIdx < 0) throw new IllegalArgumentException("'Output:' missing after Input at position " + in);
            p.pos = outIdx + "Output:".length();
            Object output = p.value();
            out.add(new Example("Example " + (out.size() + 1), args, output));
            from = p.pos;
        }
        if (out.isEmpty()) throw new IllegalArgumentException("No 'Input:' / 'Output:' blocks found in examples text.");
        return out;
    }

    /**
     * Leest {@code a = 1, b = [2,3]} of {@code 1, [2,3]} (namen optioneel).
     * {@code greedy}: lees door tot het einde, komma's optioneel (Testcase-formaat met newlines).
     */
    private static List<Object> parseArgList(LiteralParser p, boolean greedy) {
        var args = new ArrayList<Object>();
        while (!p.atEnd()) {
            int save = p.pos;
            if (Character.isJavaIdentifierStart(p.peek())) {
                p.word();
                if (p.peekIs('=')) p.pos++;
                else p.pos = save;
            }
            args.add(p.value());
            boolean comma = p.tryConsume(',');
            if (!greedy && !comma) break;
        }
        return args;
    }

    private Method findMethod(String name) {
        var candidates = Arrays.stream(getClass().getDeclaredMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()) && !m.isSynthetic() && !m.isBridge())
                .filter(m -> !m.getName().equals("main"))
                .filter(m -> name == null ? Modifier.isPublic(m.getModifiers()) : m.getName().equals(name))
                .toList();
        if (candidates.size() != 1) {
            String found = candidates.stream().map(Method::getName).toList().toString();
            throw new IllegalStateException(name == null
                    ? "examples()/show() needs exactly one public method in Solution, found " + found
                    + ". Pass the method name: examples(\"methodName\", text)."
                    : "Expected exactly one method named '" + name + "' in Solution, found " + candidates.size() + ".");
        }
        Method m = candidates.getFirst();
        m.setAccessible(true);
        return m;
    }

    private static Object[] convertArgs(Method m, List<Object> raw) {
        Type[] types = m.getGenericParameterTypes();
        if (types.length != raw.size()) {
            throw new IllegalArgumentException(m.getName() + " has " + types.length + " parameter(s) but the example has "
                    + raw.size() + " value(s)");
        }
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            try {
                args[i] = convert(raw.get(i), types[i]);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("argument " + (i + 1) + " (" + types[i].getTypeName() + "): " + e.getMessage(), e);
            }
        }
        return args;
    }

    private Object invoke(Method m, Object[] args) {
        try {
            Object result = m.invoke(this, args);
            return m.getReturnType() == void.class ? args[0] : result;
        } catch (InvocationTargetException e) {
            throw Harness.<RuntimeException>sneaky(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T sneaky(Throwable t) throws T {
        throw (T) t;
    }

    // ======================================================================
    // 🏃 Runner
    // ======================================================================

    private record Outcome(Object value, Throwable error, long nanos, boolean timedOut) {}

    private void runCase(String name, Object expected, Supplier<?> invocation) {
        if (skipIfStopped(name)) return;
        Outcome out = execute(invocation);
        if (out.timedOut()) fail(name, out, timeoutMessage());
        else if (out.error() != null) fail(name, out, "Threw " + describe(out.error()) + "\n" + trace(out.error()));
        else if (deepEquals(expected, out.value())) pass(name, out, null);
        else fail(name, out, mismatch(expected, out.value()));
    }

    /** Voert de aanroep uit in een aparte thread (grote stack + timeout); meet alleen de aanroep zelf. */
    private static Outcome execute(Supplier<?> invocation) {
        Object[] value = new Object[1];
        Throwable[] error = new Throwable[1];
        long[] nanos = new long[1];
        Thread worker = new Thread(null, () -> {
            long t0 = System.nanoTime();
            try {
                value[0] = invocation.get();
            } catch (Throwable t) {
                error[0] = t;
            }
            nanos[0] = System.nanoTime() - t0;
        }, "solution", stackSizeBytes);
        worker.setDaemon(true); // een vastgelopen test houdt de JVM niet in leven
        worker.start();
        long limit = DEBUGGING ? 0 : timeoutMs;
        try {
            worker.join(limit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (worker.isAlive()) return new Outcome(null, null, limit * 1_000_000, true);
        return new Outcome(value[0], error[0], nanos[0], false);
    }

    private static boolean skipIfStopped(String name) {
        if (!stopped) return false;
        skipped++;
        System.out.println("⏭️ SKIP  " + name);
        return true;
    }

    private static void pass(String name, Outcome out, String extra) {
        passed++;
        totalNanos += out.nanos();
        System.out.println("✅ PASS  " + name + timing(out) + (extra == null ? "" : " — " + extra));
    }

    private static void fail(String name, Outcome out, String reason) {
        failed++;
        totalNanos += out.nanos();
        System.out.println("❌ FAIL  " + name + timing(out));
        System.out.println(indent(reason, 4));
        System.out.println();
        if (stopOnFirstFail) stopped = true;
    }

    private static String timing(Outcome out) {
        return showTiming && out.nanos() > 0 ? "  (" + formatNanos(out.nanos()) + ")" : "";
    }

    private static String timeoutMessage() {
        return "Timeout: no result within " + timeoutMs + " ms (infinite loop? raise timeoutMs if needed)";
    }

    private static void printSummary() {
        if (passed + failed + skipped == 0) return;
        String line = "=".repeat(50);
        System.out.println(line);
        System.out.printf("%s Summary: %d passed, %d failed%s, %d total%n",
                failed == 0 ? "✅" : "❌", passed, failed,
                skipped > 0 ? ", " + skipped + " skipped" : "", passed + failed + skipped);
        if (showTiming) System.out.println("Total time: " + formatNanos(totalNanos));
        System.out.println(line);
        System.out.flush();
        if (exitNonZeroOnFail && failed > 0) Runtime.getRuntime().halt(1);
    }

    private static String formatNanos(long nanos) {
        if (nanos < 1_000) return nanos + " ns";
        if (nanos < 1_000_000) return String.format(Locale.ROOT, "%.1f µs", nanos / 1e3);
        if (nanos < 1_000_000_000) return String.format(Locale.ROOT, "%.2f ms", nanos / 1e6);
        return String.format(Locale.ROOT, "%.2f s", nanos / 1e9);
    }

    private static String describe(Throwable t) {
        return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
    }

    /** Stacktrace met alleen frames uit je eigen code (klikbaar in IntelliJ). */
    private static String trace(Throwable t) {
        var sb = new StringBuilder();
        int shown = 0, hidden = 0;
        for (StackTraceElement f : t.getStackTrace()) {
            String cls = f.getClassName();
            boolean internal = cls.startsWith("java.") || cls.startsWith("jdk.") || cls.startsWith("sun.")
                    || cls.equals(Harness.class.getName()) || cls.startsWith(Harness.class.getName() + "$")
                    || f.getMethodName().startsWith("lambda$");
            if (internal) continue;
            if (shown < maxTraceFrames) {
                sb.append("at ").append(f).append('\n');
                shown++;
            } else {
                hidden++;
            }
        }
        if (hidden > 0) sb.append("... ").append(hidden).append(" more frame(s)\n");
        if (t.getCause() != null && t.getCause() != t) sb.append("Caused by: ").append(describe(t.getCause()));
        return sb.toString().stripTrailing();
    }

    private static String mismatch(Object expected, Object actual) {
        var sb = new StringBuilder();
        sb.append("expected: ").append(render(expected)).append('\n');
        sb.append("  actual: ").append(render(actual));
        String exp = pretty(expected), act = pretty(actual);
        String hint = expected instanceof Expectation e ? e.explain(actual) : null;
        if (hint != null) {
            sb.append("\n    hint: ").append(hint);
        } else if (exp.equals(act)) {
            sb.append("\n   types: expected ").append(typeName(expected)).append(", actual ").append(typeName(actual));
        } else if (!(expected instanceof Expectation)) {
            sb.append("\n    diff: ").append(firstDiff(exp, act));
        }
        return sb.toString();
    }

    private static String typeName(Object o) {
        if (o == null) return "null";
        if (o instanceof List<?>) return "List";
        if (o instanceof Set<?>) return "Set";
        if (o instanceof Map<?, ?>) return "Map";
        return o.getClass().getSimpleName();
    }

    private static String firstDiff(String a, String b) {
        int n = Math.min(a.length(), b.length());
        int i = 0;
        while (i < n && a.charAt(i) == b.charAt(i)) i++;
        if (i == n && a.length() == b.length()) return "(none)";
        int from = Math.max(0, i - 15);
        return "at char " + i + ": …" + a.substring(from, Math.min(a.length(), i + 15))
                + "…  vs  …" + b.substring(from, Math.min(b.length(), i + 15)) + "…";
    }

    /** Matrices/geneste lijsten met lange regels worden rij voor rij geprint. */
    private static String render(Object o) {
        String flat = pretty(o);
        if (flat.length() <= 100) return flat;
        List<Object> rows = o instanceof Expectation ? null : asItems(o);
        if (rows == null || rows.isEmpty() || !rows.stream().allMatch(r -> asItems(r) != null)) return flat;
        var sb = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            sb.append("\n  ").append(pretty(rows.get(i))).append(i < rows.size() - 1 ? "," : "");
        }
        return sb.append("\n]").toString();
    }

    private static String indent(String s, int spaces) {
        String pad = " ".repeat(spaces);
        return pad + s.replace("\n", "\n" + pad);
    }

    private static String abbreviate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ======================================================================
    // 🎯 Matchers (te gebruiken als "expected")
    // ======================================================================

    /** Een expected-waarde met eigen vergelijkingslogica. */
    public interface Expectation {
        boolean matches(Object actual);

        String describe();

        /** Optionele extra uitleg bij een mismatch. */
        default String explain(Object actual) {
            return null;
        }
    }

    /** Volgorde van de buitenste lijst/array maakt niet uit (3Sum, Group Anagrams, Permutations, ...). */
    public static Expectation anyOrder(Object expected) {
        return new AnyOrder(expected, false);
    }

    /** Volgorde maakt op geen enkel niveau uit (Combination Sum, 4Sum, ...). */
    public static Expectation anyOrderDeep(Object expected) {
        return new AnyOrder(expected, true);
    }

    /** Elk van de opgegeven antwoorden is goed (bijv. Longest Palindromic Substring: "bab" of "aba"). */
    public static Expectation oneOf(Object... options) {
        return new Expectation() {
            public boolean matches(Object actual) {
                for (Object o : options) if (deepEquals(o, actual)) return true;
                return false;
            }

            public String describe() {
                return "oneOf(" + String.join(", ", Arrays.stream(options).map(Harness::pretty).toList()) + ")";
            }
        };
    }

    /** Getal binnen {@link #epsilon} van {@code value}. */
    public static Expectation approx(double value) {
        return approx(value, epsilon);
    }

    public static Expectation approx(double value, double eps) {
        return new Expectation() {
            public boolean matches(Object actual) {
                return actual instanceof Number n && Math.abs(n.doubleValue() - value) <= eps;
            }

            public String describe() {
                return "approx(" + value + " ± " + eps + ")";
            }
        };
    }

    private record AnyOrder(Object expected, boolean deep) implements Expectation {
        public boolean matches(Object actual) {
            return deepEquals(normalize(expected, deep, true), normalize(actual, deep, true));
        }

        public String describe() {
            return (deep ? "anyOrderDeep(" : "anyOrder(") + pretty(expected) + ")";
        }

        public String explain(Object actual) {
            if (!(normalize(expected, deep, true) instanceof List<?> e)) return null;
            if (!(normalize(actual, deep, true) instanceof List<?> a)) return null;
            return "missing " + pretty(minus(e, a)) + ", unexpected " + pretty(minus(a, e));
        }

        /** Arrays → lijsten; sorteert de buitenste lijst (en bij deep ook alle geneste lijsten). */
        private static Object normalize(Object o, boolean deep, boolean top) {
            List<Object> items = asItems(o);
            if (items == null) return o;
            var out = new ArrayList<Object>(items.size());
            for (Object x : items) out.add(normalize(x, deep, false));
            if (top || deep) out.sort(Comparator.comparing(Harness::pretty));
            return out;
        }

        private static List<Object> minus(List<?> a, List<?> b) {
            var counts = new HashMap<String, Integer>();
            for (Object x : b) counts.merge(pretty(x), 1, Integer::sum);
            var out = new ArrayList<Object>();
            for (Object x : a) {
                if (counts.merge(pretty(x), -1, Integer::sum) < 0) out.add(x);
            }
            return out;
        }
    }

    // ======================================================================
    // 🔍 Deep equals + pretty printing
    // ======================================================================

    /**
     * Diepe vergelijking: matchers, getallen (double met epsilon, int/long op waarde), alle arrays
     * (ook genest), List, Set, Map, ListNode en TreeNode (structureel).
     */
    static boolean deepEquals(Object expected, Object actual) {
        if (expected instanceof Expectation e) return e.matches(actual);
        if (expected == actual) return true;
        if (expected == null || actual == null) return false;

        if (expected instanceof Number x && actual instanceof Number y) return numbersEqual(x, y);

        if (expected.getClass().isArray() && actual.getClass().isArray()) {
            int n = Array.getLength(expected);
            if (n != Array.getLength(actual)) return false;
            for (int i = 0; i < n; i++) {
                if (!deepEquals(Array.get(expected, i), Array.get(actual, i))) return false;
            }
            return true;
        }

        if (expected instanceof List<?> a && actual instanceof List<?> b) {
            if (a.size() != b.size()) return false;
            Iterator<?> ia = a.iterator(), ib = b.iterator();
            while (ia.hasNext()) if (!deepEquals(ia.next(), ib.next())) return false;
            return true;
        }

        if (expected instanceof Set<?> a && actual instanceof Set<?> b) {
            return a.size() == b.size()
                    && a.stream().map(Harness::pretty).sorted().toList()
                    .equals(b.stream().map(Harness::pretty).sorted().toList());
        }

        if (expected instanceof Map<?, ?> a && actual instanceof Map<?, ?> b) {
            if (a.size() != b.size()) return false;
            for (var e : a.entrySet()) {
                if (!b.containsKey(e.getKey()) || !deepEquals(e.getValue(), b.get(e.getKey()))) return false;
            }
            return true;
        }

        if (expected instanceof ListNode a && actual instanceof ListNode b) {
            return listNodeToList(a).equals(listNodeToList(b));
        }
        if (expected instanceof TreeNode a && actual instanceof TreeNode b) return treesEqual(a, b);

        return expected.equals(actual);
    }

    private static boolean numbersEqual(Number x, Number y) {
        boolean floating = x instanceof Double || x instanceof Float || x instanceof BigDecimal
                || y instanceof Double || y instanceof Float || y instanceof BigDecimal;
        if (!floating) return x.longValue() == y.longValue();
        double a = x.doubleValue(), b = y.doubleValue();
        if (Double.isNaN(a) || Double.isNaN(b)) return Double.isNaN(a) && Double.isNaN(b);
        if (Double.isInfinite(a) || Double.isInfinite(b)) return a == b;
        return Math.abs(a - b) <= epsilon * Math.max(1.0, Math.abs(a));
    }

    private static boolean treesEqual(TreeNode a, TreeNode b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.val == b.val && treesEqual(a.left, b.left) && treesEqual(a.right, b.right);
    }

    /** Leesbare weergave in LeetCode-stijl: strings gequote, ListNode/TreeNode als lijst. */
    public static String pretty(Object o) {
        if (o == null) return "null";
        if (o instanceof Expectation e) return e.describe();
        if (o instanceof String s) return '"' + s + '"';
        if (o instanceof Character c) return "'" + c + "'";
        if (o instanceof ListNode n) return pretty(listNodeToList(n));
        if (o instanceof TreeNode t) return pretty(treeToList(t));
        if (o instanceof Map<?, ?> m) {
            var parts = new ArrayList<String>(m.size());
            for (var e : m.entrySet()) parts.add(pretty(e.getKey()) + "=" + pretty(e.getValue()));
            parts.sort(String::compareTo);
            return "{" + String.join(", ", parts) + "}";
        }
        if (o instanceof Set<?> s) {
            return "{" + String.join(", ", s.stream().map(Harness::pretty).sorted().toList()) + "}";
        }
        List<Object> items = asItems(o);
        if (items != null) {
            var parts = new ArrayList<String>(items.size());
            for (Object x : items) parts.add(pretty(x));
            return "[" + String.join(", ", parts) + "]";
        }
        return String.valueOf(o);
    }

    /** Array (ook primitief) of List → List&lt;Object&gt;, anders null. */
    private static List<Object> asItems(Object o) {
        if (o == null) return null;
        if (o.getClass().isArray()) {
            int n = Array.getLength(o);
            var out = new ArrayList<Object>(n);
            for (int i = 0; i < n; i++) out.add(Array.get(o, i));
            return out;
        }
        if (o instanceof List<?> l) return new ArrayList<>(l);
        return null;
    }

    // ======================================================================
    // 🧰 Kleine helpers
    // ======================================================================

    /** Print waarden gescheiden door spaties; strings ongequote, de rest via {@link #pretty}. */
    public static void log(Object... parts) {
        var sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(parts[i] instanceof String s ? s : pretty(parts[i]));
        }
        System.out.println(sb);
    }

    @SafeVarargs
    public static <T> List<T> list(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    // ======================================================================
    // 🧩 Parsers voor LeetCode-literals
    // ======================================================================

    /** {@code "[1,2,3]"} → int[] */
    public static int[] ints(String s) { return parseAs(s, int[].class); }

    public static long[] longs(String s) { return parseAs(s, long[].class); }

    public static double[] doubles(String s) { return parseAs(s, double[].class); }

    public static boolean[] bools(String s) { return parseAs(s, boolean[].class); }

    /** {@code "[\"a\",\"b\"]"} of {@code "abc"} → char[] */
    public static char[] chars(String s) { return parseAs(s, char[].class); }

    /** {@code "[\"eat\",\"tea\"]"} → String[] */
    public static String[] strings(String s) { return parseAs(s, String[].class); }

    /** {@code "[[1,2],[3,4]]"} → int[][] */
    public static int[][] intMatrix(String s) { return parseAs(s, int[][].class); }

    /** {@code "[[\"5\",\"3\",\".\"],...]"} → char[][] (Sudoku, Word Search, ...) */
    public static char[][] charMatrix(String s) { return parseAs(s, char[][].class); }

    public static String[][] stringMatrix(String s) { return parseAs(s, String[][].class); }

    /** {@code "[1,2,3]"} → List&lt;Integer&gt; */
    public static List<Integer> intList(String s) { return parseAs(s, listOf(Integer.class)); }

    /** {@code "[[2,2,3],[7]]"} → List&lt;List&lt;Integer&gt;&gt; */
    public static List<List<Integer>> intLists(String s) { return parseAs(s, listOf(listOf(Integer.class))); }

    public static List<String> stringList(String s) { return parseAs(s, listOf(String.class)); }

    public static List<List<String>> stringLists(String s) { return parseAs(s, listOf(listOf(String.class))); }

    /** Parse een literal naar een willekeurig (generiek) type, bijv. {@code parseAs(s, long[][].class)}. */
    @SuppressWarnings("unchecked")
    public static <T> T parseAs(String literal, Type type) {
        return (T) convert(parse(literal), type);
    }

    /** Ruwe parse: lijsten → List, getallen → BigDecimal, strings, true/false/null. */
    public static Object parse(String literal) {
        var p = new LiteralParser(literal, 0);
        Object v = p.value();
        if (!p.atEnd()) throw p.error("unexpected trailing input");
        return v;
    }

    private static ParameterizedType listOf(Type element) {
        return new ParameterizedType() {
            public Type[] getActualTypeArguments() { return new Type[]{element}; }

            public Type getRawType() { return List.class; }

            public Type getOwnerType() { return null; }

            public String getTypeName() { return "List<" + element.getTypeName() + ">"; }
        };
    }

    /** Converteert een geparste waarde naar het gevraagde Java-type. */
    static Object convert(Object v, Type type) {
        if (type instanceof WildcardType w) return convert(v, w.getUpperBounds()[0]);
        if (type instanceof ParameterizedType p) {
            Class<?> raw = (Class<?>) p.getRawType();
            if (v == null) return null;
            Type element = p.getActualTypeArguments()[0];
            Collection<Object> out = Set.class.isAssignableFrom(raw) ? new LinkedHashSet<>()
                    : Deque.class.isAssignableFrom(raw) || Queue.class.isAssignableFrom(raw) ? new ArrayDeque<>()
                    : new ArrayList<>();
            if (!raw.isInstance(out)) throw new IllegalArgumentException("unsupported type " + type.getTypeName());
            for (Object x : items(v, type)) out.add(convert(x, element));
            return out;
        }
        if (type instanceof GenericArrayType g) {
            Type component = g.getGenericComponentType();
            Class<?> rawComponent = component instanceof ParameterizedType pc ? (Class<?>) pc.getRawType() : Object.class;
            List<Object> xs = items(v, type);
            Object arr = Array.newInstance(rawComponent, xs.size());
            for (int i = 0; i < xs.size(); i++) Array.set(arr, i, convert(xs.get(i), component));
            return arr;
        }

        Class<?> c = (Class<?>) type;
        if (v == null) {
            if (c.isPrimitive()) throw new IllegalArgumentException("null for primitive " + c.getName());
            return null;
        }
        if (c == int.class || c == Integer.class) return number(v, c).intValueExact();
        if (c == long.class || c == Long.class) return number(v, c).longValueExact();
        if (c == short.class || c == Short.class) return number(v, c).shortValueExact();
        if (c == byte.class || c == Byte.class) return number(v, c).byteValueExact();
        if (c == double.class || c == Double.class) return number(v, c).doubleValue();
        if (c == float.class || c == Float.class) return number(v, c).floatValue();
        if (c == boolean.class || c == Boolean.class) {
            if (v instanceof Boolean b) return b;
            throw new IllegalArgumentException("expected true/false but got " + pretty(v));
        }
        if (c == char.class || c == Character.class) {
            if (v instanceof String s && s.length() == 1) return s.charAt(0);
            throw new IllegalArgumentException("expected a single character but got " + pretty(v));
        }
        if (c == String.class) {
            if (v instanceof String s) return s;
            throw new IllegalArgumentException("expected a string but got " + pretty(v));
        }
        if (c == char[].class && v instanceof String s) return s.toCharArray();
        if (c.isArray()) {
            List<Object> xs = items(v, type);
            Object arr = Array.newInstance(c.getComponentType(), xs.size());
            for (int i = 0; i < xs.size(); i++) Array.set(arr, i, convert(xs.get(i), c.getComponentType()));
            return arr;
        }
        if (c == ListNode.class) return listNode((int[]) convert(v, int[].class));
        if (c == TreeNode.class) return tree((Integer[]) convert(v, Integer[].class));
        if (c == Object.class) return plain(v);
        if (c.isAssignableFrom(ArrayList.class)) return plain(v);
        throw new IllegalArgumentException("unsupported type " + c.getName());
    }

    private static List<Object> items(Object v, Type type) {
        if (v instanceof List<?> l) return new ArrayList<>(l);
        throw new IllegalArgumentException("expected a list for " + type.getTypeName() + " but got " + pretty(v));
    }

    private static BigDecimal number(Object v, Class<?> target) {
        if (v instanceof BigDecimal d) return d;
        throw new IllegalArgumentException("expected a number for " + target.getSimpleName() + " but got " + pretty(v));
    }

    /** Zonder doeltype: gehele getallen → Integer/Long, overige → Double, lijsten recursief. */
    private static Object plain(Object v) {
        if (v instanceof BigDecimal d) {
            try {
                long l = d.longValueExact();
                return l == (int) l ? (Object) (int) l : (Object) l;
            } catch (ArithmeticException e) {
                return d.doubleValue();
            }
        }
        if (v instanceof List<?> l) {
            var out = new ArrayList<Object>(l.size());
            for (Object x : l) out.add(plain(x));
            return out;
        }
        return v;
    }

    /** Minimale recursive-descent parser voor LeetCode-waarden: [..], "..", getallen, true/false/null. */
    private static final class LiteralParser {
        final String s;
        int pos;

        LiteralParser(String s, int pos) {
            this.s = s;
            this.pos = pos;
        }

        void skipWs() {
            while (pos < s.length() && (Character.isWhitespace(s.charAt(pos)) || s.charAt(pos) == ' ')) pos++;
        }

        boolean atEnd() {
            skipWs();
            return pos >= s.length();
        }

        char peek() {
            if (atEnd()) throw error("unexpected end of input");
            return s.charAt(pos);
        }

        boolean peekIs(char c) {
            return !atEnd() && s.charAt(pos) == c;
        }

        boolean tryConsume(char c) {
            if (!peekIs(c)) return false;
            pos++;
            return true;
        }

        Object value() {
            char c = peek();
            if (c == '[') return array();
            if (c == '"' || c == '\'') return string(c);
            if (c == '-' || c == '+' || c == '.' || Character.isDigit(c)) return number();
            if (Character.isJavaIdentifierStart(c)) {
                String w = word();
                return switch (w) {
                    case "true" -> true;
                    case "false" -> false;
                    case "null" -> null;
                    default -> throw error("unknown word '" + w + "'");
                };
            }
            throw error("unexpected character '" + c + "'");
        }

        List<Object> array() {
            pos++; // '['
            var out = new ArrayList<Object>();
            if (tryConsume(']')) return out;
            while (true) {
                out.add(value());
                if (tryConsume(']')) return out;
                if (!tryConsume(',')) throw error("expected ',' or ']'");
            }
        }

        String string(char quote) {
            pos++;
            var sb = new StringBuilder();
            while (true) {
                if (pos >= s.length()) throw error("unterminated string");
                char c = s.charAt(pos++);
                if (c == quote) return sb.toString();
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (pos >= s.length()) throw error("unterminated escape");
                char e = s.charAt(pos++);
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (pos + 4 > s.length()) throw error("bad unicode escape");
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> sb.append(e); // \" \\ \' \/
                }
            }
        }

        BigDecimal number() {
            int start = pos;
            while (pos < s.length() && "+-.eE0123456789".indexOf(s.charAt(pos)) >= 0) pos++;
            try {
                return new BigDecimal(s.substring(start, pos));
            } catch (NumberFormatException e) {
                pos = start;
                throw error("invalid number");
            }
        }

        String word() {
            int start = pos;
            while (pos < s.length() && Character.isJavaIdentifierPart(s.charAt(pos))) pos++;
            return s.substring(start, pos);
        }

        IllegalArgumentException error(String msg) {
            int from = Math.max(0, pos - 20), to = Math.min(s.length(), pos + 20);
            return new IllegalArgumentException(msg + " at position " + pos + " near: «"
                    + s.substring(from, to).replace('\n', ' ') + "»");
        }
    }

    // ======================================================================
    // 🔗 ListNode / 🌳 TreeNode helpers
    // ======================================================================

    /** {@code listNode(1, 2, 3)} → 1 → 2 → 3 */
    public static ListNode listNode(int... values) {
        ListNode dummy = new ListNode(0), cur = dummy;
        for (int v : values) cur = cur.next = new ListNode(v);
        return dummy.next;
    }

    /** {@code listNode("[1,2,3]")} */
    public static ListNode listNode(String literal) {
        return listNode(ints(literal));
    }

    public static List<Integer> listNodeToList(ListNode head) {
        var out = new ArrayList<Integer>();
        for (ListNode cur = head; cur != null; cur = cur.next) {
            out.add(cur.val);
            if (out.size() > 1_000_000) throw new IllegalStateException("ListNode has > 1e6 nodes — cycle?");
        }
        return out;
    }

    /** Level-order zoals LeetCode: {@code tree(1, null, 2, 3)} */
    public static TreeNode tree(Integer... levelOrder) {
        if (levelOrder.length == 0 || levelOrder[0] == null) return null;
        var root = new TreeNode(levelOrder[0]);
        var queue = new ArrayDeque<TreeNode>();
        queue.add(root);
        int i = 1;
        while (!queue.isEmpty() && i < levelOrder.length) {
            TreeNode node = queue.remove();
            if (i < levelOrder.length && levelOrder[i++] instanceof Integer l) queue.add(node.left = new TreeNode(l));
            if (i < levelOrder.length && levelOrder[i++] instanceof Integer r) queue.add(node.right = new TreeNode(r));
        }
        return root;
    }

    /** {@code tree("[1,null,2,3]")} */
    public static TreeNode tree(String literal) {
        return tree((Integer[]) parseAs(literal, Integer[].class));
    }

    /** TreeNode → level-order lijst met nulls, zonder trailing nulls (LeetCode-formaat). */
    public static List<Integer> treeToList(TreeNode root) {
        var out = new ArrayList<Integer>();
        var queue = new ArrayDeque<Optional<TreeNode>>();
        queue.add(Optional.ofNullable(root));
        while (!queue.isEmpty()) {
            TreeNode n = queue.remove().orElse(null);
            out.add(n == null ? null : n.val);
            if (n != null) {
                queue.add(Optional.ofNullable(n.left));
                queue.add(Optional.ofNullable(n.right));
            }
        }
        int end = out.size();
        while (end > 0 && out.get(end - 1) == null) end--;
        return new ArrayList<>(out.subList(0, end));
    }
}
