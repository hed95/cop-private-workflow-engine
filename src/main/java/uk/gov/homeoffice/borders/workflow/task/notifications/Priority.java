package uk.gov.homeoffice.borders.workflow.task.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Data;

import java.io.IOException;
import java.util.Arrays;

@Data
public class Priority {

    private Type type = Type.STANDARD;
    private boolean notificationBoost;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @JsonDeserialize(using = Type.TypeDeserializer.class)
    public enum Type {
        STANDARD(50),
        URGENT(100),
        EMERGENCY(1000);

        private int value;

        Type(int value) {
            this.value = value;
        }

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

        public static class TypeDeserializer extends JsonDeserializer<Type> {

            @Override
            public Type deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                ObjectCodec oc = p.getCodec();
                TreeNode treeNode = oc.readTree(p);
                if (treeNode instanceof ObjectNode) {
                    ObjectNode objectNode = (ObjectNode) treeNode;
                    return Type.fromValue(objectNode.get("value").asInt());
                } else {
                    TextNode textNode = (TextNode)treeNode;
                    return Type.fromType(textNode.textValue());
                }

            }
        }

    }


}