There is a slight difference in the ArrayELResolver and ListELResolver.
In 3.0 they do not throw an exception if the index is out of bounds when accessing the value.

The BeanELResolver differences:

* When getting a BeanProperty the Flowable and Eclipse implementations use the Class as a key, Tomcat uses the class name as a key.
* In Tomcat and Flowable bean property methods are lazy evaluated only when they are needed. In Eclipse, they are always evaluated (even if they are not needed)
* In Eclipse and Tomcat the BeanProperty methods are retrieved using the base class of the property descriptor. In Flowable it is done based on the method declaring class.
* Tomcat uses a special local ConcurrentCache that stores using ConcurrentHashMap and WeakHashMap
* In the invoke method Tomcat uses the default ExpressionFactory to coerce the method name object to String.
* Tomcat and Eclipse throw an exception if it couldn't find an exact match for the parameters. See Tomcat Util#findMethod and ELUtil#findMethod.
* When invoking the bean method for getting a value Tomcat and Eclipse try to convert the types to the required type. Tomcat uses the default factory, Eclipse uses the passed ELContext. We should use the context if we are doing that.
