package org.example; // ou le package de votre choix
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
public class Calcul {

    private static final int MAX_EXHAUSTIVE_MATCHES = 15;

    private static final int SAMPLING_SIZE = 200000;

    public static int scenToInt(int[] scen) {
        int v = 0;
        for (int x : scen) {
            v = 3*v + x;
        }
        return v;
    }

    public static int[] intToScen(int code, int M) {
        int[] out = new int[M];
        for (int i = M-1; i >= 0; i--) {
            int r = code % 3;
            code = code / 3;
            out[i] = r;
        }
        return out;
    }

    public static int ticketToInt(int[] ticket) {
        return scenToInt(ticket);
    }

    public static int nbUncovered(Collection<Integer> ticketsInt, int M) {
        int total = (int) Math.pow(3, M);
        if (ticketsInt.isEmpty()) {
            return total;
        }
        if (ticketsInt.size() == total) {
            // Tous couverts
            return 0;
        }
        if (M <= MAX_EXHAUSTIVE_MATCHES) {
            int nbUncov = 0;
            for (int scenCode = 0; scenCode < total; scenCode++) {
                if (!ticketsInt.contains(scenCode)) {
                    nbUncov++;
                }
            }
            return nbUncov;
        } else {
            int sampleCount = SAMPLING_SIZE;
            int uncoveredCountInSample = 0;

            for (int i = 0; i < sampleCount; i++) {
                int scenCode = ThreadLocalRandom.current().nextInt(total);
                if (!ticketsInt.contains(scenCode)) {
                    uncoveredCountInSample++;
                }
            }

            double fraction = (double) uncoveredCountInSample / sampleCount;
            return (int) Math.round(fraction * total);
        }
    }


    public static int worstCaseHits(Collection<Integer> ticketsInt, int M) {
        if (ticketsInt.isEmpty()) {
            return 0;
        }
        int total = (int) Math.pow(3, M);
        if (ticketsInt.size() == total) {
            return M;
        }

        if (M <= MAX_EXHAUSTIVE_MATCHES) {
            int worst = M;
            for (int scenCode = 0; scenCode < total; scenCode++) {
                int bestThis = computeBestHitsForScenario(ticketsInt, scenCode, M);
                if (bestThis < worst) {
                    worst = bestThis;
                    if (worst == 0) {
                        break;
                    }
                }
            }
            return worst;

        } else {
            int worst = M;
            int sampleCount = SAMPLING_SIZE;

            for (int i = 0; i < sampleCount; i++) {
                int scenCode = ThreadLocalRandom.current().nextInt(total);
                int bestThis = computeBestHitsForScenario(ticketsInt, scenCode, M);
                if (bestThis < worst) {
                    worst = bestThis;
                    if (worst == 0) break;
                }
            }
            return worst;
        }
    }

    private static int computeBestHitsForScenario(Collection<Integer> ticketsInt,
                                                  int scenCode,
                                                  int M)
    {
        if (ticketsInt.contains(scenCode)) {
            return M;
        }
        return 0;
    }

    public static List<ScenarioCost> kBestScenariosWithCost(double[][] probList, int k) {
        if (k <= 0) {
            return Collections.emptyList();
        }
        int M = probList.length;

        // order[i] contiendra (issue, pIssue) trié par prob décroissante
        List<List<IssueProb>> order = new ArrayList<>();
        List<double[]> costs = new ArrayList<>();
        double[] bestSuffixCost = new double[M+1];

        for (double[] p : probList) {
            List<IssueProb> row = new ArrayList<>();
            for (int issue=0; issue<3; issue++) {
                row.add(new IssueProb(issue, p[issue]));
            }
            row.sort((a,b) -> Double.compare(b.prob, a.prob));
            order.add(row);

            double[] costRow = new double[3];
            for (int iIssue=0; iIssue<3; iIssue++) {
                costRow[iIssue] = -Math.log(row.get(iIssue).prob);
            }
            costs.add(costRow);
        }

        for (int i=M-1; i>=0; i--) {
            bestSuffixCost[i] = bestSuffixCost[i+1] + costs.get(i)[0];
        }

        PriorityQueue<HeapItem> heap = new PriorityQueue<>();
        heap.add(new HeapItem(0.0, 0, new int[0]));

        List<ScenarioCost> results = new ArrayList<>();
        double worstKeptCost = Double.POSITIVE_INFINITY;

        while (!heap.isEmpty() && results.size()<k) {
            HeapItem top = heap.poll();
            double currentCost = top.cost;
            int idx = top.idx;
            int[] partial = top.partial;

            if (currentCost + bestSuffixCost[idx] > worstKeptCost) {
                break;
            }
            if (idx == M) {
                results.add(new ScenarioCost(partial, currentCost));
                if (results.size()==k) {
                    worstKeptCost = currentCost;
                }
                continue;
            }

            double[] costRow = costs.get(idx);
            List<IssueProb> row = order.get(idx);

            for (int rank=0; rank<3; rank++) {
                double newCost = currentCost + costRow[rank];
                double bound   = newCost + bestSuffixCost[idx+1];
                if (bound > worstKeptCost) {
                    continue;
                }
                int newIssue = row.get(rank).issue;
                int[] newPartial = Arrays.copyOf(partial, partial.length+1);
                newPartial[newPartial.length-1] = newIssue;
                heap.add(new HeapItem(newCost, idx+1, newPartial));
            }
        }

        return results;
    }

