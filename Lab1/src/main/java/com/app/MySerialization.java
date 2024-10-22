package com.app;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySerialization {

    public static String serialize(Object object, Class<?> clazz) {
        StringBuilder serializedData = new StringBuilder();
        serializeFields(object, serializedData, clazz);
        serializedData.append("$");
        return serializedData.toString();
    }

    public static Object deserialize(Object object, Class<?> clazz, String data) {
        Map<String, String> fieldValues = new HashMap<>();

        String listRegex = "(\\w+)=>([^$]+\\$[^$]+\\$*\\$)";

        Pattern listPattern = Pattern.compile(listRegex, Pattern.DOTALL);
        Matcher listMatcher = listPattern.matcher(data);

        if (listMatcher.find()) {
            String key = listMatcher.group(1);
            String listValue = listMatcher.group(2).trim();
//            System.out.println("List Value: " + listValue);
            fieldValues.put(key, "=>" + listValue);
        }

        String remainingFields = data;
        if (data.contains("$$")) {

            remainingFields = data.split("\\$\\$")[1];
            if (listMatcher.find()) {
                remainingFields = data.substring(listMatcher.end()).trim();
            }
        }
        String[] remainingPairs = remainingFields.split("&");

        for (String pair : remainingPairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                fieldValues.put(key, value);
            }
        }

//        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
//            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
//        }

        deserializeFields(object, fieldValues, clazz);
        return object;
    }


    private static void serializeFields(Object obj, StringBuilder serializedData, Class<?> clazz) {
        try {
            if (clazz.getSuperclass() != null) {
                serializeFields(obj, serializedData, clazz.getSuperclass());
            }

            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(obj);

                if (value instanceof LocalDateTime) {
                    value = ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } else if (value instanceof List<?>) {
                    serializedData.append(field.getName()).append("=>");
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        serializedData.append(serialize(item, item.getClass()));
                    }
                    serializedData.append("$");
                    if (!list.isEmpty()) {
                        serializedData.setLength(serializedData.length() - 1);
                    }
                    serializedData.append("$");
                    continue;
                }

                serializedData.append(field.getName()).append("=").append(value).append("&");
            }

            if (serializedData.length() > 0) {
                serializedData.setLength(serializedData.length() - 1);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error during serialization: " + e.getMessage(), e);
        }
    }

    private static void deserializeFields(Object obj, Map<String, String> fieldValues, Class<?> clazz) {
        try {

            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldValue = fieldValues.get(fieldName);
//                System.out.println("===================================");
//                System.out.println("Field Name: " + fieldName + ", Field Value: " + fieldValue);

                if (fieldValue.endsWith("$")) {
                    fieldValue = fieldValue.substring(0, fieldValue.length() - 1);
                }

                if (field.getType() == double.class || field.getType() == Double.class) {
                    field.set(obj, Double.parseDouble(fieldValue.trim()));
                } else if (field.getType() == LocalDateTime.class) {
                    field.set(obj, LocalDateTime.parse(fieldValue.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } else if (List.class.isAssignableFrom(field.getType())) {
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        Class<?> listType = (Class<?>) pt.getActualTypeArguments()[0];

                        List<Object> list = new ArrayList<>();
                        String listContent = fieldValue.trim();

//                        System.out.println("List: " + listContent);

                        if (listContent.startsWith("=>")) {
                            listContent = listContent.substring(2, listContent.length() - 1);
                            String[] items = listContent.split("\\$");

//                            System.out.println("Items: " + Arrays.toString(items));

                            for (String item : items) {
                                if (!item.trim().isEmpty()) {
                                    Object listItem = listType.getDeclaredConstructor().newInstance();

                                    String[] itemFields = item.split("&");

//                                    System.out.println("Item Fields: " + Arrays.toString(itemFields));

                                    Map<String, String> itemFieldValues = new HashMap<>();
                                    for (String itemField : itemFields) {
                                        String[] itemFieldParts = itemField.split("=");
                                        itemFieldValues.put(itemFieldParts[0], itemFieldParts[1]);
                                    }

//                                    System.out.println("Item Field Values: " + itemFieldValues);

                                    deserializeFields(listItem, itemFieldValues, listType);
                                    list.add(listItem);
                                }
                            }
                        }
                        field.set(obj, list);
                    }
                } else {
                    field.set(obj, fieldValue.trim());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error during deserialization: " + e.getMessage(), e);
        }
    }


}
