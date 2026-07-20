class Solution {

    /**
     * Bepaalt het minimale aantal sprongen dat nodig is om de laatste index
     * van de array te bereiken.
     *
     * <p>Greedy strategie:
     * vanuit de huidige positie bekijken we alle indices die met één sprong
     * bereikbaar zijn. Daaruit kiezen we de index waarvoor
     * {@code index + nums[index]} maximaal is. Die index geeft ons voor de
     * daaropvolgende sprong het grootst mogelijke bereik.</p>
     *
     * <p>De opgave garandeert dat de laatste index altijd bereikbaar is.</p>
     *
     * @param nums nums[i] is de maximale spronglengte vanaf index i
     * @return het minimale aantal sprongen naar de laatste index
     */
    public int jump(int[] nums) {
        int lastIndex = nums.length - 1;
        int currentIndex = 0;
        int jumpsNeeded = 0;

        while (currentIndex < lastIndex) {
            // Verste index die direct vanaf de huidige positie bereikbaar is.
            int currentReach = currentIndex + nums[currentIndex];

            /*
             * Wanneer de laatste index al binnen het huidige bereik ligt,
             * is nog precies één sprong nodig. De tussenliggende indices
             * hoeven dan niet meer onderzocht te worden.
             */
            if (currentReach >= lastIndex) {
                return jumpsNeeded + 1;
            }

            /*
             * Zoek binnen het huidige bereik naar de beste landingspositie:
             * de index vanwaar we bij de volgende sprong het verst kunnen komen.
             */
            int bestNextIndex = currentIndex;
            int farthestNextReach = currentReach;

            for (int candidateIndex = currentIndex + 1;
                 candidateIndex <= currentReach;
                 candidateIndex++) {

                int candidateReach =
                        candidateIndex + nums[candidateIndex];

                if (candidateReach > farthestNextReach) {
                    farthestNextReach = candidateReach;
                    bestNextIndex = candidateIndex;
                }
            }

            /*
             * Maak de greedy keuze en ga verder vanaf de gevonden index.
             * Door de bereikbaarheidsgarantie bestaat er altijd een kandidaat
             * die ons verder brengt wanneer het einde nog niet bereikbaar is.
             */
            currentIndex = bestNextIndex;
            jumpsNeeded++;
        }

        return jumpsNeeded;
    }
}