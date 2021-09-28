To release `defexception`, we use `lein release`.

`defexception` uses java, and the jar will contain a class file, so to
ensure usability with java versions down to 1.8, ensure that you are
using a java 1.8 version.  If you use [`jenv`](https://www.jenv.be/),
this should be automatic.

You will need to ensure that you have credentials configured for clojars
using the "releases" alias.  See `lein help deploy" under "Authentication".
