# Introduction #

Organic growth is so...well...natural!

Crank is an open source project providing both a generic validation framework, and code generation facilities for the JSF/Spring/Hibernate stack, which is very popular in corporate America today. Crank is not a Grails project, although it initially was designed to provide CRUD application UI to databases. It can be further extended to a more feature rich application because it allows you to add additional JSF components to your application above and beyond just Crank. That is because it is a JSF application framework. In some ways it is similar to JBoss Seam with just a different approach.

# Working Features Of Validation Framework #

Mostly what works is the annotation driven validation.

  * Java 5 annotations
  * Allows for multiple sources of Validation meta-data (Annotation, Property files)
  * You can pick one or use more than one at a time
  * Annotation and Property file is supported
  * JSF Integration via JSF Validation integrtation
  * Spring MVC/Spring Webflow Integration via Spring Validator
  * Can Generate JavaScript based on Validation metadata
  * Can make Ajax calls based on Validation metadata
  * Easy to integrate with existing annotation driven frameworks

# Working Features Of CRUD Framework #
  * JPA DAO object
  * You can define finder methods using no Java code (AOP enabled)
  * A lightweight Detached Criteria API that uses POJOs
  * An easy to use Detached Criteria DSL (domain specific language) that makes using the Criteria API a breeze.

# Details #
  * 1.0.x releases support JSF 1.x framework, and spring IOC.
  * 2.0.x release is currently planned and will support JSF 2.x along with latest versions of JPA and spring IOC. Plans for improving support with Richfaces 4.x and Primefaces.




[release](maven.md)