package uk.gov.homeoffice.borders.workflow.task.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.Arrays;

@Data
public class Priority {

    private Type type = Type.STANDARD;

    public boolean withSMS;
    public boolean withEmail;


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
        public static Type fromValue(String type) {
            return Arrays.asList(values()).stream().filter(t -> t.name().
                    equalsIgnoreCase(type)).findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid priority"));
        }


    }


}