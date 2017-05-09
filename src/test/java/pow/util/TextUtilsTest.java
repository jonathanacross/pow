package pow.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TextUtilsTest {

    private static class TestCase {
        public final String raw;
        public final String zero;
        public final String oneIndef;
        public final String oneDef;
        public final String two;

        public TestCase(String raw, String zero, String oneIndef, String oneDef, String two) {
            this.raw = raw;
            this.zero = zero;
            this.oneIndef = oneIndef;
            this.oneDef = oneDef;
            this.two = two;
        }
    }

    private final List<TestCase> examples = Arrays.asList(
            new TestCase("!Bladeturner", "Bladeturner", "Bladeturner", "Bladeturner", "Bladeturner"),
            new TestCase("!The Star of Elendil", "The Star of Elendil", "The Star of Elendil", "The Star of Elendil", "The Star of Elendil"),
            new TestCase("!The one Ring", "The one Ring", "The one Ring", "The one Ring", "The one Ring"),
            new TestCase("!Grith, king of the orcs", "Grith, king of the orcs", "Grith, king of the orcs", "Grith, king of the orcs", "Grith, king of the orcs"),
            new TestCase("& flask~ of oil", "no flasks of oil", "a flask of oil", "the flask of oil", "2 flasks of oil"),
            new TestCase("water", "no water", "water", "the water", "2 water"),
            new TestCase("grass", "no grass", "grass", "the grass", "2 grass"),
            new TestCase("paper", "no paper", "paper", "the paper", "2 paper"),
            new TestCase("chain mail~", "no chain mails", "chain mail", "the chain mail", "2 chain mails"),
            new TestCase("soft leather armor~", "no soft leather armors", "soft leather armor", "the soft leather armor", "2 soft leather armors"),
            new TestCase("& ant~", "no ants", "an ant", "the ant", "2 ants"),
            new TestCase("& el|f|ves|", "no elves", "an elf", "the elf", "2 elves"),
            new TestCase("& iguana~", "no iguanas", "an iguana", "the iguana", "2 iguanas"),
            new TestCase("& octop|us|i|", "no octopi", "an octopus", "the octopus", "2 octopi"),
            new TestCase("& umbrella~", "no umbrellas", "an umbrella", "the umbrella", "2 umbrellas"),
            new TestCase("& ball~", "no balls", "a ball", "the ball", "2 balls"),
            new TestCase("& blacksmith~", "no blacksmiths", "a blacksmith", "the blacksmith", "2 blacksmiths"),
            new TestCase("& glyph~", "no glyphs", "a glyph", "the glyph", "2 glyphs"),
            new TestCase("& ruby~", "no rubies", "a ruby", "the ruby", "2 rubies"),
            new TestCase("& torch~", "no torches", "a torch", "the torch", "2 torches"),
            new TestCase("& bush~", "no bushes", "a bush", "the bush", "2 bushes"),
            new TestCase("& box~", "no boxes", "a box", "the box", "2 boxes"),
            new TestCase("& cutlass~", "no cutlasses", "a cutlass", "the cutlass", "2 cutlasses"),
            new TestCase("& topaz~", "no topazes", "a topaz", "the topaz", "2 topazes"),
            new TestCase("& kni|fe|ves|", "no knives", "a knife", "the knife", "2 knives"),
            new TestCase("& sta|ff|ves| of iron", "no staves of iron", "a staff of iron", "the staff of iron", "2 staves of iron")
    );

    @Test
    public void testFormatNone() {
        for (TestCase testCase: examples) {
            String formatted = TextUtils.format(testCase.raw, 0, false);
            assertEquals(testCase.zero, formatted);
        }
    }

    @Test
    public void testFormatOneIndefinite() {
        for (TestCase testCase: examples) {
            String formatted = TextUtils.format(testCase.raw, 1, false);
            assertEquals(testCase.oneIndef, formatted);
        }
    }

    @Test
    public void testFormatOneDefinite() {
        for (TestCase testCase: examples) {
            String formatted = TextUtils.format(testCase.raw, 1, true);
            assertEquals(testCase.oneDef, formatted);
        }
    }

    @Test
    public void testFormatTwo() {
        for (TestCase testCase: examples) {
            String formatted = TextUtils.format(testCase.raw, 2, false);
            assertEquals(testCase.two, formatted);
        }
    }


}

