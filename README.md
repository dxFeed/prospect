# Prospect: Property Specification Library

Type-safe and composable configuration properties for Kotlin.

Notable features include:
* Zero runtime dependencies
* Type-safety and explicit nullability
* Rich defaults and fully customizable core
* Substitution of environment variables and system properties
* Support for override files

## Prospect in Action

This section demonstrates a use-case for the library.
There is also an [introduction](#introduction) that explains core concepts in more detail.

Suppose we have the following properties file `config.properties`:

```properties
verbose=true
worker.count=64
levels=1:1:2:3:5
endpoint.login=admin@acme
endpoint.password=${ENDPOINT_PASSWORD}
```

Let us break down what these example properties mean:
- `verbose` - a boolean flag, that should be false by default;
- `worker.count` - a required positive integer that must be a power;
- `levels` - a non-empty list of numbers separated by colons;
- `endpoint.login` - a login that must end with `@acme`;
- `endpoint.password` - an optional password whose value must not be logged.

This is how the properties can be declaratively described with Prospect in Kotlin:
```kotlin
class AppConfig : Props() {

    /**
     * Expects an optional flag property called `verbose`,
     * using default value of false if property is not provided.
     *
     * Name of the property is taken from the name of the class member.
     */
    val verbose: Boolean
            by propertyOfBoolean(default = false)

    /**
     * Expects a required numeric property called `worker.count`,
     * checking if provided value is positive and if provided value is
     * a power of 2.
     */
    val workerCount: Int
            by propertyOfInt("worker.count").checkPositive()
                .check("expected power of 2") { it.countOneBits() == 1 }

    /**
     * Expects a required property called `levels`.
     *
     * Property string is split by custom separator,
     * and each item is parsed by a given parser.
     *
     * Additionally, the list is checked to be not empty.
     */
    val levels: List<Int>
            by propertyOfList(separator = ":") { it.toInt() }
                .checkNotEmpty()

    /**
     * More properties are nested with prefix `endpoint`.
     * In this case the resulting properties are:
     * - `endpoint.login`
     * - `endpoint.password`
     */
    val endpoint: EndpointConfig
            by nested("endpoint") { EndpointConfig() }

}

class EndpointConfig : Props() {

    /**
     * Expects a required string value.
     *
     * Checks that the value conforms to a given regex.
     */
    val login: String
            by propertyOfString("login")
                .checkMatches(".*@acme$".toRegex())

    /**
     * Expects an optional string value, that is masked with `*****`
     * whenever the properties are formatted for printing.
     */
    val password: String?
            by propertyOfPassword("password").optional()

}
```

Using this class we can read properties from files and verify them while loading.

The following code will load the properties from the `app.properties` file.
If the properties are correct and all checks have passed, we can access
each property in the config as regular members.

```kotlin
fun main() {
    val config: AppConfig = AppConfig().loadOverridableFromFile("app.properties")
    println(config.toMultilineString())
}
```

By default, `loadOverridableFromFile` will also look for an override-file, named
`app.properties.override` in this case. Any properties in the override file
will have precedence over properties in the initial file.

Notice that in the end we print out the config as a multi-line string.
This is useful, for example, for logging the final configuration when starting a service.

This will produce the following output:
```
verbose = true
worker.count = 64
levels = [1, 1, 2, 3, 5]
endpoint.login = admin@acme
endpoint.password = *****
```

## Introduction

This section describes core concepts of the library along with usage patterns.

There are only few things we need to do to start using Prospect.

* Declare a class that extends `Props` and specifies all our properties.
* Convert original property keys and values into `FlatProps`.
* Load the flat values into a props instance by calling `load` function or an alternative.

### `Props`

Whenever we need to declare our properties, we do it in our own class that extends `Props`.

```kotlin
class MyProps : Props() {
    val port by propertyOfInt("port", default = 8080)
}
```

Within the scope of the child class we get access to core primitives of property specification.
However, it is rarely needed to use those directly as there are various extension functions
that let us declare properties via member delegation mechanism.

Here are some examples of the helper functions that can be used with `by` construct:
`propertyOfString`, `propertyOfPassword`, `propertyOfList`.
We can explore all provided shortcut functions by typing `propertyOf` in the IDE.

### Building Properties

When declaring properties we have full control of how they will be resolved.

At the core of it is the `property` function that returns a `PropBuilder`.
As we would expect, the builder lets us customize the property by chaining calls.

```kotlin
val customPassword by property<String>("password")
    .parse { it.trim('#') }
    .withDefault { System.getenv("test.password") }
    .check { it.length > 10 }
    .format { "<hidden>" }
```

Since `PropBuilder` implements `provideDelegate` function, it means that its instance can be used
by Kotlin to extract a member delegate.

A special mention is required for the `optional` function available for the `PropBuilder`.
It is only possible to call this function as the last in the chain as it turns the builder
into an `OptionalPropBuilder`. The latter does not have any customization functions to preserve type-safety.

```kotlin
val logFile: String? by propertyOfString("log.file").optional()
```

In contrast to regular properties, optional properties return the value of `null` if their values
were not present during loading from `FlatProps`.

### `FlatProps`

In order to load raw properties into an instance of `Props` we need to give it `FlatProps`.

The idea of flat properties is that regardless of the original format the properties are
represented as a list of key-value pairs. Both keys and values are runtime strings,
and there are no duplicate keys to remove ambiguity. Optionally, each flat value can have
a specified `source` which will be used in error reports to aid in troubleshooting.

In the simplest case, if we have a map from strings to strings, we can convert it to a `MapFlatProps`
implementation by calling `myMap.toFlatProps()`. The keys and values are trimmed by default.

```kotlin
val myMap = mapOf("prop1" to " value1 ", " prop2" to "value2")
val flatProps = myMap.toFlatProps()
```

Normally, we do not need to explicitly handle the flat properties. Instead, we use helper functions
that let us read and load properties at the same time. For instance, we can call `loadFromFile`
function on an instance of `Props` to read and load a properties file.

### Property Loading

The following table describes loading process of a property value from flat properties.

| Flat Value \ Property | Non-Optional | Optional |
| --- | --- | --- |
| `null` | has default => _default value_ <br/> no default => **error** | has default => _default value_ <br/> no default => `null` |
| _empty_ | **error** | `null` |
| _non-empty_ | _parsed value_ | _parsed value_ |

If the parser returns `null`, then loading continues as if the flat value was initially _empty_.
If `useOnEmpty` is true for a default value of a property,
then for _empty_ flat values loading continues as if the flat value was initially `null`.
