A fast ~~and furious~~ and safe library for evaluating user-defined (e.g. from configuration or an API) Java[TM] expressions at runtime using [Janino](https://janino-compiler.github.io/janino).

With this library you can:

- make your application flexible with fast user-defined business rules from configuration or an API or ...
- build predicates and arithmetic expressions at runtime and evaluates them as fast as compiled code does

As documentation, please use [JavaExpressionEvaluator](src/main/kotlin/alexeilijin/evaluation/expression/janino/JavaExpressionEvaluator.kt). 

As a usage example, please use [JavaExpressionEvaluatorTest](src/test/kotlin/alexeilijin/evaluation/expression/janino/JavaExpressionEvaluatorTest.kt).

You can also use this library to get a list of classes from the java.base module.