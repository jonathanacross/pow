package utils;

import pow.backend.utils.ItemCostStats;
import pow.backend.utils.StatsLogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Class to aid in automatic pricing of items.  This takes as input a data file with columns
//
//   [item] [buy/see] [cost] [item attribute 1] ... [item attribute n]
//
// Buy/see is the label for whether the player bought (or didn't buy, but only saw) the item.  The
// attributes are the item features used for pricing the item.
//
// This assumes the item pricing is of the form
//   cost = (sum_i  weight_i attribute_i)^2.
//
// Attributes consist of basic attack stats (+toHit, +toDam, +def), player stats (int, con, dex,
// str), magic resistances (rFire, etc.) other attributes (speed, wealth, slots),
// and action bonuses (for health, mana, speed potions, etc).
// Item slots are not included in the computation.  They may be considered in the future, but
// they don't fit in so well with the cost function above. For now, I'll hardcode as a separate
// multiplier in the game.
//
// We want to find the weights for the different attributes to find the best fitting cost
// function.  The loss used for the fitting has two parts:
//
//   1. Misclassification error
//      (predicted cost) / (label cost) describes how much we over/underpredict an item cost.  We
//      actually take the log of this to put the error on a linear scale.  If we underpredict and
//      there was a buy at the label cost, then this is an error -- the user is willing to pay
//      more for the item.  Conversely, if we overpredict and the user looks at but does not buy,
//      this is an error.  We downweight this error some because it's less clear why a person may
//      not have bought an item after seeing it -- maybe they weren't interested in that item to
//      begin with, rather than not buying because it was too expensive.  Similar to SVM training,
//      if we underpredict the cost a seen item or overpredict the cost for a bought item, we
//      assign 0 error.
//
//   2. Regularization
//      There will be multiple weights that suffice to assign a cost formula that has the same
//      errors (over/under prediction) on the finite set of examples.  So we add an additional
//      L1 regularization to prefer weights should be small and break ties.
//
// From here, we use gradient-descent with backtracking to find the weights for the best fit.
class CostFit {
    // seeing is only 0.25 as important as buying, as there's less signal.
    private static final double SEE_IMPORTANCE = 0.5;

    // importance of parameter regularization
    private static final double REGULARIZATION_WEIGHT = 0.1;

    private static double priceItem(int[] attributes, double[] weights) {
        double weightedSum = 0;
        for (int i = 0; i < attributes.length; i++) {
            weightedSum += attributes[i] * weights[i];
        }
        // Exponential is mathematically simpler and more explainable,
        // but grows too quickly.  Linear is also fairly nice, but
        // grows too slowly.  Squaring seems a good compromise.
        return weightedSum * weightedSum;
    }

//    private static List<ItemCostStats> getExampleData() {
//        List<Example> examples = new ArrayList<>();
//        examples.add(new Example(false, 50, new int[] {0, 1}));
//        examples.add(new Example(false, 75, new int[] {0, 2}));
//        examples.add(new Example(false, 1000, new int[] {0, 4}));
//        examples.add(new Example(true, 10, new int[] {0, 1}));
//        examples.add(new Example(true, 100, new int[] {0, 3}));
//        examples.add(new Example(true, 1000, new int[] {1, 0}));
//        examples.add(new Example(false, 5000, new int[] {1, 0}));
//        examples.add(new Example(true, 8000, new int[] {3, 0}));
//        examples.add(new Example(false, 50000, new int[] {5, 0}));
//        examples.add(new Example(false, 2000, new int[] {1, 5}));
//        return examples;
//    }

    private static double loss(List<ItemCostStats> examples, double[] weights) {
        double totalLoss = 0;

        // misclassification loss
        double buyWeight = -1;
        double seeWeight = -buyWeight * SEE_IMPORTANCE;
        for (ItemCostStats e : examples) {
            double estimatedCost = priceItem(e.attributes, weights);
            double actualCost = e.cost;

            double ratioError = Math.log(estimatedCost) - Math.log(actualCost);
            double lossWeight = e.action == ItemCostStats.Action.BUY ? buyWeight : seeWeight;
            double loss = lossWeight * ratioError;
            totalLoss += Math.max(loss, 0);
        }

        // parameter regularization loss
        for (double w : weights) {
            totalLoss += REGULARIZATION_WEIGHT * Math.abs(w);
        }

        return totalLoss;
    }

