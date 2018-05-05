#Akka-Http-Cache

Cache is inherent part of Any Application, but for building web application cache use case is
very critical and important because client server communication should be as efficient as
possible

We have created Simple though effective real life example of Akka-Http Cache to elaborate the
technical details with easy implementation approach and to brief the idea where Cache can be
useful while building akka-http application.

Akka-Http cache is based on Caffeine framework which in itself is very robust and fast as
it allows to incorporate Cache asynchronously.

* Akka-Http example demonstrating usage of Cache in Akka-Http
  * 1. We have build cart component of Online Shopping
  * 2. For every product added into the cart we compute total cart value by adding price of
       all products
  * 3. For every request for total cart value we don't want to compute this Heavy Computation
       again and again until there are no new products added into the shopping cart
  * 4. We have just taken a simple scenario to describe a heavy computation but in Production
       it can be call to some un-managed service whose result we want to cache.
  * 5. Since float arithmetic is heavy computation from CPU Perspective so we can use Cache
       to avoid re-computation of total cart untill there are no new products added

For further reading please refer to official Lightbend Inc. Docs

https://doc.akka.io/docs/akka-http/current/common/caching.html

https://github.com/ben-manes/caffeine
