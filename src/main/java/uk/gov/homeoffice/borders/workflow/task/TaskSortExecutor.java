package uk.gov.homeoffice.borders.workflow.task;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class TaskSortExecutor {

    public void applySort(TaskQuery taskQuery, Sort sort) {
        ofNullable(sort.getOrderFor("taskName")).ifPresent(s -> {
            if (s.isAscending()) {
                taskQuery.orderByTaskName().asc();
            } else {
                taskQuery.orderByTaskName().desc();
            }
        });

        ofNullable(sort.getOrderFor("created")).ifPresent(s -> {
            if (s.isAscending()) {
                taskQuery.orderByTaskCreateTime().asc();
            } else {
                taskQuery.orderByTaskCreateTime().desc();
            }
        });

        ofNullable(sort.getOrderFor("dueDate")).ifPresent(s -> {
            if (s.isAscending()) {
                taskQuery.orderByDueDate().asc();
            } else {
                taskQuery.orderByDueDate().desc();
            }
        });
        ofNullable(sort.getOrderFor("assignee")).ifPresent(s -> {
            if (s.isAscending()) {
                taskQuery.orderByTaskAssignee().asc();
            } else {
                taskQuery.orderByTaskAssignee().desc();
            }
        });
        ofNullable(sort.getOrderFor("priority")).ifPresent(s -> {
            if (s.isAscending()) {
                taskQuery.orderByTaskPriority().asc();
            } else {
                taskQuery.orderByTaskPriority().desc();
            }
        });

    }
}