 static List<ScenarioCost> kBestClosestScenarios(double[][] probList,
                                                           List<List<Integer>> allowedChoices,
                                                           int k) {
        if (k<=0) {
            return Collections.emptyList();
        }
        int M = probList.length;
        if (M!=allowedChoices.size()) {
            throw new IllegalArgumentException("allowed_choices doit avoir la même taille que probList");
        }

        List<List<IssueProb>> order = new ArrayList<>();
        List<double[]> costs = new ArrayList<>();
        double[] bestSuffixCost = new double[M+1];
        int[] minSuffixDist     = new int[M+1];

        for (int i=0; i<M; i++) {
            double[] p = probList[i];
            List<IssueProb> row = new ArrayList<>();
            for (int issue=0; issue<3; issue++){
                row.add(new IssueProb(issue, p[issue]));
            }
            row.sort((a,b)->Double.compare(b.prob,a.prob));
            order.add(row);

            // costs = -log(prob)
            double[] costRow = new double[3];
            for (int r=0; r<3; r++){
                costRow[r] = -Math.log(row.get(r).prob);
            }
            costs.add(costRow);
        }

        for (int i=M-1; i>=0; i--) {
            bestSuffixCost[i] = bestSuffixCost[i+1] + costs.get(i)[0];
            IssueProb bestIssue = order.get(i).get(0);
            int plus = (allowedChoices.get(i).contains(bestIssue.issue)) ? 0 : 1;
            minSuffixDist[i] = minSuffixDist[i+1]+ plus;
        }

        PriorityQueue<HeapItemClosest> heap = new PriorityQueue<>();
        heap.add(new HeapItemClosest(0, 0.0, 0, new int[0]));

        List<ScenarioCost> results = new ArrayList<>();
        double[] worstKeep = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        // (dist, cost) -> bound

        while(!heap.isEmpty() && results.size()<k) {
            HeapItemClosest top = heap.poll();
            int distCur = top.dist;
            double costCur = top.cost;
            int idx = top.idx;
            int[] partial = top.partial;

            double boundDist = distCur + minSuffixDist[idx];
            double boundCost = costCur + bestSuffixCost[idx];
            if (boundDist>worstKeep[0] || (boundDist==worstKeep[0] && boundCost>worstKeep[1])) {
                continue;
            }

            if (idx==M) {
                // scenario complet
                results.add(new ScenarioCost(partial, costCur));
                if (results.size()==k) {
                    worstKeep[0] = distCur;
                    worstKeep[1] = costCur;
                }
                continue;
            }

            double[] costRow = costs.get(idx);
            List<IssueProb> row = order.get(idx);
            for(int rank=0; rank<3; rank++){
                IssueProb ip = row.get(rank);
                int newDist = distCur + (allowedChoices.get(idx).contains(ip.issue)? 0:1);
                double newCost = costCur + costRow[rank];

                double bDist = newDist + minSuffixDist[idx+1];
                double bCost = newCost + bestSuffixCost[idx+1];
                if (bDist>worstKeep[0] || (bDist==worstKeep[0] && bCost>worstKeep[1])){
                    continue;
                }
                int[] newPartial = Arrays.copyOf(partial, partial.length+1);
                newPartial[newPartial.length-1] = ip.issue;
                heap.add(new HeapItemClosest(newDist,newCost,idx+1, newPartial));
            }
        }

        return results;
    }

    public static double[] oddsToProb(double c1, double cN, double c2) {
        double inv1 = 1.0/c1;
        double invN = 1.0/cN;
        double inv2 = 1.0/c2;
        double s = inv1 + invN + inv2;
        return new double[]{ inv1/s, invN/s, inv2/s };
    }

    public static double[] buildDistribution(double[] pMatch) {
        int M = pMatch.length;
        double[] dist = new double[M+1];
        dist[0] = 1.0;
        for(int i=0; i<M; i++){
            double pm = pMatch[i];
            double[] newDist = new double[M+1];
            for(int k=0; k<=M; k++){
                if(dist[k]>0){
                    newDist[k]   += dist[k]*(1 - pm);
                    if(k+1<=M){
                        newDist[k+1]+= dist[k]*pm;
                    }
                }
            }
            dist = newDist;
        }
        return dist;
    }


    public static double[] computeStats(double[] dist) {
        int M = dist.length -1;
        double E=0.0, E2=0.0;
        for(int k=0; k<=M; k++){
            E  += k*dist[k];
            E2 += (k*k)*dist[k];
        }
        double Var = E2 - E*E;
        if(Var<0) Var=0;
        double std = Math.sqrt(Var);
        return new double[]{ E, std };
    }

    private static class IssueProb {
        int issue;
        double prob;
        IssueProb(int issue, double prob){
            this.issue=issue; this.prob=prob;
        }
    }
    private static class HeapItem implements Comparable<HeapItem>{
        double cost;
        int idx;
        int[] partial;

        HeapItem(double cost, int idx, int[] partial){
            this.cost=cost; this.idx=idx; this.partial=partial;
        }

        @Override
        public int compareTo(HeapItem o) {
            return Double.compare(this.cost, o.cost);
        }
    }

    private static class HeapItemClosest implements Comparable<HeapItemClosest>{
        int dist;
        double cost;
        int idx;
        int[] partial;

        HeapItemClosest(int dist, double cost, int idx, int[] partial){
            this.dist=dist; this.cost=cost; this.idx=idx; this.partial=partial;
        }

        @Override
        public int compareTo(HeapItemClosest o){
            int cmp = Integer.compare(this.dist, o.dist);
            if(cmp!=0) return cmp;
            return Double.compare(this.cost, o.cost);
        }
    }


    public static class ScenarioCost {
        public int[] scenario;
        public double cost;
        public ScenarioCost(int[] scenario, double cost){
            this.scenario = scenario;
            this.cost     = cost;
        }
    }
}
