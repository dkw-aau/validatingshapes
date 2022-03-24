package cs.qse;

import cs.utils.Tuple2;
import cs.utils.Tuple3;
import org.semanticweb.yars.nx.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to compute stats of cardinality constraints.
 */
public class StatsComputer {
    Map<Tuple3<Integer, Integer, Integer>, EntityCount> shapeTripletStats; // Size O(T*P*T)
    Map<Integer, Set<Integer>> propWithClassesHavingMaxCountOne; // Size O(P*T)
    
    public StatsComputer() {this.propWithClassesHavingMaxCountOne = new HashMap<>();}
    
    /**
     * This method is used to compute entity count
     */
    public void computeStatistics(Map<Node, EntityData> entityDataHashMap, Map<Integer, Integer> classEntityCount) {
        
        entityDataHashMap.forEach((entity, entityData) -> {
            Set<Integer> instanceClasses = entityDataHashMap.get(entity).getClassTypes();
            if (instanceClasses != null) {
                for (Integer c : instanceClasses) {
                    for (Tuple2<Integer, Integer> propObjTuple : entityData.getPropertyConstraints()) {
                        Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3<>(c, propObjTuple._1, propObjTuple._2);
                        EntityCount sc = this.shapeTripletStats.get(tuple3);
                        if (sc == null) {
                            this.shapeTripletStats.put(tuple3, new EntityCount(1));
                        } else {
                            Integer newSupp = sc.getEntityCount() + 1;
                            sc.setEntityCount(newSupp);
                            this.shapeTripletStats.put(tuple3, sc);
                        }
                    }
                }
            }
            
        });
    }
    
    //Setters
    public void setShapeTripletStats(Map<Tuple3<Integer, Integer, Integer>, EntityCount> shapeTripletStats) {
        this.shapeTripletStats = shapeTripletStats;
    }
}
