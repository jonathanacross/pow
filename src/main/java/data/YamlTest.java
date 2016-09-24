package data;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlTest {

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        Animal myDog = new Dog("rufus","english shepherd");
        Animal myCat = new Cat("goya", "mice");

        try {
            String dogJson = objectMapper.writeValueAsString(myDog);
            System.out.println(dogJson);
            Animal deserializedDog = objectMapper.readValue(dogJson, Animal.class);
            System.out.println("Deserialized dogJson Class: " + deserializedDog.getClass().getSimpleName());
            String catJson = objectMapper.writeValueAsString(myCat);
            System.out.println(catJson);
            Animal deserializedCat = objectMapper.readValue(catJson, Animal.class);
            System.out.println("Deserialized catJson Class: " + deserializedCat.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
