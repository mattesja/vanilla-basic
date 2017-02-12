import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLTypeResolver;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class HelloWorldAnnotation {

  static Map<String, GraphQLObjectType> registry;

  public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException, InstantiationException {

    registry = new HashMap<String, GraphQLObjectType>();

    GraphQLObjectType rootType = GraphQLAnnotations.object(RootObject.class);

    GraphQLObjectType objectType2 = GraphQLAnnotations.object(MyObject2.class);

    registry.put("MyObject2", objectType2);

    GraphQLObjectType objectType = GraphQLAnnotations.object(MyObject.class);

    registry.put("MyObject", objectType);

    GraphQLSchema schema = GraphQLSchema.newSchema()
        .query(rootType)
        .build(new HashSet(Arrays.asList(rootType, objectType, objectType2)));

    GraphQL graphQL2 = GraphQL.newGraphQL(schema).build();

    // fails due to complex issues in graphql-java-annotations
    Object result2 = graphQL2.execute("{items { ... on MyObject {a, my {b}} ... on MyObject2 {a, b}  }}", new RootObject());
    System.out.println(((ExecutionResultImpl) result2).getErrors());
    System.out.println(((Map)((ExecutionResultImpl) result2).getData()).entrySet());

    // should print: items=[{a=a1, my={b=b1}}, {a=a1, b=b1}]
  }

  public static class RootObject {
    @GraphQLField
    public List<MyInterface> getItems() {
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

    @GraphQLField
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

  @GraphQLTypeResolver(value = MyTypeResolver.class)
  public static interface MyInterface {
    @GraphQLField
    public String getA();
    @GraphQLField
    public String getB();
  }

  public static class MyTypeResolver implements TypeResolver {

    @Override
    public GraphQLObjectType getType(Object object) {
      return registry.get(object.getClass().getSimpleName());
    }
  }


}