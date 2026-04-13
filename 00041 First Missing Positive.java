// Runtime 2ms beats 38%
// Memory 71MB beats 64%
class Solution {
    /**
     * Vind het laagste positieve getal dat niet binnen nums[] aanwezig is,
     * maar dan zodat geheugengebruik O(1) is en O(n) in tijd
     *
     * @param nums ongesorteerde array met getallen
     * @return kleinste positieve getal niet aanwezig in nums[]
     */
    public int firstMissingPositive(int[] nums) {
        // Strategie: herschik nums[] zodat nums[i] == i+1 voor iedere i+1 die aanwezig is,
        // en nums[i] == 0 wanneer i+1 niet aanwezig is.

        for (int i = 0; i < nums.length; i++) {
            this.rearrangeNumber(nums, i);
        }

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != i + 1) {
                return i + 1;
            }
        }

        // Indien geen getal binnen de array gevonden, zijn alle nummers van 1...nums.length aanwezig.
        // In dit geval is het antwoord nums.length+1.
        return nums.length + 1;
    }

    /**
     * Het nummer op de plek van nums[i] staat mogelijk op de verkeerde plek. Rangschik opnieuw.
     *
     * @param nums
     * @param i
     */
    private void rearrangeNumber(int[] nums, int i) {
        int num;

        // Getal staat niet op de juiste positie van nums, en kunnen we wisselen naar de juiste plek
        while ((num = nums[i]) > 0 && nums[i] <= nums.length && nums[i] != i + 1) {
            // Getal is in range van 1...nums.length

            // Wissel het getal op de verkeerde positie, met het getal waar we naartoe willen verplaatsen.
            // en herhaal totdat we niet langer kunnen wisselen naar een valide positie.
            int switchWithNum = nums[num - 1];
            nums[num - 1] = num;
            nums[i] = switchWithNum;

            if (num == switchWithNum) {
                // Het getal waar we mee wisselen is identiek aan het getal wat we al hadden.
                // Opnieuw blijven wisselen heeft hier geen zin.
                break;
            }
        }
    }
}