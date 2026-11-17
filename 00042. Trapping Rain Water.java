// Runtime 1ms beats 59.38%
// Memory 47.88MB beats 44.63%
class Solution {
    public int trap(int[] height) {
        int left = 0;
        int right = height.length - 1;
        int trappedWater = 0;
        int trapWaterUntilLevel = 0;
        // Zo lang als we nog niet alles gehad hebben, ga door...
        while (left < right) {
            // Update het nieuwe niveau tot waar water blijft staan:
            trapWaterUntilLevel = Math.max(trapWaterUntilLevel, Math.min(height[left], height[right]));

            // Hoogtes onder het niveau tot waar water blijft staan, wordt opgeteld
            trappedWater += Math.max(0, trapWaterUntilLevel - height[left]);
            trappedWater += Math.max(0, trapWaterUntilLevel - height[right]);

            // We blijven de laagste pointer bewegen naar het hogere punt
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }

        return trappedWater;
    }
}