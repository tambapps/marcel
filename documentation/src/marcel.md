# Marcel, a simple and efficient programming language for any JVM

Marcel is a programming language built with the following goals in mind:
- Being simple, not too verbose, allowing to write programs quickly
- Runnable on Android devices (this language is **guaranteed** to be compilable and executable on any Android devices)
- use primitive types when possible (e.g. [for collections](./language-specification/types.md#collections-of-primitives))

Its features are inspired from many languages such as Groovy, Kotlin, Vlang and Perl. You can consult the source code of
this language [on GitHub](https://github.com/tambapps/marcel/wiki)

<br/>
Marcel compiles to Java classes. You can execute marcel script/projects on any JVMs, as long as the Marcel stdlib is included
in your classpath.

As stated above, marcel is guaranteed to be compilable and executable on any Android devices. An android app will come soon(-ish)
 for that.

<br/>

Some integrations with Android APIs should come at some point (e.g. send SMS from a Marcel script, run a Marcel script 
on the background with Android's [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) 
and get a notification once the work is finished)

<br/>

Note that this language is at an early development stage (it started on January 2023) and is therefore not stable yet. I will do my
best not to bring radical changes, but I cannot guaranty that yet.

