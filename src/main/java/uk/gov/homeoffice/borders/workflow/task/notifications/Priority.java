package uk.gov.homeoffice.borders.workflow.task.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.Arrays;

@Data
public class Priority {

    private Type type = Type.STANDARD;
    private boolean notificationBoost;

    @JsonFormat(shape=JsonFormat.Shape.OBJECT)
    public enum Type {
        STANDARD(50),
        URGENT(100),
        EMERGENCY(1000);

        private int value;

        Type(int value) {
            this.value = value;
        }

        @JsonValue
        @JsonProperty("value")
        public int getValue() {
            return value;
        }


        @JsonCreator
        public static Type fromValue(int value) {
            return Arrays.stream(values()).filter(t -> t.value == value).findAny().orElseThrow(() -> new IllegalArgumentException("Invalid priority"));
        }

        @JsonCreator
        public static Type fromType(String type) {
            return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(type)).findAny().orElseThrow(() -> new IllegalArgumentException("Invalid priority"));
        }



    }


}