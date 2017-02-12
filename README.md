* HelloWorldPlain uses plain graphql-java
* HelloWorldAnnotation uses plain graphql-java-annotation

Both are using the same dummy beans and the same query.
Unfortunately graphql-java-annotation fails due to complex issues regarding the interface handling.

I discovered these issues:

* query with fragments fails ({items { ... on MyObject {a, my {b}} ... on MyObject2 {a, b}  }})
  Error: PossibleFragmentSpreads.doTypesOverlap() returns false, because fragType + parentType reference to different interface instances.
* List must not use Wildcard return types e.g. List<? extends MyInterface>
* GraphQLAnnotations.getObjectBuilder() uses object.getInterfaces() which only returns direct interfaces of that class.
  Here ClassUtils.getAllInterfaces etc. should be used http://stackoverflow.com/questions/6616055/get-all-derived-interfaces-of-a-class