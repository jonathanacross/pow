package pow.backend.dungeon.gen;

import pow.util.DebugLogger;
import pow.util.TextUtils;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// adapted from http://www.roguebasin.com/index.php?title=Names_from_a_high_order_Markov_Process_and_a_simplified_Katz_back-off_scheme
public class NameGenerator {

    private static final NameGenerator instance;

    static {
        try {
            List<String> names = getNames("/data/names.tsv");
            instance = new NameGenerator(names, 3, 4, 10);
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private MarkovModel mm;
    private final int minLength;
    private final int maxLength;

    private NameGenerator(List<String> names, int order, int minLength, int maxLength) {
        mm = new MarkovModel(order);
        for (String name: names) {
            mm.observe(name, 1);
        }
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public static String getRandomName(Random rng) {
        String name;
        do {
            name = TextUtils.capitalize(instance.mm.generate(rng));
        } while (name.length() < instance.minLength || name.length() > instance.maxLength);
        return name;
    }

    private static List<String> getNames(String resource) throws IOException {
        // Get file from resources folder
        InputStream tsvStream = NameGenerator.class.getResourceAsStream(resource);
        TsvReader reader = new TsvReader(tsvStream);

        List<String> names = new ArrayList<>();

        for (String[] line : reader.getData()) {
            names.add(line[0]);
        }

        return names;
    }

    private static class CategoricalRandomVariable {
        private final Map<String, Double> frequencies;
        private double total;

        public CategoricalRandomVariable() {
            frequencies = new HashMap<>();
            total = 0;
        }

        public void observe(String event, double count) {
            if (!frequencies.containsKey(event)) {
                frequencies.put(event, count);
            } else {
                frequencies.put(event, frequencies.get(event) + count);
            }
            total += count;
        }

        public String sample(Random rng) {
            double x = rng.nextDouble() * total;
            for (Map.Entry<String, Double> entry : frequencies.entrySet()) {
                String event = entry.getKey();
                double count = entry.getValue();
                if (x <= count) {
                    return event;
                }
                x -= count;
            }
            // shouldn't get here.
            return null;
        }

        public double getProb(String event) {
            return frequencies.get(event) / total;
        }

        @Override
        public String toString() {
            return frequencies.toString();
        }
    }

    private static class MarkovModel {

        // something not in any name to mark the beginning/end
        private static final String BOUNDARY_SYMBOL = "_";
        public final int order;
        public final String prefix;
        public final String suffix;
        public final Map<String, CategoricalRandomVariable> counts;


        public MarkovModel(int order) {
            this.order = order;
            this.prefix = TextUtils.repeat(BOUNDARY_SYMBOL, order);
            this.suffix = BOUNDARY_SYMBOL;
            this.counts = new HashMap<>();
        }

        private CategoricalRandomVariable getRandomVarFromContext(String context) {
            if (! this.counts.containsKey(context)) {
                this.counts.put(context, new CategoricalRandomVariable());
            }
            return this.counts.get(context);
        }

        // In case we run into a sequence that we haven't seen in training data,
        // this helps find a simpler sequence that should be there.  E.g.,
        // backoff("grob") (on an order 3 model) will try:
        // rob (order 3) first
        // ob (order 2) second
        // b (order 1) third
        // "" (order 0) last
        // We should have at least the order 1 stuff from training data no matter what.
        private String backoff(String context) {
            int len = context.length();
            if (len >= order) {
                context = context.substring(len - order);
            } else {
                context = TextUtils.repeat(BOUNDARY_SYMBOL, order - context.length()) + context;
            }

            while (! counts.containsKey(context) && context.length() > 0) {
                context = context.substring(1);
            }

            return context;
        }

        // Adds example to the list of training examples.  In fact, we train all lower-order
        // examples from the word as well.  So, if we were to add the name "froge" on a 3rd order
        // model, then we add the training examples
        // ___ -> f   __f -> r   _fr -> o   fro -> g   rog -> e   oge -> _
        //  __ -> f    _f -> r    _r -> o    ro -> g    og -> e    ge -> _
        //   _ -> f     f -> r     r -> o     o -> g     g -> e     e -> _
        public void observe(String example, double count) {
            String sequence = prefix + example + suffix;
            for (int i = order; i < sequence.length(); i++) {
                String context = sequence.substring(i - order, i);
                String event = String.valueOf(sequence.charAt(i));
                for (int j = 0; j <= context.length(); j++) {
                    getRandomVarFromContext(context.substring(j)).observe(event, count);
                }
            }
        }

        public String sample(String context, Random rng) {
            context = backoff(context);
            return getRandomVarFromContext(context).sample(rng);
        }

        public String generate(Random rng) {
            String sequence = sample(prefix, rng);
            while (sequence.charAt(sequence.length()-1) != (BOUNDARY_SYMBOL.charAt(0))) {
                sequence += sample(sequence, rng);
            }
            return sequence.substring(0, sequence.length()-1);
        }
    }
}
