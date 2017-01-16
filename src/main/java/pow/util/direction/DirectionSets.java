package pow.util.direction;

public class DirectionSets {
    public static class Cardinal {
        public static final int N = 0;
        public static final int E = 1;
        public static final int S = 2;
        public static final int W = 3;
        private static final int[] OPPOSITES = {2,3,0,1};
        private static final Direction[] DIRECTIONS = {Direction.N, Direction.E, Direction.S, Direction.W};
        private static final String[] NAMES = {"north", "east", "south", "west"};
        public static int size() { return 4; }
        public static int getOpposite(int dirIdx) { return OPPOSITES[dirIdx]; }
        public static Direction getDirection(int dirIdx) { return DIRECTIONS[dirIdx]; }
        public static String getName(int dirIdx) { return NAMES[dirIdx]; }
    }

    // TODO: add similar classes for diagonal/all directions if it's useful

}
