# Marshell initialization scripts

Here are some useful initialization scripts that you can use for your shells. These scripts
provide useful methods and utilities to enhance your marshell experience, given a context.

## File explorer initializer

With the below script, transform Marshell into a file explorer, with shell-like methods such as `cd`,
`ls` and a variable `pwd` to get the current directory.


```marcel
pwd = System.getProperty("user.home") ? new File(System.getProperty("user.home"))
    // for Marshell android compatibility
    : getVariable<File>('ROOT_DIR')
if (pwd == null) {
  println("WARNING: Couldn't initialise properly pwd")
}
_hint = pwd


fun File cd(String path) {
  File f = pwd.child(path)
  if (!f.exists()) throw new IllegalArgumentException("Directory $f doesn't exists")
  if (!f.isDirectory()) throw new IllegalArgumentException("File $f isn't a directory")
  pwd = f
  _hint = pwd
  return f
}


fun File file(String path) -> pwd.child(path)

fun void ls() {
  File[] files = pwd.listFiles()
  if (files != null) {
    for (File f in files) println("- $f")
  }
}
```