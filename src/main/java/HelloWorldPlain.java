import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.schema.*;

import java.util.*;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class HelloWorldPlain {

  static Map<String, GraphQLObjectType> registry;

  public static void main(String[] args) {

    registry = new HashMap<String, GraphQLObjectType>();

    GraphQLInterfaceType interfaceType = GraphQLInterfaceType.newInterface()
        .name("MyInterface")
        .typeResolver(new TypeResolver() {
          @Override
          public GraphQLObjectType getType(Object object) {
            return registry.get(object.getClass().getSimpleName());
          }
        })
        .build();

    GraphQLObjectType rootType = newObject()
        .name("RootObject")
        .field(
            newFieldDefinition()
                .name("items")
                .type(GraphQLList.list(interfaceType)))
        .build();

    GraphQLObjectType objectType2 = newObject()
        .name("MyObject2")
        .field(
            newFieldDefinition()
                .name("a")
                .type(GraphQLString))
        .field(newFieldDefinition()
            .name("b")
            .type(GraphQLString))
        .withInterface(interfaceType)
        .build();

    registry.put("MyObject2", objectType2);

    GraphQLObjectType objectType = newObject()
        .name("MyObject")
        .field(
            newFieldDefinition()
                .name("a")
                .type(GraphQLString))
        .field(newFieldDefinition()
            .name("b")
            .type(GraphQLString))
        .field(newFieldDefinition()
            .name("my")
            .type(objectType2))
        .withInterface(interfaceType)
        .build();

    registry.put("MyObject", objectType);

    GraphQLSchema schema = GraphQLSchema.newSchema()
        .query(rootType)
        .build(new HashSet(Arrays.asList(rootType, objectType, objectType2)));

    GraphQL graphQL2 = GraphQL.newGraphQL(schema).build();

    Object result2 = graphQL2.execute("{items { ... on MyObject {a, my {b}} ... on MyObject2 {a, b}  }}", new RootObject());
    System.out.println(((ExecutionResultImpl) result2).getErrors());
    System.out.println(((Map)((ExecutionResultImpl) result2).getData()).entrySet());

    // Prints: items=[{a=a1, my={b=b1}}, {a=a1, b=b1}]
  }

  public static class RootObject {
    public List<? extends MyInterface> getItems() {
      return Arrays.asList(new MyObject(), new MyObject2());
    }
  }

  public static class MyObject implements MyInterface {
    public String getA() {
      return "a1";
    }

    public String getB() {
      return "b1";
    }

    public MyObject2 getMy() {
      return new MyObject2();
    }
  }

  public static class MyObject2 implements MyInterface {
    public String getA() {
      return "a2";
    }

    public String getB() {
      return "b2";
    }
  }

  public static interface MyInterface {
    public String getA();
    public String getB();
  }

}