    // computes dLoss/dWeight_i.
    private static double[] negLossGradient(List<ItemCostStats> examples, double[] weights) {
        double grad[] = new double[weights.length];

        // misclassification loss gradient
        double buyWeight = -1;
        double seeWeight = -buyWeight * SEE_IMPORTANCE;
        for (ItemCostStats e : examples) {
            double estimatedCost = priceItem(e.attributes, weights);
            double actualCost = e.cost;

            double ratioError = Math.log(estimatedCost) - Math.log(actualCost);
            double lossWeight = e.action == ItemCostStats.Action.BUY ? buyWeight : seeWeight;
            double loss = lossWeight * ratioError;
            if (loss > 0) {
                // We misclassified. Add to gradient.
                for (int i = 0; i < weights.length; i++) {
                    grad[i] += lossWeight * e.attributes[i];
                }
            }
        }

        // parameter regularization loss
        for (int i = 0; i < weights.length; i++) {
            double w = weights[i];
            grad[i] += REGULARIZATION_WEIGHT * Math.signum(w);
        }

        // negate
        for (int i = 0; i < weights.length; i++) {
            grad[i] *= -1;
        }

        return grad;
    }

    private static double[] move(double[] x, double[] dir, double stepsize) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] + dir[i] * stepsize;
        }
        return result;
    }

    private static String printVector(int[] vec) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int v : vec) {
            sb.append(v + ", ");
        }
        sb.append("]");

        return sb.toString();
    }


    private static String printVector(double[] vec) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (double v : vec) {
            sb.append(v + ", ");
        }
        sb.append("]");

        return sb.toString();
    }

    // Finds the weights to minimize the loss function using gradient descent
    // and backtracking line search.
    private static double[] fit(List<ItemCostStats> examples) {
        final double MIN_STEP_SIZE = 1e-6;
        final int MAX_ITERS = 100;

        int dim = examples.get(0).attributes.length;
        double[] weights = new double[dim];
        double bestLoss;

        for (int iters = 0; iters < MAX_ITERS; iters++) {
            double currLoss = loss(examples, weights);

            // find a direction.  Use the negative gradient!
            double[] grad = negLossGradient(examples, weights);

            System.out.println("-------------------");
            System.out.println("iter = " + iters);
            System.out.println("weights = " + printVector(weights));
            System.out.println("grad = " + printVector(grad));
            System.out.println("loss = " + currLoss);

            // find a stepsize
            double stepSize = 1;
            double[] candidateStep = move(weights, grad, stepSize);
            double candidateLoss = loss(examples, candidateStep);
            while (candidateLoss >= currLoss && stepSize > MIN_STEP_SIZE) {
                stepSize *= 0.5;
                candidateStep = move(weights, grad, stepSize);
                candidateLoss = loss(examples, candidateStep);
            }
            System.out.println("stepSize = " + stepSize);

            // stopping criteria
            if (stepSize <= MIN_STEP_SIZE) {
                break;
            }

            // take a step
            weights = candidateStep;
        }

        return weights;
    }

    private static void printResults(List<ItemCostStats> examples, double[] weights) {
        System.out.println("=========== costs from best weights ===");
        for (ItemCostStats e : examples) {
            double estCost = priceItem(e.attributes, weights);
            int estCostR = (int) Math.round(estCost);
            System.out.println(estCostR + "\t" + e.name + "\t" + printVector(e.attributes) + "\t" + e.action + "\t" + e.cost);
        }

        System.out.println("=========== weights ===");
        for (ItemCostStats.Attribute a : ItemCostStats.Attribute.values()) {
            System.out.println(a.getName() + "\t" + weights[a.getIndex()]);
        }
    }

    public static void main(String args[]) throws IOException {
        List<ItemCostStats> examples = StatsLogUtils.readShopData();
        double[] weights = fit(examples);
        printResults(examples, weights);
    }
}


