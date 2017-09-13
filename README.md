ApiVersioning
=============
[![CircleCI master](https://img.shields.io/circleci/project/github/ChibiTomo/ApiVersioning/master.svg?style=flat)](https://circleci.com/gh/ChibiTomo/ApiVersioning/tree/master) [![Release master](https://img.shields.io/maven-central/v/net.chibidevteam/apiversioning.svg)]()

This library allows you to make your web API versionable (very usefull with REST).

You will be able to do versions through:
- **Path**: OK
- **Header**: [To be done](https://github.com/ChibiTomo/ApiVersioning/issues/1)
- **Parameter**: [To be done](https://github.com/ChibiTomo/ApiVersioning/issues/2)

Requirements
------------

This library works with Spring 4.3.9.

Quick start
-----------

### 1. Add the dependency to maven
```xml
<dependency>
    <groupId>net.chibidevteam</groupId>
    <artifactId>apiversioning</artifactId>
    <version>${apiversioning-version}</version>
</dependency>
```

### 2. Add a component-scan to your Spring configuration
Then, you have to do tell Spring to do a component scan on `net.chibidevteam.apiversioning`.

You can do this with annotations, but do not forget to configure Spring to scan your own package:
```java
@ComponentScan("net.chibidevteam.apiversioning")
@Configuration
public class MyConfigClass {
  (...)
}
```

Or with `applicationContext.xml`:
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-2.5.xsd">
  
  (...)
  
    <context:component-scan base-package="net.chibidevteam.apiversioning" />

  (...)
  
</beans>
```

### 3. Tell ApiVersioning wich versions will be supported
After that, all you have to do is to set `net.chibidevteam.apiversioning.versions.supported` to a coma-separated list of supported versions. You can do it inside your `application.properties`, or inside `apiversioning.properties`.
For Spring-boot users, you can use xml or yml, but only with default Spring file `application.(xml|yml)`.
Eg:
```yml
net.chibidevteam.apiversioning:
  versions:
    supported: 0,1.7,1.8,2.5,3,4
```

**If you do not set the supported versions, there will be a `NoSupportedVersionException` thrown.**

### 4. Aaaand... That's all!
Start creating a Controller with `@ApiRequestMapping`, or `@RequestMapping`, and `@ApiVersion` if you want your endpoint to match only for specifics versions.

Eg: 
With `net.chibidevteam.apiversioning.versions.supported=0,1.7,1.8,2.5,3,4.0`
```java
@Controller
@ApiVersion(">1.5")
@ApiRequestMapping("/example")
public class MyController {

    @ApiVersion("<2.5")
    @ApiRequestMapping("/")
    @ResponseBody
    public String example() {
        return "Hello World";
    }

    @ApiVersion({ ">2.5", "!3" })
    @ApiRequestMapping
    @ResponseBody
    public String newExample() {
        return "Hello in version higher than 2.5!";
    }

    @ApiVersion("3")
    @RequestMapping
    @ResponseBody
    public String exampleV3() {
        return "Hello in version 3!";
    }

    @RequestMapping("/test")
    @ResponseBody
    public String getTest() {
        return "Hello Test";
    }
}
```
This will create the following endpoints:
- `example` will be mapped to `/api/v1.7/example/` and `/api/v1.8/example/`
- `newExample` will be mapped to `/api/example`, `/api/v2.5/example` and `/api/v4/example`
- `exampleV3` will be mapped to `/api/v3/example`
- `getTest` will be mapped to `/api/example/test`, `/api/v1.7/example/test`, `/api/v1.8/example/test`, `/api/v2.5/example/test`, `/api/v3/example/test` and `/api/v3.5/example/test`

- Any request that match `/api/v0/**`, as any non mapped endpoint, will leads to a 404 HTTP error.
- Any request that match `/api/{apiVersion}/**` with unsupported version, will leads to a `ConstraintViolationException`. 
If not handled, this will leads to a 500 HTTP error.

Configurations
--------------

You can configure your API with differents properties that will be used by ApiVersioning.
Here are the default values:

```yml
net.chibidevteam.apiversioning:
  path:
    base: /api
    varname: apiVersion
    api: ${net.chibidevteam.apiversioning.path.base}/{${net.chibidevteam.apiversioning.path.varname}}
    prefix: v
  versions:
    regex: (\d+)(?:\.(\d+))?(?:\.(\d+))?(.*)
    supported:
```

- net.chibidevteam.apiversioning.**path._base_**: This is the base path of your API. It is used for the default value of `net.chibidevteam.apiversioning.path.api`.
- net.chibidevteam.apiversioning.**path._varname_**: This is the path-variable name that you want to use. If you want, you will be able to use it with annotations like `@PathVariable`.
- net.chibidevteam.apiversioning.**path._api_**: All request handler mapped with `@ApiRequestMapping` will automaticaly be placed behind this path. I recommend to leave this value untouched as it is quite intuitive by default: /your/base/path/{apiVersion}. 
*Default: `/api/{apiversion}`*
- net.chibidevteam.apiversioning.**path._prefix_**: This is the version prefix that you want in your path. By default it is `v`. It means that a version **MUST** starts with `v` to be valid. Eg: `/api/v1.6` is valid, but not `/api/1.6`. If you do not want prefix, just leave it empty.
- net.chibidevteam.apiversioning.**versions._regex_**: This is the format of your versions. It will be used to parse the version, so **do not forget the capturing groups**!
- net.chibidevteam.apiversioning.**versions._supported_**: You already know this one. It is the only **madatory** property. It has to be a coma-separated list of versions.

Mapping
-------

All you have to do is to use `@RequestMapping` as usual. If you want your handler to be correctly mapped, make sure you use the  `net.chibidevteam.apiversioning.path.api` as the base path. 

If you do not want to rewrite the base path each time, you should use `@ApiRequestMapping`. You can use it exactly the same way as `@RequestMapping`. It effect is to prepend `net.chibidevteam.apiversioning.path.api` to the path that will be used.

**WARNING**: All of the following examples will be mapped to `/api/{apiVersion}/type/method`:
`@ApiRequestMapping` on both type and method:
```java
@Controller
@ApiRequestMapping("/type")
public class MyController {

    @ApiRequestMapping("/method")
    @ResponseBody
    public String example() {
        return "Hello World";
    }
    
}
```
`@ApiRequestMapping` only on type:
```java
@Controller
@ApiRequestMapping("/type")
public class MyController {

    @RequestMapping("/method")
    @ResponseBody
    public String example() {
        return "Hello World";
    }
    
}
```
`@ApiRequestMapping` only on method:
```java
@Controller
@RequestMapping("/type")
public class MyController {

    @ApiRequestMapping("/method")
    @ResponseBody
    public String example() {
        return "Hello World";
    }
    
}
```

If you use both `@RequestMapping` and `@ApiRequestMapping` on the same element, `@ApiRequestMapping` will be the only annotation used. `@RequestMapping` will be ignored.

Version restriction
-------------------

To restrict your handle to certain versions, you have to use `@ApiVersion`. Its parameter is a String, or an array of String, that represent allowed versions.

### 1. Version representation

First the version must match the regex pattern setted in `net.chibidevteam.apiversioning.versions.regex`.
Then, depending on how you want to map your handler, you can use the following prefixes:

* Exact: nothing or `=`, the requested version must be exactly this.
* Exclude: `!`, the requested version must not be exactly this.
* Compatibility: `^`, the requested version must be greater or equal to this and smaller than the next major.
* Superiority: `>`, the requested version must be greater or equal to this.
* Inferiority: `<`, the requested version must be strictly smaller than this.

### 2. Matching rules

You can set any number of version representation in `@ApiVersion`. However, you have to take the following rules in considerations:
* If there is multiple exact versions, the requested version must match at least one.
* The same applies for compatibility representations.
* If there is exclude versions, the requested version must match all of them.
* If there is a mix between superiority and/or inferiority, the requested version must match all of them.
* All of the preceding rules apply together.

You can put version representation in any order.

Eg: with supported versions: 0, 1.7, 1.8, 2.5, 3 and 4.0
* `@ApiVersion("1.7")` allows 1.7
* `@ApiVersion("!1.7")` allows 0, 1.8, 2.5, 3 and 4.0
* `@ApiVersion("^1.5")` allows 1.7 and 1.8
* `@ApiVersion(">1.5")` allows 1.7, 1.8, 2.5, 3 and 4.0
* `@ApiVersion("<1.5")` allows 0
* `@ApiVersion({"<2.5", "!1.7"})` allows 0, 1.8 and 2.5
* `@ApiVersion({"<2.5", "^1.5"})` allows 1.7 and 1.8
* `@ApiVersion({"<2.5", ">1.7"})` allows 1.7 and 1.8
* `@ApiVersion({"<2.5", "^3"})` allows nothing

### 3. Type restriction

It means that if a type restrict versions, its method will not be able to see other versions.

Eg: with supported versions: 0, 1.7, 1.8, 2.5, 3 and 4.0
If type has `@ApiVersion("<1.8")`, it means that its methods can only see versions 0 and 1.7
So, if a method has `@ApiVersion("^2.5")`, the method will not be mapped to any version.
However, if a method has `@ApiVersion("^1.7")`, the method will be mapped to version 1.7 only.

Limitations and warnings
------------------------

The logic on how ApiVersioning uses and compares version is the one behind (Semantic Versioning)[http://semver.org/]. 

It was build wis this logic in mind. Any 'RC1', 'alpha', 'SNAPSHOT' or others will be dropped in the `others` property of the `Version` class and will be ignored.
It means that `4.3.9.RELEASE`, `4.3.9-RC2`, `4.3.9-SNAPSHOT`, `4-PRD.3.9-dev` (with regex `(\d+)-(.{3})(?:\.(\d+))?(?:\.(\d+))?(.*)`) will recognized as `4.3.9` and will all be the same version.

As long as your pattern respect the order, type and logic of Major, then Minor and then Patch, you will be able to use this library.

Please take in account that as ApiVersioning can **ONLY** compares Major, Minor and Patch in this order, it is not recommended to change the version regex.

F.A.Q.
------

### Why is 'net.chibidevteam.apiversioning.versions.supported' mandatory? I do not want to support any version for now...
It is simple: if you do not set this, it can leads to ambiguous mapping of your request handlers. It is not a bug from ApiVersioning.

For example, if you try to handle `/endpoint` with a method for v1 and another for v2, they will be both mapped to empty, it means to root. From here, Spring complains about ambiguous mapping.
