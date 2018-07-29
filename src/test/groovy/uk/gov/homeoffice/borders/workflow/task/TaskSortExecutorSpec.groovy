package uk.gov.homeoffice.borders.workflow.task

import org.camunda.bpm.engine.impl.Direction
import org.camunda.bpm.engine.impl.QueryOrderingProperty
import org.camunda.bpm.engine.impl.TaskQueryImpl
import org.springframework.data.domain.Sort
import spock.lang.Specification
import spock.lang.Unroll

class TaskSortExecutorSpec extends Specification {

    TaskSortExecutor underTest = new TaskSortExecutor()
    TaskQueryImpl taskQuery = null

    def setup() {
        taskQuery = new TaskQueryImpl()
    }

    @Unroll
    def 'can sort by #field and #direction and taskQuery has sort properties size == #size'(field, propertyName, direction, size) {

        expect:
        def sort = Direction.ASCENDING ? Sort.by(field).ascending() : Sort.by(field).descending()
        underTest.applySort(taskQuery, sort)
        taskQuery.getOrderingProperties().size() == size
        def orderingProperty = taskQuery.getOrderingProperties().first() as QueryOrderingProperty
        orderingProperty.getQueryProperty().name ==  propertyName

        where:
        field           | propertyName                      | direction                 | size
        "taskName"      | "NAME_"                           | Direction.ASCENDING       | 1
        "created"       | "CREATE_TIME_"                    | Direction.ASCENDING       | 1
        "taskName"      | "NAME_"                           | Direction.DESCENDING      | 1
        "created"       | "CREATE_TIME_"                    | Direction.DESCENDING      | 1
        "dueDate"       | "DUE_DATE_"                       | Direction.DESCENDING      | 1
        "dueDate"       | "DUE_DATE_"                       | Direction.ASCENDING       | 1
        "priority"      | "PRIORITY_"                       | Direction.ASCENDING       | 1
        "priority"      | "PRIORITY_"                       | Direction.DESCENDING      | 1
        "assignee"      | "ASSIGNEE_"                       | Direction.ASCENDING       | 1
        "assignee"      | "ASSIGNEE_"                       | Direction.DESCENDING      | 1
    }
}
