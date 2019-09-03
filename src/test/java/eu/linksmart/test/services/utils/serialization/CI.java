package eu.linksmart.test.services.utils.serialization;

/**
 * Created by José Ángel Carvajal on 03.09.2019 a researcher of Fraunhofer FIT.
 */
public abstract class CI {
    public static void ciCollapseStartMark(String label){
        System.out.println("travis_fold:start:"+label);
    }
    public static void ciCollapseEndMark(String label){
        System.out.println("travis_fold:end:"+label);
    }
}
