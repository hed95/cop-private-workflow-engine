package uk.gov.homeoffice.borders.workflow.event;

import org.camunda.bpm.engine.impl.history.AbstractHistoryLevel;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;

import java.util.ArrayList;
import java.util.List;

public class CustomHistoryEventLevel extends AbstractHistoryLevel implements HistoryLevel {

    public static final CustomHistoryEventLevel INSTANCE = new CustomHistoryEventLevel();

    private static List<HistoryEventType> eventTypes = new ArrayList<>();

    static {
        eventTypes.add(HistoryEventTypes.PROCESS_INSTANCE_START);
        eventTypes.add(HistoryEventTypes.PROCESS_INSTANCE_END);
        eventTypes.add(HistoryEventTypes.TASK_INSTANCE_CREATE);
        eventTypes.add(HistoryEventTypes.TASK_INSTANCE_COMPLETE);
        eventTypes.add(HistoryEventTypes.TASK_INSTANCE_UPDATE);
    }

    public static HistoryLevel getInstance() {
        return INSTANCE;
    }

    @Override
    public int getId() {
        return 222;
    }

    @Override
    public String getName() {
        return "cop-history-level";
    }

    @Override
    public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
        return eventTypes.contains(eventType);

    }
}
