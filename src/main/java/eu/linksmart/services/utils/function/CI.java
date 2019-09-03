package eu.linksmart.services.utils.function;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by José Ángel Carvajal on 03.09.2019 a researcher of Fraunhofer FIT.
 */
public abstract class CI {
    private static final Set<String> mark = new HashSet<>();
    public static void ciCollapseStartMark(String label){
        if (System.getenv().containsKey("CI"))
            System.out.println("travis_fold:start:"+label);
    }
    public static void ciCollapseEndMark(String label){
        if (System.getenv().containsKey("CI"))
            System.out.println("travis_fold:end:"+label);
    }
    public static void ciCollapseMark(String label){
        if (mark.contains(label)){
            ciCollapseEndMark(label);
            mark.remove(label);
        }else {
            ciCollapseStartMark(label);
            mark.add(label);
        }
    }

}
