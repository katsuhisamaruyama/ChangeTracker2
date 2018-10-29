# ChangeTracker v2

ChangeTracker is an Eclipse plugin that provides modules related to change operations.  

__org.jtool.changetracker.core__
* Defines changes perations and thier repositoy, and manages, converts, and analyzes change operations stored in the repositories.  Operation history slices are also provided.

__org.jtool.changetracker.recorder__
* Stores change operations that are converted from change macros into the repository.  

__org.jtool.changetracker.replayer__
* Provides UI that replays change operations stored in the repositories.  


## Requirement

JDK 1.8 
[Eclipse](https://www.eclipse.org/) 4.7 (Oxygen) and later  

## License

[Eclipse Public License 1.0 (EPL-1.0)](<https://opensource.org/licenses/eclipse-1.0.php>)

## Install

### Using Eclipse Update Site

Select menu items: "Help" -> "Install New Software..." ->  
Input `http://katsuhisamaruyama.github.io/ChangeTracker2/org.jtool.changetracker.site/site.xml` in the text field of "Work with:"  

### Manually Downloading

Download the latest release of the jar file in the [plug-in directory](<https://github.com/katsuhisamaruyama/ChangeTracker2/tree/master/org.jtool.changetracker.site/plugins>)
and put it in the 'plug-ins' directory under the Eclipse installation. Eclipse needs to be  restarted.

## Older Version

https://github.com/katsuhisamaruyama/changetracker

## Author

[Katsuhisa Maruyama](http://www.fse.cs.ritsumei.ac.jp/~maru/index.html)