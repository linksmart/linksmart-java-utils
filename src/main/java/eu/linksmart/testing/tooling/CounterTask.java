package eu.linksmart.testing.tooling;

import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by José Ángel Carvajal on 22.12.2016 a researcher of Fraunhofer FIT.
 */
public class CounterTask extends TimerTask {

    volatile  private long i = 0,n =1, total;
    protected Logger loggerService = null;
    volatile private Map<String, Long> counters = new ConcurrentHashMap<>();
    final private boolean topicCounter;

    public CounterTask() {
        topicCounter = false;
    }

    public CounterTask(Logger logger, boolean topicCounter) {
        loggerService = logger;
        this.topicCounter =topicCounter;
    }

    public  void newEventInTopic(String topic) {
        if(topicCounter) {
            if (!counters.containsKey(topic))
                counters.put(topic, 0L);
            counters.put(topic, counters.get(topic) + 1);
        }
        i++;
    }


    public void run() {
        double avg;
        long messages;

        String counterStr;

        if((messages = i) == 0)
            return;

        i=0;
        total += messages;

        avg = total / n;
        n++;



        counterStr = counters.entrySet()
                .stream()
                .map(entry -> "\"" + entry.getKey() + "\"" + " : " + entry.getValue())
                .collect(Collectors.joining(", "));


        String message = "{\"total\": " + String.valueOf(total) +
                // ", \"lapsed\": "+(after.getTime()-before.getTime())/1000.0+
                ", \"messages\": " + String.valueOf(messages) +
                ", \"avg\": " + String.valueOf(avg) +
                ", \"time\":\"" + Instant.now().toString() +"\""+
                ((topicCounter)?", \"counters\":{ " + counterStr + " } " :"")+
                "}";
        if (loggerService != null)
            loggerService.info(message);
        else
            System.out.println(message);


    }

}