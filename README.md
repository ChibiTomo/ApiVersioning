ApiVersioning
=============

This library allows you to make your web API versionable (very usefull with REST).
You will be able to do versions through:
- **Path**: OK
- **Header**: [To be done](https://github.com/ChibiTomo/ApiVersioning/issues/1)
- **Parameter**: [To be done](https://github.com/ChibiTomo/ApiVersioning/issues/2)

Quick start
-----------

First of all, you have to do tell spring to do a component scan on `net.chibidevteam.apiversioning`.

You can do this with annotations:
```java
(...)
@ComponentScan("net.chibidevteam.apiversioning")
@configuration
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

After that, all you have to do is to set `net.chibidevteam.apiversioning.versions.supported` to a coma-separated list of supported versions, and start creating a Controller with `@ApiRequestMapping` instead of `@RequestMapping` and `@ApiVersion` if you want your endpoint valid only for matching versions.

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

Any request that match `/api/v0/**` will leads to a 404 HTTP error.
Any request that match `/api/{apiVersion}/**` witf unsupported version, will leads to a 400 HTTP error.