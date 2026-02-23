class Solution {
    /**
     * Geeft het {@code n}-de element terug van de "count-and-say"-reeks.
     *
     * <p>De reeks is recursief gedefinieerd:
     * <ul>
     *   <li>{@code countAndSay(1) = "1"}</li>
     *   <li>{@code countAndSay(n)} is de run-length encoding (RLE) van {@code countAndSay(n-1)}</li>
     * </ul>
     *
     * <p>Voorbeeld van de eerste termen:
     * <pre>{@code
     * n=1 -> "1"
     * n=2 -> "11"   (één 1)
     * n=3 -> "21"   (twee 1-en)
     * n=4 -> "1211" (één 2, daarna één 1)
     * }</pre>
     *
     * @param n de 1-gebaseerde index in de reeks (moet {@code >= 1} zijn)
     * @return de {@code n}-de term als digit-string
     */
    public String countAndSay(int n) {
        if (n == 1)
            return "1";
        return rle(countAndSay(n - 1));
    }

    /**
     * Berekent de run-length encoding (RLE) van een string met cijfers.
     *
     * <p>Deze methode leest de input van links naar rechts en zet elke maximale reeks
     * van gelijke digits om naar: {@code <aantal><digit>}. Het resultaat is de
     * concatenatie van al deze stukjes.
     *
     * <p>Voorbeelden:
     * <pre>{@code
     * rle("1")       -> "11"        (één '1')
     * rle("11")      -> "21"        (twee '1'-en)
     * rle("21")      -> "1211"      (één '2', daarna één '1')
     * rle("3322251") -> "23321511"  (twee '3'-en, drie '2'-en, één '5', één '1')
     * }</pre>
     *
     * @param str inputstring bestaande uit digit-karakters ('0'..'9'); moet niet leeg zijn
     * @return de run-length encoded representatie van {@code str}
     */
    private String rle(String str) {
        var sb = new StringBuilder(str.length() * 2);
        int huidigeDigit = str.charAt(0) - '0';
        int huidigeAantal = 0;
        for (int i = 0; i < str.length(); i++) {
            // Karakter omzetten naar getal
            int digit = str.charAt(i) - '0';
            // Indien dezelfde digit, count ophogen
            if (huidigeDigit == digit) {
                huidigeAantal++;
            } else {
                // Indien nieuwe digit, reeks beeindigen, count resetten en opnieuw beginnen
                sb.append(huidigeAantal).append(huidigeDigit);
                huidigeAantal = 1;
                huidigeDigit = digit;
            }
            // Indien einde van de String, ook huidige reeks wegschrijven
            if (i == str.length() - 1)
                sb.append(huidigeAantal).append(huidigeDigit);
        }
        return sb.toString();
    }
}